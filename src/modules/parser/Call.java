package modules.parser;

/** Identifies a call. Used exclusivly in the {@link Disassembler}. */
public class Call {

	final int arguments;

	final String name;

	Call(String name, int arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return name + "(" + arguments + ")";
	}
}