package building.expressions.main.functions;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import building.expressions.abstractions.ScopeHolder;
import building.expressions.abstractions.interfaces.ValueHolder;
import building.expressions.normal.brackets.OpenBlock;
import building.expressions.normal.containers.Name;
import building.expressions.normal.containers.Variable;
import building.expressions.possible.Call;
import building.types.specific.DataType;
import building.types.specific.FlagType;
import interpreting.modules.interpreter.Interpreter;
import runtime.datatypes.Value;
import runtime.datatypes.array.ArrayValue;
import runtime.exceptions.IllegalCallException;
import runtime.exceptions.IllegalReturnException;

/**
 * This is the class for a Function-Declaration.
 * 
 * If a {@link Function} gets called, this happens through the {@link Call}-Class and
 * {@link Interpreter#call}.
 */
public class Function extends Definition {

	/** All expected parameters. */
	private final LinkedHashMap<Name, DataType> paramBlueprint;

	/**
	 * Defines and registers a {@link Function}.
	 * 
	 * @param name       is the unique {@link Name} of this {@link Definition}.
	 * @param params     is the {@link LinkedHashMap} of all parameters (types and names) of this
	 *                   {@link Function}. Shouldn't be null.
	 * @param returnType is the {@link DataType} of the return value. Should be null if this is a void.
	 * @param os         is the {@link OpenBlock} of this {@link ScopeHolder}. Shouldn't be null.
	 * @param flags      are optional {@link FlagType}s.
	 */
	public Function(int lineID, Name name, LinkedHashMap<Name, DataType> params, DataType returnType, OpenBlock os) {
		super(lineID, name, os);
		if (params == null)
			throw new AssertionError("Params cannot be null.");
		this.paramBlueprint = params;
		this.returnType = returnType;
	}

	@Override
	public Value call(ValueHolder... params) {
		if (expectedParams() != params.length)
			throw new IllegalCallException(getOriginalLine(),
					"The function " + getNameString() + " is called with the wrong amount of params. \nWere: " + params.length
							+ " but should have been " + expectedParams() + ".");
		finalCheck(); // Check if this was already called.
		// Init Params
		int i = 0;
		for (Entry<Name, DataType> param : paramBlueprint.entrySet()) {
			Value v = params[i++].getValue();
			if (v instanceof ArrayValue av)
				av.init();
			new Variable(lineIdentifier, getScope(), param.getValue(), param.getKey(), v);
		}
		callFirstLine();
		getScope().clear();
		// The return-value is now set.
		if (returnType != null && returnVal == null) {
			throw new IllegalReturnException(getOriginalLine(),
					getNameString() + " was defined to return a value of type: " + returnType + ", but returned nothing.");
		}
		Value temp = returnVal;
		returnVal = null;
		return temp;
	}

	@Override
	public int expectedParams() {
		return paramBlueprint.size();
	}
}
