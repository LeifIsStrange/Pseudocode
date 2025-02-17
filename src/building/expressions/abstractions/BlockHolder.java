package building.expressions.abstractions;

import building.expressions.abstractions.interfaces.BlockBracket;
import building.expressions.main.functions.NativeFunction;
import building.expressions.main.statements.FlagSpace;
import building.expressions.main.statements.ReturnStatement;
import building.expressions.normal.brackets.OpenBlock;
import building.types.abstractions.SpecificType;
import interpreting.modules.interpreter.Interpreter;

/**
 * Any {@link MainExpression} that end its line with a {@link OpenBlock}.
 * 
 * @see ScopeHolder
 * @see FlagSpace
 */
public abstract class BlockHolder extends MainExpression {

	protected final OpenBlock ob;

	/**
	 * Create a {@link BlockHolder} by passing its {@link OpenBlock}.
	 * 
	 * @param myType shouldn't be null.
	 * @param ob     can be null for {@link NativeFunction}s.
	 */
	public BlockHolder(int lineID, SpecificType myType, OpenBlock ob) {
		super(lineID, myType);
		this.ob = ob;
	}

	/** Returns the lineID of the start of this Block. */
	public final int getStart() {
		return ob.lineIdentifier;
	}

	/** Returns the lineID of the end of this Block. */
	public final int getEnd() {
		return ob.getMatch();
	}

	/**
	 * Calls the line after the {@link OpenBlock}.
	 * 
	 * @return false if this function shouldn't call any other functions afterwards.
	 *         {@link ReturnStatement#execute}
	 */
	public final boolean callFirstLine() {
		return Interpreter.execute(getStart() + 1);
	}

	/** Calls the next line after the closing {@link BlockBracket}. */
	@Override
	public final boolean callNextLine() {
		return Interpreter.execute(getEnd() + 1);
	}

}
