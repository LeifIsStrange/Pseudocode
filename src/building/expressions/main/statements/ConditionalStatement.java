package building.expressions.main.statements;

import static building.types.specific.KeywordType.ANY;
import static building.types.specific.KeywordType.ELIF;
import static building.types.specific.KeywordType.ELSE;
import static building.types.specific.KeywordType.IF;

import building.expressions.abstractions.ScopeHolder;
import building.expressions.abstractions.interfaces.ValueHolder;
import building.expressions.main.loops.ConditionalLoop;
import building.expressions.normal.brackets.OpenBlock;
import building.types.specific.KeywordType;
import errorhandeling.NonExpressionException;
import errorhandeling.PseudocodeException;
import interpreting.modules.interpreter.Interpreter;
import interpreting.program.ProgramLine;

/**
 * If-, Elif- Any- or Else-Statement.
 *
 * @see ScopeHolder
 * @see ConditionalLoop
 */
public final class ConditionalStatement extends ScopeHolder {

	private final ValueHolder condition;
	private ConditionalStatement nextBlock;

	/**
	 * Creates a {@link ConditionalStatement}, based on the passed {@link KeywordType}.
	 *
	 * @param lineID is the identifier of the matching {@link ProgramLine}.
	 * @param myType is the identifying Type, eiter {@link KeywordType#IF}, {@link KeywordType#ELIF}
	 * {@link KeywordType#ANY} or {@link KeywordType#ELSE}.
	 * @param condition can be null
	 * @param os shouldn't be null
	 */
	public ConditionalStatement(int lineID, KeywordType myType, ValueHolder condition, OpenBlock os) {
		super(lineID, myType, os);
		if (myType != IF && myType != ELIF && myType != ANY && myType != ELSE)
			throw new AssertionError("Type has to be if, elif, any or else.");
		this.condition = condition;
	}

	/** Initialises the following elif / any / else statement. */
	public void setNextBlock(ConditionalStatement nextBlock) {
		if (this.nextBlock != null)
			throw new AssertionError("Trying an invalid connection with this Statement.");
		if (nextBlock.is(IF))
			throw new AssertionError("If can only be at the top of an if/elif/any/else Construct.");
		if (is(ELSE)) {
			throw new PseudocodeException("InvalidConstruct", //
					"An else cannot have a following " + nextBlock + "-statement.", //
					getDataPath());
		}
		if (!is(ELIF) && nextBlock.is(ANY)) {
			throw new PseudocodeException("InvalidConstruct", //
					"An any-block can only follow an elif-statement.", //
					getDataPath());
		}
		this.nextBlock = nextBlock;
	}

	@Override
	public boolean execute() {
		try {
			if (is(IF) || is(ELIF)) {
				if (condition.asBool().value) // Execute after condition is true.
					return executeBody() ? Interpreter.execute(findAnyCase()) : false;
				// Find any or end if successfull.
				return Interpreter.execute(findElseCase()); // Find next else if not successfull.
			} else if (is(ANY) && condition != null) {
				// If any-if condition is true
				if (condition.asBool().value) {
					if (!executeBody()) // ... and return got triggered
						return false; // Return
				}
				return Interpreter.execute(endOfConstruct()); // End, if condition was false or statement is done.
			} else // Execute without condition. ANY / ELSE
				return executeBody() ? Interpreter.execute(endOfConstruct()) : false; // Jump to end after execution
		} catch (NonExpressionException e) {
			throw new PseudocodeException(e, getDataPath());
		}
	}

	/**
	 * Execute the body of the current construct.
	 *
	 * @return true if the search for following blocks should procede and false if a
	 * {@link ReturnStatement} was triggered inside of this block.
	 */
	private boolean executeBody() {
		if (!callFirstLine()) {
			getScope().clear();
			return false; // Return Statement got triggered.
		}
		getScope().clear();
		return true; // No Return. Call next block.
	}

	/**
	 * Returns the lineID of the next elif- or else-Statement, or the end of the construct, if none
	 * exist.
	 */
	private int findElseCase() {
		if (nextBlock != null) {
			if (nextBlock.is(ELIF) || nextBlock.is(ELSE))
				return nextBlock.getStart();
			if (nextBlock.is(ANY))
				return nextBlock.findElseCase();
			throw new PseudocodeException("InvalidConstruct", "Unexpected " + nextBlock + " after " + type, getDataPath());
		}
		return endOfConstruct();
	}

	/**
	 * Returns the lineID of the connected any-Statement, or the end of the construct, if none exist.
	 */
	private int findAnyCase() {
		if (nextBlock != null) {
			if (nextBlock.is(ELIF))
				return nextBlock.findAnyCase();
			if (nextBlock.is(ANY))
				return nextBlock.getStart();
		}
		return endOfConstruct();
	}

	/** Returns the lineID after the last elif/else in this construct. */
	private int endOfConstruct() {
		if (nextBlock != null)
			return nextBlock.endOfConstruct();
		return getEnd() + 1;
	}
}
