package expressions.normal.operators.logic;

import datatypes.BoolValue;
import datatypes.Value;
import expressions.abstractions.interfaces.ValueHolder;
import expressions.normal.operators.Operator;
import expressions.normal.operators.OperatorTypes.InfixOperator;

@FunctionalInterface
interface LogicalOperation {
	BoolValue execute(BoolValue a, BoolValue b);
}

public final class LogicalOperator extends Operator {

	private final LogicalOperation operation;

	public LogicalOperator(int line, InfixOperator operator) {
		super(line, operator);
		operation = switch (operator) {
		case AND -> (b1, b2) -> BoolValue.valueOf(b1.value && b2.value);
		case NAND -> (b1, b2) -> BoolValue.valueOf(!(b1.value && b2.value));

		case OR -> (b1, b2) -> BoolValue.valueOf(b1.value || b2.value);
		case NOR -> (b1, b2) -> BoolValue.valueOf(!(b1.value || b2.value));

		case XOR -> (b1, b2) -> BoolValue.valueOf(b1.value ^ b2.value);
		case XNOR -> (b1, b2) -> BoolValue.valueOf(b1.value == b2.value);

		default -> throw new IllegalArgumentException("Unexpected value: " + operator);
		};
	}

	@Override
	public final Associativity getAssociativity() {
		return Associativity.NONE;
	}

	@Override
	public final Value perform(ValueHolder a, ValueHolder b) {
		return operation.execute(a.getValue().asBool(), b.getValue().asBool());
	}

}