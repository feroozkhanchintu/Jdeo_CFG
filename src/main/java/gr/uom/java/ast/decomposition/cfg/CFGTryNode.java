package gr.uom.java.ast.decomposition.cfg;

import gr.uom.java.ast.decomposition.AbstractStatement;
import gr.uom.java.ast.decomposition.CatchClauseObject;
import gr.uom.java.ast.decomposition.TryStatementObject;

import java.util.ArrayList;
import java.util.List;

public class CFGTryNode extends CFGBlockNode {
	private boolean hasResources;
	public CFGTryNode(AbstractStatement statement) {
		super(statement);
		TryStatementObject tryStatement = (TryStatementObject)statement;
		this.hasResources = tryStatement.hasResources();
	}
	
	public boolean hasResources() {
		return hasResources;
	}

	public boolean hasCatchClause() {
		return ((TryStatementObject)getStatement()).hasCatchClause();
	}
}
