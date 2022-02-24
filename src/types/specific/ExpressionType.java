package types.specific;

import datatypes.Value;
import expressions.abstractions.Expression;
import expressions.main.CloseScope;
import expressions.main.Declaration;
import expressions.main.OperationAssignment;
import expressions.normal.brackets.OpenScope;
import expressions.normal.containers.Name;
import expressions.possible.Assignment;
import types.AbstractType;
import types.SuperType;

public enum ExpressionType implements AbstractType {

	/**
	 * = Zeichen
	 *
	 * @see Declaration
	 * @see Assignment
	 */
	ASSIGNMENT("="),

	/**
	 * Ausgeschriebener Wert.
	 *
	 * @see Literal
	 */
	LITERAL("Literal"),

	/**
	 * Ausgeschriebener alphanumerischer Name.
	 *
	 * @see Name
	 */
	NAME("Name"),

	/**
	 * { Zeichen
	 *
	 * @see OpenBlock
	 */
	OPEN_SCOPE("{"),

	/**
	 * } Zeichen
	 *
	 * @see CloseBlock
	 */
	CLOSE_SCOPE("}"),

	/**
	 * +=, -=, *=, /=, %=, ^=
	 * 
	 * @see OperationDeclaration
	 */
	OPERATION_ASSIGNMENT("Operation-Declaration");

	private final String expression;

	ExpressionType(String expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return expression;
	}

	@Override
	public Expression create(String arg, int lineID) {
		return switch (this) {
			// Direct build
			case ASSIGNMENT -> "=".equals(arg) ? new Assignment(lineID) : null;
			case OPEN_SCOPE -> "{".equals(arg) ? new OpenScope(lineID) : null;
			case CLOSE_SCOPE -> "}".equals(arg) ? new CloseScope(lineID) : null;
			case NAME -> Name.isName(arg) ? new Name(lineID, arg) : null;
			// External build
			case OPERATION_ASSIGNMENT -> OperationAssignment.stringToOpAssign(arg, lineID);
			case LITERAL -> Value.stringToLiteral(arg);
		};
	}

	@Override
	public boolean is(SuperType superType) {
		return superType == SuperType.EXPRESSION_TYPE;
	}
}
