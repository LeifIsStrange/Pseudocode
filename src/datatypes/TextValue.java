package datatypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import exceptions.runtime.CastingException;
import expressions.special.Type;
import expressions.special.ValueHolder;

public class TextValue extends Value {

	private final String value;

	public TextValue(char val) {
		this(String.valueOf(val));
	}

	public TextValue(String val) {
		value = val;
	}

	@Override
	public boolean canCastTo(Type type) {
		return switch (type) {
		case VAR -> true; // Gibt sich selbst zur�ck
		case VAR_ARRAY -> true; // Gibt char-array zur�ck
		case TEXT_ARRAY -> true; // Gibt char-array zur�ck
		case BOOL -> true; // Gibt false f�r 0 und true f�r alles andere wieder
		case NUMBER_ARRAY -> true; // Gibt die einzelnen Ziffern zur�ck
		case TEXT -> true; // Gibt text-repr�sentation zur�ck
		case NUMBER -> Value.isNumber(value); // Nur wenn es tats�chlich eine Zahl ist. Siehe: TextValue#asNumber
		case BOOL_ARRAY -> Value.asBoolValue(value) != null; // Nur wenn es tats�chlich ein Boolean literal ist.
		};
	}

	@Override
	public ArrayValue asVarArray() throws CastingException {
		List<ValueHolder> params = new ArrayList<>(value.length());
		for (char c : value.toCharArray())
			params.add(new TextValue(c));
		return new ArrayValue(Type.VAR_ARRAY, params);
	}

	@Override
	public ArrayValue asBoolArray() throws CastingException {
		List<ValueHolder> params = new ArrayList<>(value.length());
		for (char c : value.toCharArray())
			params.add(new TextValue(c).asBool());
		return new ArrayValue(Type.BOOL_ARRAY, params);
	}

	@Override
	public ArrayValue asTextArray() throws CastingException {
		List<ValueHolder> params = new ArrayList<>(value.length());
		for (char c : value.toCharArray())
			params.add(new TextValue(c));
		return new ArrayValue(Type.TEXT_ARRAY, params);
	}

	@Override
	public ArrayValue asNumberArray() throws CastingException {
		List<ValueHolder> params = new ArrayList<>(value.length());
		for (char c : value.toCharArray())
			params.add(new TextValue(c).asNumber());
		return new ArrayValue(Type.NUMBER_ARRAY, params);
	}

	@Override
	public BoolValue asBool() throws CastingException {
		Boolean b = asBoolValue(value);
		if (b == null)
			throw new CastingException("Only boolean literals and 1 and 0 can be casted from text to bool.");
		return new BoolValue(b.booleanValue());
	}

	@Override
	public NumberValue asNumber() throws CastingException {
		if (Value.isNumber(value))
			return new NumberValue(new BigDecimal(value));
		throw new CastingException("Cannot cast values other than numbers or boolean literals from text to number.\n");
	}

	@Override
	public TextValue asText() throws CastingException {
		return this;
	}

	@Override
	public Type getType() {
		return Type.TEXT;
	}

	@Override
	public BoolValue eq(Value val) {
		return new BoolValue(val instanceof TextValue t && t.value.equals(value));
	}

	@Override
	public BoolValue neq(Value val) {
		return new BoolValue(!(val instanceof TextValue t && t.value.equals(value)));
	}

	/**
	 * Returns the raw String value of this TextValue.
	 * 
	 * Do not use this in an Operation!
	 */
	public String rawString() {
		return value;
	}

	public static TextValue concat(TextValue t1, TextValue t2) {
		return new TextValue(t1.value + t2.value);
	}

	public static TextValue multiply(TextValue t, int times) {
		return new TextValue(t.value.repeat(times));
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}
}
