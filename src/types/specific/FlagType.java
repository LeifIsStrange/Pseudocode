package types.specific;

import static types.SuperType.EXPECTED_TYPE;
import static types.specific.ExpressionType.NAME;
import static types.specific.KeywordType.FUNC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import expressions.abstractions.Expression;
import expressions.normal.BuilderExpression;
import expressions.normal.containers.Variable;
import modules.formatter.Formatter;
import types.AbstractType;
import types.SuperType;

/**
 * Modifiers that change the behaviour of certain objects/variables.
 * 
 * Flags are no Keywords.
 */
public enum FlagType implements AbstractType {

	/**
	 * Tells, that the following definition doesn't exist in the code files, but rather in the
	 * Interpreter.
	 */
	NATIVE("native", 2, FUNC),

	/**
	 * Tells, that the value of the following variable can only get defined one.
	 * 
	 * Assures that a function can only get called once.
	 */
	FINAL("final", 1, EXPECTED_TYPE, NAME, NATIVE, FUNC),

	/**
	 * Tells, that a following {@link Variable} is completely unchangeable/immutable.
	 * 
	 */
	CONSTANT("const", 1, EXPECTED_TYPE, NAME);

	final String flag;

	public final AbstractType[] expected;

	/**
	 * Determines the order of flags. A low number tells, that the flag is rather at the beginning of
	 * the line than in the middle.
	 */
	final int rank;

	private FlagType(String flag, int rank, AbstractType... expected) {
		this.flag = flag;
		this.rank = rank;
		this.expected = expected;
	}

	@Override
	public String toString() {
		return flag;
	}

	@Override
	public Expression create(String arg, int lineID) {
		if (!flag.equals(arg.strip()))
			return null;
		return new BuilderExpression(this);
	}

	@Override
	public boolean is(SuperType superType) {
		return superType == SuperType.FLAG_TYPE;
	}

	/** Checks, if the passed {@link String} is a {@link FlagType}. */
	public static boolean isFlag(String arg) {
		return flagFromString(arg) != null;
	}

	/** Returns a FlagType, if the passed String matches any. */
	private static FlagType flagFromString(String arg) {
		for (FlagType t : values()) {
			if (t.flag.equals(arg))
				return t;
		}
		return null;
	}

	/**
	 * Orders a list of flags, removes the duplicates and unnecessary ones.
	 * 
	 * Gets called in {@link Formatter#orderFlags}
	 */
	public static List<String> orderFlags(List<String> flags) {
		// Remove duplicates
		flags = new ArrayList<String>(new HashSet<String>(flags));
		// Remove unnecessary.
		if (flags.contains(FINAL.flag) && flags.contains(CONSTANT.flag))
			flags.remove(flags.indexOf(FINAL.flag));

		// Order
		flags.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				FlagType f1 = flagFromString(o1);
				FlagType f2 = flagFromString(o2);
				return Integer.compare(f1.rank, f2.rank);
			}
		});
		return flags;
	}

}