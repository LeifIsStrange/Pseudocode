package building.expressions.normal.operators;

import static building.types.abstractions.SpecificType.MERGED;
import static runtime.datatypes.MaybeValue.NULL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import building.expressions.abstractions.Expression;
import building.expressions.abstractions.interfaces.Operatable;
import building.expressions.abstractions.interfaces.ValueHolder;
import building.expressions.normal.operators.infix.ComparativeOperator;
import building.expressions.normal.operators.infix.InfixOperator;
import building.expressions.normal.operators.infix.LogicalOperator;
import building.expressions.possible.multicall.MultiCall;
import building.expressions.possible.multicall.MultiCallable;
import building.types.specific.operators.InfixOpType;
import errorhandeling.PseudocodeException;
import interpreting.modules.merger.SuperMerger;
import misc.helper.StringHelper;
import runtime.datatypes.Value;

/** Consist of n Operators and n + 1 ValueHolders. */
public final class Operation extends Expression implements MultiCallable, ValueHolder {

	private final List<Operatable> operation;

	/** Gets called when an Operation is constructed in the {@link SuperMerger}. */
	public Operation(int lineID, List<Operatable> op) {
		super(lineID, MERGED);
		if (op.size() < 3)
			throw new AssertionError("An operation has to atleast contain one operator and two values.\nWas " + op);
		this.operation = format(op);
		if (op.contains(NULL))
			throw new PseudocodeException("NullNotAllowed",
					"Null isn't allowed to be in an operation.\n"
							+ StringHelper.pointUnderline(toString(), toString().indexOf(NULL.toString()), NULL.toString().length()),
					getDataPath());
	}

	/** Converts multiple ComparativeOperators */
	private List<Operatable> format(List<Operatable> op) {
		for (int i = 1; i < op.size(); i += 2) {
			if (op.get(i) instanceof ComparativeOperator && i + 2 < op.size() && op.get(i + 2) instanceof ComparativeOperator) {
				op.add(i + 2, new LogicalOperator(lineIdentifier, InfixOpType.AND));
				op.add(i + 3, op.get(i + 1));
				i += 2;
			}
		}
		return Collections.unmodifiableList(op);
	}

	@Override
	public Value getValue() {
		return recValue(new ArrayList<>(operation));
	}

	private Value recValue(List<Operatable> op) {
		ValueHolder a = (ValueHolder) op.remove(0);
		InfixOperator o = (InfixOperator) op.remove(0);
		ValueHolder b = (ValueHolder) op.remove(0);
		if (a instanceof MultiCall mc)
			return o.executeFor(mc.content, b); // <- internal b call
		else {
			a = a.getValue(); // <- a call only here
			if (b instanceof MultiCall mc)
				return o.executeFor(a, mc.content); // <- internal a call
		}
		while (!op.isEmpty()) {
			InfixOperator n = (InfixOperator) op.remove(0);
			ValueHolder c = (ValueHolder) op.remove(0);
			if (o.op.rank >= n.op.rank || n.isRightAssociative()) {
				a = o.perform(a, b);
				o = n;
				b = c;
			} else {
				b = n.perform(b, c);
			}
		}
		return o.perform(a, b);
	}

	@Override
	public String toString() {
		String res = "";
		for (Operatable e : operation) {
			res += switch (e) {
				case InfixOperator io -> " " + io.type + " ";
				case ValueHolder vh -> vh.toString();
				default -> throw new PseudocodeException("IllegalOperation", "Illegal Element in Operation: " + e, getDataPath());
			};
		}
		return res;
	}
}