package building.expressions.normal.casting;

import static building.types.SuperType.MERGED;

import building.expressions.abstractions.Expression;
import building.expressions.abstractions.interfaces.ValueHolder;
import building.types.specific.data.ExpectedType;
import runtime.datatypes.Value;
import runtime.exceptions.CastingException;

/**
 * Changes the type of a value by calling {@link Value#as(ExpectedType)}.
 */
public class ExplicitCast extends Expression implements ValueHolder {

	private final ExpectedType targetType;
	private final ValueHolder target;

	/**
	 * Creates an {@link ExplicitCast}.
	 * 
	 * @param targetType is the type that the target should be casted to.
	 * @param target     is the {@link Value} that gets casted.
	 */
	public ExplicitCast(int lineID, ExpectedType targetType, ValueHolder target) {
		super(lineID, MERGED);
		this.targetType = targetType;
		this.target = target;
		if (targetType == null || target == null)
			throw new AssertionError("Targettype and valueholder cannot be null.");
	}

	/**
	 * Returns the value of {@link #target}, casted to the {@link #targetType}.
	 * 
	 * @throws CastingException, if the cast isn't supported.
	 */
	@Override
	public Value getValue() throws CastingException {
		return target.getValue().as(targetType);
	}

}