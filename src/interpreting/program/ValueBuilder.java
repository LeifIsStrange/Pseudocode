package interpreting.program;

import static runtime.datatypes.numerical.ConceptualNrValue.NAN;
import static runtime.datatypes.numerical.ConceptualNrValue.NEG_INF;
import static runtime.datatypes.numerical.ConceptualNrValue.POS_INF;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import runtime.datatypes.BoolValue;
import runtime.datatypes.MaybeValue;
import runtime.datatypes.Value;
import runtime.datatypes.numerical.ConceptualNrValue;
import runtime.datatypes.numerical.DecimalValue;
import runtime.datatypes.textual.CharValue;
import runtime.datatypes.textual.TextValue;

public final class ValueBuilder {

	/** Replace escaped characters with the real ascii values. */
	public static TextValue escapeText(String arg) {
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

	// Static String-Checks

	/**
	 * This should get exclusivly used when checking if a String matches the literal words "true" or
	 * "false".
	 */
	public static boolean isBoolean(String value) {
		if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
			return true;
		return false;
	}

	/**
	 * Checks if this String is an acceptable Number.
	 *
	 * (Digits with optional minus, decimal point and period Brackets or any {@link ConceptualNrValue})
	 */
	public static boolean isNumber(String value) {
		return Pattern.matches("(-?)\\d+(\\.\\d+)?(\\(\\d+\\))?", value) ^ Pattern.matches("(?i)(-?" + POS_INF.txt + ")|" + NAN.txt, value);
	}

	/** Checks if this String starts and ends with the symbol " */
	public static boolean isString(String value) {
		return value.startsWith("\"") && value.endsWith("\"");
	}

	/** Checks if this char starts and ends with the symbol ' */
	public static boolean isChar(String value) {
		return value.length() == 3 && value.startsWith("\'") && value.endsWith("\'");
	}

	/** Checks if this char starts and ends with the symbol ' */
	public static boolean isNull(String value) {
		return MaybeValue.NULL.toString().equals(value);
	}

	/** Checks if the passe {@link String} is a literal {@link Value}. */
	public static boolean isLiteral(String arg) {
		return isBoolean(arg) || isNumber(arg) || isString(arg) || isChar(arg) || isNull(arg);
	}

	/**
	 * Builds a {@link Value} from a {@link String}.
	 *
	 * Called in {@link ExpressionType#create(String, int)}
	 *
	 * Returns null if the {@link String} is not a literal value.
	 */
	public static Value stringToLiteral(String arg) {
		// Is Single Value
		if (isBoolean(arg))
			return BoolValue.valueOf("true".equals(arg));
		if (isNumber(arg)) {
			if (POS_INF.txt.equals(arg))
				return POS_INF;
			if (NEG_INF.txt.equals(arg))
				return NEG_INF;
			if (NAN.txt.equals(arg))
				return NAN;
			return DecimalValue.create(new BigDecimal(arg));
		}
		if (isString(arg))
			return ValueBuilder.escapeText(arg.substring(1, arg.length() - 1));
		if (isChar(arg))
			return new CharValue(arg.charAt(1));
		if (isNull(arg))
			return MaybeValue.NULL;
		return null;
	}
}