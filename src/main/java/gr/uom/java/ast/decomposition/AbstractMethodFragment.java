package gr.uom.java.ast.decomposition;

public abstract class AbstractMethodFragment {
	private AbstractMethodFragment parent;

	protected AbstractMethodFragment(AbstractMethodFragment parent) {
		this.parent = parent;
	}

	public AbstractMethodFragment getParent() {
    	return this.parent;
    }

}
