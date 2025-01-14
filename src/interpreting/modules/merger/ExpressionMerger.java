package interpreting.modules.merger;

import static building.expressions.abstractions.GlobalScope.GLOBAL;
import static misc.supporting.Output.print;

import java.util.Collections;
import java.util.List;

import building.expressions.abstractions.Expression;
import building.expressions.abstractions.GlobalScope;
import building.expressions.abstractions.MainExpression;
import building.expressions.abstractions.Scope;
import building.expressions.abstractions.ScopeHolder;
import building.expressions.abstractions.interfaces.Flaggable;
import building.expressions.abstractions.interfaces.Registerable;
import building.expressions.main.CloseBlock;
import building.expressions.main.functions.Definition;
import building.expressions.main.functions.MainFunction;
import building.expressions.main.statements.FlagSpace;
import building.expressions.normal.BuilderExpression;
import building.expressions.possible.allocating.Allocating;
import building.types.abstractions.AbstractType;
import building.types.specific.BuilderType;
import building.types.specific.FlagType;
import building.types.specific.KeywordType;
import errorhandeling.PseudocodeException;
import importing.filedata.paths.DataPath;
import interpreting.program.ProgramLine;
import launching.Main;
import runtime.defmanager.DefManager;

public abstract class ExpressionMerger {

	protected static List<BuilderExpression> line;
	protected static List<BuilderExpression> orgExp;
	protected static int lineID;
	protected static Scope outer;
	protected static DataPath dataPath;

	/**
	 * Takes all pure {@link Expression}s from a {@link ProgramLine} as input and merges them into a
	 * {@link MainExpression}.
	 *
	 * @param line
	 */
	public static MainExpression merge(ProgramLine pline) {
		// Init
		orgExp = Collections.unmodifiableList(pline.getExpressions());
		line = pline.getExpressions();
		lineID = pline.lineID;
		dataPath = pline.dataPath;
		outer = findScope();
		debugLine(outer);
		MainExpression main = (MainExpression) SuperMerger.build();
		// Check if line was correctly build
		if (main == null || !SuperMerger.line.isEmpty()) {
			throw new AssertionError(dataPath + ": Main-Merge got finished too early or was null.\nMain: " + main + "\nLine: " + line
					+ "\nOrgLine: " + orgExp);
		}
		// Sets the Scope
		initScopes(main);
		// Tests if this is illegally in the global-scope.
		globalScopeCheck(main);
		// Sets flags from overlying FlagSpaces
		if (main instanceof Flaggable f)
			collectFlags(f);
		return main;
	}

	/**
	 * Checks if the passed {@link MainExpression} lies in the {@link GlobalScope} and throws an
	 * {@link IllegalCodeFormatException}. This method gets triggered immediatly after the merge in the
	 * {@link ExpressionMerger} and should get contain everything that can lie in the
	 * {@link GlobalScope}. This is tied to {@link #initScopes(MainExpression)}
	 *
	 * Only {@link Allocating}, {@link MainFunction}, {@link Definition}, {@link FlagSpace} and the
	 * corresponding {@link CloseBlock} can lie in the {@link GlobalScope}.
	 */
	private static void globalScopeCheck(MainExpression main) {
		if (!(main instanceof Allocating) && !(main instanceof Definition) && !(main instanceof MainFunction)
				&& !(main instanceof FlagSpace) && !(main instanceof CloseBlock)) {
			if ((main instanceof ScopeHolder sh && sh.getOuterScope() == GLOBAL) || main.getScope() == GLOBAL) {
				throw new PseudocodeException("InvalidPosition", //
						main.toString() + " \"" + main.type + "\" shouldn't lie in the global-scope.", //
						main.getDataPath());
			}
		}
	}

	/**
	 * This function:
	 *
	 * <pre>
	 * -Sets the {@link Scope} of the {@link MainExpression}.
	 * -Registers the inner {@link Scope} if the main is a {@link ScopeHolder}.
	 * -Registers the main, if it is a {@link Registerable} {@link Definition} or {@link MainFunction}.
	 * </pre>
	 */
	private static void initScopes(MainExpression main) {
		// Set the Scope for a fully merged Expression.
		main.setScope(outer);

		// Initialise the own Scope if this main is a ScopeHolder.
		if (main instanceof ScopeHolder sh)
			sh.initScope();

		// Register the main func immediatly.
		if (main instanceof MainFunction)
			((ScopeHolder) main).getOuterScope().register((Registerable) main);

		if (main instanceof Definition r)
			DefManager.register(r);
	}

	/**
	 * If the {@link MainExpression} is a {@link Flaggable}, and lies in a {@link FlagSpace}, it gets
	 * all of the flags from that {@link FlagSpace}.
	 */
	private static void collectFlags(Flaggable main) {
		for (int i = lineID - 1; i >= 0; i--) {
			MainExpression m = Main.PROGRAM.getLine(i).getMainExpression();
			if (m instanceof FlagSpace fs)
				main.addFlags(fs.getFlags());
			if (m instanceof CloseBlock cb)
				i = cb.getMatch();
		}
	}

	/** Finds the Scope of this line. */
	private static Scope findScope() {
		for (int i = lineID - 1; i >= 0; i--) {
			MainExpression m = Main.PROGRAM.getLine(i).getMainExpression();
			if (m instanceof ScopeHolder sh)
				return sh.getScope();
			if (m instanceof CloseBlock cb)
				i = cb.getMatch();
		}
		return GlobalScope.GLOBAL;
	}

	/**
	 * Constructs an {@link Expression} from the {@link AbstractType} of the first
	 * {@link BuilderExpression}.
	 */
	protected static Expression build() {
		BuilderExpression fst = line.get(0);
		// Build the right MainExpression through recursive pattern matching.
		Expression result = switch (fst.type) {
			case KeywordType k:
				yield SuperMerger.buildKeyword();
			case BuilderType b:
				yield SuperMerger.buildAbstract();
			case FlagType f:
				yield (Expression) SuperMerger.buildFlaggable();
			default:
				yield (Expression) SuperMerger.buildVal();
		};
		return result;
	}

	private static void debugLine(Scope scope) {
		// Merge
		String ls = "Merging " + dataPath + " in " + scope.getScopeName() + ": ";
		final int MAX = 50;
		if (ls.length() > MAX)
			print(ls + " \t" + orgExp);
		else
			print(ls + " ".repeat(MAX - ls.length()) + orgExp);
	}
}
