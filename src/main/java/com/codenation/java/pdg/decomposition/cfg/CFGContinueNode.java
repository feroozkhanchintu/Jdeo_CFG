package com.codenation.java.pdg.decomposition.cfg;

import com.codenation.java.pdg.decomposition.AbstractStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;

public class CFGContinueNode extends CFGNode {
	private String label;
	private CFGNode innerMostLoopNode;
	
	public CFGContinueNode(AbstractStatement statement) {
		super(statement);
		ContinueStatement continueStatement = (ContinueStatement)statement.getStatement();
		if(continueStatement.getLabel() != null)
			label = continueStatement.getLabel().getIdentifier();
	}

	public String getLabel() {
		return label;
	}

	public boolean isLabeled() {
		return label != null;
	}

	public CFGNode getInnerMostLoopNode() {
		return innerMostLoopNode;
	}

	public void setInnerMostLoopNode(CFGNode innerMostLoopNode) {
		this.innerMostLoopNode = innerMostLoopNode;
	}
}
