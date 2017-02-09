package gr.uom.java.ast.decomposition.cfg;

import gr.uom.java.ast.decomposition.AbstractStatement;

import java.util.ArrayList;
import java.util.List;

public class CFGExitNode extends CFGNode {

	public CFGExitNode(AbstractStatement statement) {
		super(statement);
	}
}
