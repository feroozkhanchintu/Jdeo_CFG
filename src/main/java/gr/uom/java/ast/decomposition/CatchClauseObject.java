package gr.uom.java.ast.decomposition;

import java.util.ArrayList;
import java.util.List;

public class CatchClauseObject {
	
	private CompositeStatementObject body;
	private List<AbstractExpression> expressionList;
	private TryStatementObject parent;
	
	public CatchClauseObject() {
		this.expressionList = new ArrayList<AbstractExpression>();
		this.parent = null;
	}

    public void setParent(TryStatementObject parent) {
    	this.parent = parent;
    }

	public TryStatementObject getParent() {
		return parent;
	}

	public void setBody(CompositeStatementObject body) {
		this.body = body;
	}

	public CompositeStatementObject getBody() {
		return body;
	}

	public void addExpression(AbstractExpression expression) {
		expressionList.add(expression);
	}

	public List<AbstractExpression> getExpressions() {
		return expressionList;
	}

	public List<String> stringRepresentation() {
		List<String> stringRepresentation = new ArrayList<String>();
		stringRepresentation.add(this.toString());
		stringRepresentation.addAll(body.stringRepresentation());
		return stringRepresentation;
	}

	public List<CompositeStatementObject> getIfStatements() {
		List<CompositeStatementObject> ifStatements = new ArrayList<CompositeStatementObject>();
		ifStatements.addAll(body.getIfStatements());
		return ifStatements;
	}

	public List<CompositeStatementObject> getSwitchStatements() {
		List<CompositeStatementObject> switchStatements = new ArrayList<CompositeStatementObject>();
		switchStatements.addAll(body.getSwitchStatements());
		return switchStatements;
	}

	public List<TryStatementObject> getTryStatements() {
		List<TryStatementObject> tryStatements = new ArrayList<TryStatementObject>();
		tryStatements.addAll(body.getTryStatements());
		return tryStatements;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("catch");
		if(expressionList.size() > 0) {
			sb.append("(");
			for(AbstractExpression expression : expressionList)
				sb.append(expression.toString());
			sb.append(")");
		}
		return sb.toString();
	}
}
