package gr.uom.java.ast.decomposition;

import gr.uom.java.ast.ASTInformation;
import gr.uom.java.ast.ASTInformationGenerator;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

public abstract class AbstractStatement extends AbstractMethodFragment {

	private ASTInformation statement;
	private StatementType type;
	
    public AbstractStatement(ASTNode statement, StatementType type, AbstractMethodFragment parent) {
    	super(parent);
    	this.type = type;
    	this.statement = ASTInformationGenerator.generateASTInformation(statement);
    }

    public ASTNode getStatement() {
    	return this.statement.recoverASTNode();
    }

    public StatementType getType() {
		return type;
	}

	public abstract List<String> stringRepresentation();
}
