package parsing.program;

import static datatypes.numerical.ConceptualNrValue.NAN;
import static datatypes.numerical.ConceptualNrValue.NEG_INF;
import static datatypes.numerical.ConceptualNrValue.POS_INF;

import java.math.BigDecimal;

import datatypes.BoolValue;
import datatypes.TextValue;
import datatypes.Value;
import datatypes.numerical.DecimalValue;
import exceptions.runtime.UnexpectedTypeError;

public final class ValueBuilder {

	/**
	 * Replace escaped characters with the real ascii values.
	 */
	private static TextValue excapeText(String arg) {
		for (int i = 0; i < arg.length() - 1; i++) {
			if (arg.charAt(i) == '\\') {
				char c = switch (arg.charAt(i + 1)) {
				case 't' -> '\t';
				case 'r' -> '\r';
				case 'n' -> '\n';
				case 'f' -> '\f';
				case '\\' -> '\\';
				case '"' -> '"';
				default -> throw new IllegalArgumentException("Unexpected value: " + arg.charAt(i + 1));
				};
				arg = arg.substring(0, i) + c + arg.substring(i + 2);
			}
		}
		return new TextValue(arg);
	}

	/** Creates a value from a string. */
	public static Value stringToLiteral(String arg) {
		// Is Single Value
		if (Value.isBoolean(arg))
			return BoolValue.valueOf("true".equals(arg));
		if (Value.isNumber(arg)) {
			if (POS_INF.txt.equals(arg))
				return POS_INF;
			if (NEG_INF.txt.equals(arg))
				return NEG_INF;
			if (NAN.txt.equals(arg))
				return NAN;
			return DecimalValue.create(new BigDecimal(arg));
		}
		if (Value.isString(arg))
			return excapeText(arg.substring(1, arg.length() - 1));
		throw new UnexpectedTypeError("Type must be known by now!");
	}
}