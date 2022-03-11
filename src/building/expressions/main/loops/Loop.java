package building.expressions.main.loops;

import static runtime.datatypes.numerical.NumberValue.ONE;
import static runtime.datatypes.numerical.NumberValue.ZERO;

import building.expressions.abstractions.ScopeHolder;
import building.expressions.abstractions.interfaces.ValueHolder;
import building.expressions.normal.brackets.OpenBlock;
import building.types.AbstractType;
import runtime.datatypes.numerical.DecimalValue;
import runtime.datatypes.numerical.NumberValue;

/**
 * A loop is a Scope that gets called repeatedly, while or until a condition is true. This gets
 * testet by {@link Loop#doContinue()}.
 * 
 * @see ConditionalLoop
 * @see ForEachLoop
 * @see FromToLoop
 * @see RepeatLoop
 */
public abstract class Loop extends ScopeHolder {

	// These should get initialised at merge
	protected ValueHolder startHolder = ZERO;
	protected ValueHolder incHolder = ONE;

	// These should get initialised at init
	protected NumberValue start;
	protected NumberValue inc;

	/** Copies the constructor {@link ScopeHolder#ScopeHolder(int, AbstractType, OpenBlock)}. */
	public Loop(int lineID, AbstractType myType, OpenBlock os) {
		super(lineID, myType, os);
	}

	/**
	 * Executes this loop as long as its run-condition is satisfied.
	 * 
	 * This method calls {@link Loop#doContinue()} for every iteration.
	 */
	@Override
	public final boolean execute() {
		initLoop();
		if (start == null || inc == null)
			throw new AssertionError("Start and end have to be initialised. See: initLoop()");
		NumberValue i = start;
		while (doContinue(i)) {
			getScope().initCounter(i, getScope(), getOriginalLine());
			if (!callFirstLine()) {
				getScope().clear();
				return false;
			}
			getScope().clear();
			i = i.add(inc);
		}
		return callNextLine();
	}

	/**
	 * Only gets called by {@link Loop#execute()} before a loop gets executed.
	 * 
	 * Set {@link #start} and {@link #inc} here.
	 */
	protected void initLoop() {
		start = startHolder.getValue().asNumber();
		inc = incHolder.getValue().asNumber();
	}

	/**
	 * Only gets called by {@link Loop#execute()}.
	 * 
	 * @param iteration is the current iteration, starting at {@link DecimalValue#ZERO}.
	 */
	protected abstract boolean doContinue(NumberValue iteration);
}