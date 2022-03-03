package modules.merger;

import static types.SuperType.ASSIGNMENT_TYPE;
import static types.SuperType.FLAG_TYPE;
import static types.SuperType.INFIX_OPERATOR;
import static types.SuperType.POSTFIX_OPERATOR;
import static types.specific.BuilderType.*;
import static types.specific.ExpressionType.NAME;
import static types.specific.FlagType.CONSTANT;
import static types.specific.FlagType.FINAL;
import static types.specific.FlagType.NATIVE;
import static types.specific.KeywordType.FUNC;
import static types.specific.KeywordType.IS;
import static types.specific.data.DataType.VAR;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import exceptions.parsing.IllegalCodeFormatException;
import exceptions.runtime.UnexpectedTypeError;
import expressions.abstractions.Expression;
import expressions.abstractions.interfaces.ValueChanger;
import expressions.abstractions.interfaces.ValueHolder;
import expressions.main.CloseScope;
import expressions.normal.BuilderExpression;
import expressions.normal.brackets.OpenScope;
import expressions.normal.containers.Name;
import expressions.normal.flag.Flaggable;
import modules.merger.sub.FuncMerger;
import modules.merger.sub.LoopMerger;
import modules.merger.sub.OpMerger;
import modules.merger.sub.StatementMerger;
import modules.merger.sub.ValueMerger;
import modules.parser.program.ValueBuilder;
import types.AbstractType;
import types.specific.BuilderType;
import types.specific.ExpressionType;
import types.specific.FlagType;
import types.specific.KeywordType;
import types.specific.data.ExpectedType;
import types.specific.operators.PrefixOpType;

/**
 * Every build-Method should atleast remove the first element of the line.
 * 
 * The subclasses of this are all indirectly called by the switch-cases in {@link #build()}.
 */
public abstract class SuperMerger extends ExpressionMerger {

	/**
	 * Constructs an {@link Expression} from the {@link AbstractType} of the first
	 * {@link BuilderExpression}.
	 */
	protected static Expression build() {
		BuilderExpression fst = line.get(0);
		// Build the right MainExpression through recursive pattern matching.
		Expression result = switch (fst.type) {
			case KeywordType k -> buildKeyword();
			case BuilderType b -> buildAbstract();
			case FlagType f -> (Expression) buildFlaggable();
			default -> (Expression) buildVal();
		};
		return result;
	}

	/**
	 * Constructs an {@link ValueHolder} from the {@link AbstractType} of the first
	 * {@link BuilderExpression}.
	 */
	protected static ValueHolder buildVal() {
		BuilderExpression fst = line.get(0);
		BuilderExpression sec = line.size() > 1 ? line.get(1) : null;
		ValueHolder result = switch (fst.type) {
			case ExpressionType e:
				yield switch (e) {
					case LITERAL:
						yield ValueBuilder.stringToLiteral(line.remove(0).value);
					case NAME:
						if (sec != null) {
							if (sec.is(OPEN_BRACKET))
								yield ValueMerger.buildCall();
							if (sec.is(ARRAY_START))
								yield ValueMerger.buildArrayAccess();
							if (sec.is(ASSIGNMENT_TYPE))
								yield ValueMerger.buildAssignment();
						}
						yield buildName();
				};
			case BuilderType b:
				yield switch (b) {
					case ARRAY_START -> ValueMerger.buildArrayLiteral();
					case OPEN_BRACKET -> ValueMerger.buildBracketedExpression();
					case MULTI_CALL_LINE -> ValueMerger.buildMultiCall();
					// Non-Value-BuilderTypes
					default -> throw new UnexpectedTypeError(orgLine, b);
				};
			case ExpectedType e:
				yield ValueMerger.buildDeclaration();
			case PrefixOpType p:
				yield OpMerger.buildPrefix();
			default:
				throw new UnexpectedTypeError(orgLine, fst.type);
		};
		// Check for follow-ups.
		if (!line.isEmpty()) {
			if (line.get(0).is(INFIX_OPERATOR))
				result = OpMerger.buildOperation(result);
			else if (line.get(0).is(POSTFIX_OPERATOR))
				result = OpMerger.buildPostfix((ValueChanger) result);
			else if (line.get(0).is(IS))
				result = ValueMerger.buildIsStatement(result);
		}
		return result;
	}

	/** Constructs an {@link Expression} from a {@link BuilderType}. This includes Scopes. */
	protected static Expression buildAbstract() {
		BuilderType type = (BuilderType) line.get(0).type;
		return switch (type) {
			// Simple BuilderTypes
			case OPEN_SCOPE -> buildOpenScope();
			case CLOSE_SCOPE -> buildCloseScope();
			// Complex BuilderTypes
			case ARRAY_START, OPEN_BRACKET, MULTI_CALL_LINE -> (Expression) buildVal();
			// Decorative BuilderTypes
			default -> throw new UnexpectedTypeError(orgLine, type);
		};
	}

	/** Constructs an {@link Expression} from a {@link KeywordType}. */
	protected static Expression buildKeyword() {
		KeywordType type = (KeywordType) line.get(0).type;
		return switch (type) {
			// Loops
			case FOR -> LoopMerger.buildForEach();
			case REPEAT -> LoopMerger.buildRepeat();
			case FROM -> LoopMerger.buildFromTo();
			case WHILE, UNTIL -> LoopMerger.buildConditional(type);
			// Statements
			case IF, ELIF, ANY, ELSE -> StatementMerger.buildConditional(type);
			case RETURN -> StatementMerger.buildReturn();
			// Callables
			case FUNC -> FuncMerger.buildFunc(false);
			case MAIN -> FuncMerger.buildMain();
			case IS, IMPORT -> throw new UnexpectedTypeError(orgLine, type);
		};
	}

	/** [{] */
	protected static OpenScope buildOpenScope() {
		line.remove(0);
		return new OpenScope(lineID);
	}

	/** [}] */
	protected static CloseScope buildCloseScope() {
		line.remove(0);
		return new CloseScope(lineID);
	}

	/** [NAME] **/
	protected static Name buildName() {
		return new Name(lineID, line.remove(0).value);
	}

	/** [Start] [ValueHolder] ((,) [ValueHolder])] [End] */
	protected static ValueHolder[] buildParts() {
		List<ValueHolder> parts = new ArrayList<>();
		line.remove(0); // Start
		do {
			Expression fst = line.get(0);
			if (!fst.is(MULTI_CALL_LINE) && !fst.is(CLOSE_BRACKET) && !fst.is(ARRAY_END))
				parts.add(buildVal());
		} while (line.remove(0).is(COMMA));
		return parts.toArray(new ValueHolder[parts.size()]);
	}

	/** [EXPECTED_TYPE] */
	protected static ExpectedType buildExpType() {
		return (ExpectedType) line.remove(0).type;
	}

	/**
	 * Super-Routine for all Flaggables.
	 * 
	 * @param flags are the flags. They get set by this Method.
	 */
	private static Flaggable buildFlaggable() {
		Set<FlagType> flags = new HashSet<>();
		while (line.get(0).is(FLAG_TYPE)) {
			if (!flags.add((FlagType) line.remove(0).type))
				throw new IllegalCodeFormatException(orgLine, "Duplicate flag. Line: " + orgLine);
		}
		Flaggable f = null;
		// Declaration with optional flags
		if (line.get(0).type instanceof ExpectedType)
			f = ValueMerger.buildDeclaration();
		// Declaration of a constant without type and optional flags
		else if (flags.contains(CONSTANT) || flags.contains(FINAL) && line.get(0).is(NAME)) {
			line.add(0, new BuilderExpression(lineID, VAR));
			f = ValueMerger.buildDeclaration();
		}
		// Returnable-Declaration with optional flags
		else if (line.get(0).is(FUNC)) {
			f = FuncMerger.buildFunc(flags.remove(NATIVE));
		} else
			throw new UnexpectedTypeError(line.get(0).type);
		f.setFlags(flags);
		return f;
	}
}