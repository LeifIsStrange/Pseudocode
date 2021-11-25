package programreader.program;

import java.util.ArrayList;

import programreader.expressions.main.Call;
import programreader.expressions.main.functions.Function;
import programreader.expressions.normal.CloseBracket;
import programreader.expressions.normal.Comma;
import programreader.expressions.normal.Literal;
import programreader.expressions.normal.Name;
import programreader.expressions.normal.OpenBracket;
import programreader.expressions.normal.Operation;
import programreader.expressions.special.Expression;
import programreader.expressions.special.Operator;
import programreader.expressions.special.ValueHolder;

public final class ValueBuilder {

	private static ArrayList<Expression> list;

	private static int lineIndex;

	public static ArrayList<Expression> buildLine(ArrayList<Expression> l, int line) {
		list = l;
		lineIndex = line;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof Literal || list.get(i) instanceof Name)
				build(i);
		}
		return list;
	}

	private static ValueHolder build(int i) {
		if (list.get(i) instanceof ValueHolder) {

			if (list.get(i) instanceof Literal) {
				if (i + 1 < list.size() && list.get(i + 1) instanceof Operator) {
					Operation op = new Operation(i, buildOperation(i));
					list.add(i, op);
					return op;
				} else
					return (Literal) list.get(i);
			}

			else if (i + 1 < list.size() && list.get(i) instanceof Name) {
				if (list.get(i + 1) instanceof Comma || list.get(i + 1) instanceof CloseBracket) {
					return (Name) list.get(i);
				} else if (list.get(i + 1) instanceof Operator) {
					Operation op = new Operation(i, buildOperation(i));
					list.set(i, op);
					return op;
				} else if (list.get(i + 1) instanceof OpenBracket && !(list.get(0) instanceof Function)) {
					if (list.get(i + 2) instanceof CloseBracket) {
						Call c = new Call(lineIndex);
						c.init(((Name) list.get(i)).getName(), null);
						list.remove(i + 1);
						list.set(i, c);
						return c;
					} else {
						Call c = new Call(lineIndex);
						c.init(((Name) list.get(i)).getName(), buildParams(i + 2));
						list.set(i, c); // Name
						list.remove(i + 1);
						return c;
					}
				}
			}
		}
		return null;
	}

	private static ArrayList<ValueHolder> buildParams(int pos) {
		ArrayList<ValueHolder> params = new ArrayList<>();
		for (int i = pos; i < list.size(); i++) {
			if (list.get(i) instanceof CloseBracket)
				break;
			if (list.get(i) instanceof Name || list.get(i) instanceof Literal) {
				ValueHolder p = build(i);
				params.add(p);
			}
		}
		while (!(list.get(pos) instanceof CloseBracket))
			list.remove(pos);
		list.remove(pos);
		return params;
	}

	private static ArrayList<Expression> buildOperation(int pos) {
		ArrayList<Expression> operation = new ArrayList<>();
		for (int i = pos; i < list.size(); i++) {
			if (list.get(i) instanceof Literal || list.get(i) instanceof Operator)
				operation.add(list.get(i));
			else if (list.get(i) instanceof Name) {
				if (i + 1 < list.size() && list.get(i + 1) instanceof OpenBracket)
					operation.add((Expression) build(i));
				else
					operation.add(list.get(i));
			} else
				break;
		}
		for (int i = 0; i < operation.size(); i++)
			list.remove(pos);
		return operation;
	}
}