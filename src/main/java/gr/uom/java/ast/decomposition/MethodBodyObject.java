package gr.uom.java.ast.decomposition;

import gr.uom.java.ast.util.ExpressionExtractor;

import java.util.List;

import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MethodBodyObject {
	
	private CompositeStatementObject compositeStatement;
	
	public MethodBodyObject(Block methodBody) {
		this.compositeStatement = new CompositeStatementObject(methodBody, StatementType.BLOCK, null);
        List<Statement> statements = methodBody.statements();
		for(Statement statement : statements) {
			processStatement(compositeStatement, statement);
		}
	}

	public CompositeStatementObject getCompositeStatement() {
		return compositeStatement;
	}

	public boolean containsSuperMethodInvocation() {
		ExpressionExtractor expressionExtractor = new ExpressionExtractor();
		List<Expression> superMethodInvocations = expressionExtractor.getSuperMethodInvocations(compositeStatement.getStatement());
		if(!superMethodInvocations.isEmpty())
			return true;
		else
			return false;
	}

	public boolean containsSuperFieldAccess() {
		ExpressionExtractor expressionExtractor = new ExpressionExtractor();
		List<Expression> superFieldAccesses = expressionExtractor.getSuperFieldAccesses(compositeStatement.getStatement());
		if(!superFieldAccesses.isEmpty())
			return true;
		else
			return false;
	}

	private void processStatement(CompositeStatementObject parent, Statement statement) {
		if(statement instanceof Block) {
			Block block = (Block)statement;
			List<Statement> blockStatements = block.statements();
			CompositeStatementObject child = new CompositeStatementObject(block, StatementType.BLOCK, parent);
			parent.addStatement(child);
			for(Statement blockStatement : blockStatements) {
				processStatement(child, blockStatement);
			}
		}
		else if(statement instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(ifStatement, StatementType.IF, parent);
			AbstractExpression abstractExpression = new AbstractExpression(ifStatement.getExpression(), child);
			child.addExpression(abstractExpression);
			//processExpression(child, ifStatement.getExpression());
			parent.addStatement(child);
			processStatement(child, ifStatement.getThenStatement());
			if(ifStatement.getElseStatement() != null) {
				processStatement(child, ifStatement.getElseStatement());
			}
		}
		else if(statement instanceof ForStatement) {
			ForStatement forStatement = (ForStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(forStatement, StatementType.FOR, parent);
			List<Expression> initializers = forStatement.initializers();
			for(Expression initializer : initializers) {
				AbstractExpression abstractExpression = new AbstractExpression(initializer, child);
				child.addExpression(abstractExpression);
				//processExpression(child, initializer);
			}
			Expression expression = forStatement.getExpression();
			if(expression != null) {
				AbstractExpression abstractExpression = new AbstractExpression(expression, child);
				child.addExpression(abstractExpression);
				//processExpression(child, expression);
			}
			List<Expression> updaters = forStatement.updaters();
			for(Expression updater : updaters) {
				AbstractExpression abstractExpression = new AbstractExpression(updater, child);
				child.addExpression(abstractExpression);
				//processExpression(child, updater);
			}
			parent.addStatement(child);
			processStatement(child, forStatement.getBody());
		}
		else if(statement instanceof EnhancedForStatement) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(enhancedForStatement, StatementType.ENHANCED_FOR, parent);
			SingleVariableDeclaration variableDeclaration = enhancedForStatement.getParameter();
			AbstractExpression variableDeclarationName = new AbstractExpression(variableDeclaration.getName(), child);
			child.addExpression(variableDeclarationName);
			//processExpression(child, variableDeclaration.getName());
			if(variableDeclaration.getInitializer() != null) {
				AbstractExpression variableDeclarationInitializer = new AbstractExpression(variableDeclaration.getInitializer(), child);
				child.addExpression(variableDeclarationInitializer);
				//processExpression(child, variableDeclaration.getInitializer());
			}
			AbstractExpression abstractExpression = new AbstractExpression(enhancedForStatement.getExpression(), child);
			child.addExpression(abstractExpression);
			//processExpression(child, enhancedForStatement.getExpression());
			parent.addStatement(child);
			processStatement(child, enhancedForStatement.getBody());
		}
		else if(statement instanceof WhileStatement) {
			WhileStatement whileStatement = (WhileStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(whileStatement, StatementType.WHILE, parent);
			AbstractExpression abstractExpression = new AbstractExpression(whileStatement.getExpression(), child);
			child.addExpression(abstractExpression);
			//processExpression(child, whileStatement.getExpression());
			parent.addStatement(child);
			processStatement(child, whileStatement.getBody());
		}
		else if(statement instanceof DoStatement) {
			DoStatement doStatement = (DoStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(doStatement, StatementType.DO, parent);
			AbstractExpression abstractExpression = new AbstractExpression(doStatement.getExpression(), child);
			child.addExpression(abstractExpression);
			//processExpression(child, doStatement.getExpression());
			parent.addStatement(child);
			processStatement(child, doStatement.getBody());
		}
		else if(statement instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			StatementObject child = new StatementObject(expressionStatement, StatementType.EXPRESSION, parent);
			//processExpression(child, expressionStatement.getExpression());
			parent.addStatement(child);
		}
		else if(statement instanceof SwitchStatement) {
			SwitchStatement switchStatement = (SwitchStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(switchStatement, StatementType.SWITCH, parent);
			AbstractExpression abstractExpression = new AbstractExpression(switchStatement.getExpression(), child);
			child.addExpression(abstractExpression);
			//processExpression(child, switchStatement.getExpression());
			parent.addStatement(child);
			List<Statement> switchStatements = switchStatement.statements();
			for(Statement switchStatement2 : switchStatements)
				processStatement(child, switchStatement2);
		}
		else if(statement instanceof SwitchCase) {
			SwitchCase switchCase = (SwitchCase)statement;
			StatementObject child = new StatementObject(switchCase, StatementType.SWITCH_CASE, parent);
			/*if(switchCase.getExpression() != null)
				processExpression(child, switchCase.getExpression());*/
			parent.addStatement(child);
		}
		else if(statement instanceof AssertStatement) {
			AssertStatement assertStatement = (AssertStatement)statement;
			StatementObject child = new StatementObject(assertStatement, StatementType.ASSERT, parent);
			/*processExpression(child, assertStatement.getExpression());
			Expression message = assertStatement.getMessage();
			if(message != null)
				processExpression(child, message);*/
			parent.addStatement(child);
		}
		else if(statement instanceof LabeledStatement) {
			LabeledStatement labeledStatement = (LabeledStatement)statement;
			CompositeStatementObject child = new CompositeStatementObject(labeledStatement, StatementType.LABELED, parent);
			/*if(labeledStatement.getLabel() != null)
				processExpression(child, labeledStatement.getLabel());*/
			parent.addStatement(child);
			processStatement(child, labeledStatement.getBody());
		}
		else if(statement instanceof ReturnStatement) {
			ReturnStatement returnStatement = (ReturnStatement)statement;
			StatementObject child = new StatementObject(returnStatement, StatementType.RETURN, parent);
			//processExpression(child, returnStatement.getExpression());
			parent.addStatement(child);	
		}
		else if(statement instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			SynchronizedStatementObject child = new SynchronizedStatementObject(synchronizedStatement, parent);
			//processExpression(child, synchronizedStatement.getExpression());
			parent.addStatement(child);
			processStatement(child, synchronizedStatement.getBody());
		}
		else if(statement instanceof ThrowStatement) {
			ThrowStatement throwStatement = (ThrowStatement)statement;
			StatementObject child = new StatementObject(throwStatement, StatementType.THROW, parent);
			//processExpression(child, throwStatement.getExpression());
			parent.addStatement(child);
		}
		else if(statement instanceof TryStatement) {
			TryStatement tryStatement = (TryStatement)statement;
			TryStatementObject child = new TryStatementObject(tryStatement, parent);
			List<VariableDeclarationExpression> resources = tryStatement.resources();
			for(VariableDeclarationExpression expression : resources) {
				AbstractExpression variableDeclarationExpression = new AbstractExpression(expression, child);
				child.addExpression(variableDeclarationExpression);
				//processExpression(child, expression);
			}
			parent.addStatement(child);
			processStatement(child, tryStatement.getBody());
			List<CatchClause> catchClauses = tryStatement.catchClauses();
			for(CatchClause catchClause : catchClauses) {
				CatchClauseObject catchClauseObject = new CatchClauseObject();
				Block catchClauseBody = catchClause.getBody();
				CompositeStatementObject catchClauseStatementObject = new CompositeStatementObject(catchClauseBody, StatementType.BLOCK, null);
				SingleVariableDeclaration variableDeclaration = catchClause.getException();
				Type variableDeclarationType = variableDeclaration.getType();
				if(variableDeclarationType instanceof UnionType) {
					UnionType unionType = (UnionType)variableDeclarationType;
					List<Type> types = unionType.types();
					for(Type type : types) {
						catchClauseObject.addExceptionType(type.resolveBinding().getQualifiedName());
					}
				}
				else {
					catchClauseObject.addExceptionType(variableDeclarationType.resolveBinding().getQualifiedName());
				}
				AbstractExpression variableDeclarationName = new AbstractExpression(variableDeclaration.getName(), child);
				catchClauseObject.addExpression(variableDeclarationName);
				if(variableDeclaration.getInitializer() != null) {
					AbstractExpression variableDeclarationInitializer = new AbstractExpression(variableDeclaration.getInitializer(), child);
					catchClauseObject.addExpression(variableDeclarationInitializer);
				}
				List<Statement> blockStatements = catchClauseBody.statements();
				for(Statement blockStatement : blockStatements) {
					processStatement(catchClauseStatementObject, blockStatement);
				}
				catchClauseObject.setBody(catchClauseStatementObject);
				child.addCatchClause(catchClauseObject);
			}
			Block finallyBlock = tryStatement.getFinally();
			if(finallyBlock != null) {
				CompositeStatementObject finallyClauseStatementObject = new CompositeStatementObject(finallyBlock, StatementType.BLOCK, null);
				List<Statement> blockStatements = finallyBlock.statements();
				for(Statement blockStatement : blockStatements) {
					processStatement(finallyClauseStatementObject, blockStatement);
				}
				child.setFinallyClause(finallyClauseStatementObject);
			}
		}
		else if(statement instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
			StatementObject child = new StatementObject(variableDeclarationStatement, StatementType.VARIABLE_DECLARATION, parent);
			/*List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
			for(VariableDeclarationFragment fragment : fragments) {
				processExpression(child, fragment.getName());
				processExpression(child, fragment.getInitializer());
			}*/
			parent.addStatement(child);
		}
		else if(statement instanceof ConstructorInvocation) {
			ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
			StatementObject child = new StatementObject(constructorInvocation, StatementType.CONSTRUCTOR_INVOCATION, parent);
			/*List<Expression> arguments = constructorInvocation.arguments();
			for(Expression argument : arguments)
				processExpression(child, argument);*/
			parent.addStatement(child);
		}
		else if(statement instanceof SuperConstructorInvocation) {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
			StatementObject child = new StatementObject(superConstructorInvocation, StatementType.SUPER_CONSTRUCTOR_INVOCATION, parent);
			/*if(superConstructorInvocation.getExpression() != null)
				processExpression(child, superConstructorInvocation.getExpression());
			List<Expression> arguments = superConstructorInvocation.arguments();
			for(Expression argument : arguments)
				processExpression(child, argument);*/
			parent.addStatement(child);
		}
		else if(statement instanceof BreakStatement) {
			BreakStatement breakStatement = (BreakStatement)statement;
			StatementObject child = new StatementObject(breakStatement, StatementType.BREAK, parent);
			/*if(breakStatement.getLabel() != null)
				processExpression(child, breakStatement.getLabel());*/
			parent.addStatement(child);
		}
		else if(statement instanceof ContinueStatement) {
			ContinueStatement continueStatement = (ContinueStatement)statement;
			StatementObject child = new StatementObject(continueStatement, StatementType.CONTINUE, parent);
			/*if(continueStatement.getLabel() != null)
				processExpression(child, continueStatement.getLabel());*/
			parent.addStatement(child);
		}
		else if(statement instanceof EmptyStatement) {
			EmptyStatement emptyStatement = (EmptyStatement)statement;
			StatementObject child = new StatementObject(emptyStatement, StatementType.EMPTY, parent);
			parent.addStatement(child);
		}
	}
/*
	private void processExpression(AbstractMethodFragment parent, Expression expression) {
		if(expression instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.METHOD_INVOCATION, parent);
			parent.addExpression(parentExpression);
			if(methodInvocation.getExpression() != null)
				processExpression(parentExpression, methodInvocation.getExpression());
			List<Expression> arguments = methodInvocation.arguments();
			for(Expression argument : arguments)
				processExpression(parentExpression, argument);
		}
		else if(expression instanceof Assignment) {
			Assignment assignment = (Assignment)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.ASSIGNMENT, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, assignment.getLeftHandSide());
			processExpression(parentExpression, assignment.getRightHandSide());
		}
		else if(expression instanceof CastExpression) {
			CastExpression castExpression = (CastExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.CAST, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, castExpression.getExpression());
		}
		else if(expression instanceof ClassInstanceCreation) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.CLASS_INSTANCE_CREATION, parent);
			parent.addExpression(parentExpression);
			if(classInstanceCreation.getExpression() != null)
				processExpression(parentExpression, classInstanceCreation.getExpression());
			List<Expression> arguments = classInstanceCreation.arguments();
			for(Expression argument : arguments)
				processExpression(parentExpression, argument);
			AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
			if(anonymousClassDeclaration != null) {
				processExpression(parentExpression, anonymousClassDeclaration);
			}
		}
		else if(expression instanceof ConditionalExpression) {
			ConditionalExpression conditionalExpression = (ConditionalExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.CONDITIONAL, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, conditionalExpression.getExpression());
			processExpression(parentExpression, conditionalExpression.getThenExpression());
			processExpression(parentExpression, conditionalExpression.getElseExpression());
		}
		else if(expression instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.FIELD_ACCESS, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, fieldAccess.getExpression());
			processExpression(parentExpression, fieldAccess.getName());
		}
		else if(expression instanceof InfixExpression) {
			InfixExpression infixExpression = (InfixExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.INFIX, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, infixExpression.getLeftOperand());
			processExpression(parentExpression, infixExpression.getRightOperand());
			List<Expression> extendedOperands = infixExpression.extendedOperands();
			for(Expression operand : extendedOperands)
				processExpression(parentExpression, operand);
		}
		else if(expression instanceof InstanceofExpression) {
			InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.INSTANCE_OF, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, instanceofExpression.getLeftOperand());
		}
		else if(expression instanceof ParenthesizedExpression) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.PARENTHESIZED, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, parenthesizedExpression.getExpression());
		}
		else if(expression instanceof PostfixExpression) {
			PostfixExpression postfixExpression = (PostfixExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.POSTFIX, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, postfixExpression.getOperand());
		}
		else if(expression instanceof PrefixExpression) {
			PrefixExpression prefixExpression = (PrefixExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.PREFIX, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, prefixExpression.getOperand());
		}
		else if(expression instanceof SuperMethodInvocation) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.SUPER_METHOD_INVOCATION, parent);
			parent.addExpression(parentExpression);
			List<Expression> arguments = superMethodInvocation.arguments();
			for(Expression argument : arguments)
				processExpression(parentExpression, argument);
		}
		else if(expression instanceof VariableDeclarationExpression) {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.VARIABLE_DECLARATION, parent);
			parent.addExpression(parentExpression);
			List<VariableDeclarationFragment> fragments = variableDeclarationExpression.fragments();
			for(VariableDeclarationFragment fragment : fragments) {
				Expression nameExpression = fragment.getName();
				processExpression(parentExpression, nameExpression);
				Expression initializerExpression = fragment.getInitializer();
				processExpression(parentExpression, initializerExpression);
			}
		}
		else if(expression instanceof ArrayAccess) {
			ArrayAccess arrayAccess = (ArrayAccess)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.ARRAY_ACCESS, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, arrayAccess.getArray());
			processExpression(parentExpression, arrayAccess.getIndex());
		}
		else if(expression instanceof ArrayCreation) {
			ArrayCreation arrayCreation = (ArrayCreation)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.ARRAY_CREATION, parent);
			parent.addExpression(parentExpression);
			List<Expression> dimensions = arrayCreation.dimensions();
			for(Expression dimension : dimensions)
				processExpression(parentExpression, dimension);
				processExpression(parentExpression, arrayCreation.getInitializer());
		}
		else if(expression instanceof ArrayInitializer) {
			ArrayInitializer arrayInitializer = (ArrayInitializer)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.ARRAY_INITIALIZER, parent);
			parent.addExpression(parentExpression);
			List<Expression> expressions = arrayInitializer.expressions();
			for(Expression arrayInitializerExpression : expressions)
				processExpression(parentExpression, arrayInitializerExpression);
		}
		else if(expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.SIMPLE_NAME, parent);
			parent.addExpression(parentExpression);
		}
		else if(expression instanceof QualifiedName) {
			QualifiedName qualifiedName = (QualifiedName)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.QUALIFIED_NAME, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, qualifiedName.getQualifier());
			processExpression(parentExpression, qualifiedName.getName());
		}
		else if(expression instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.SUPER_FIELD_ACCESS, parent);
			parent.addExpression(parentExpression);
			processExpression(parentExpression, superFieldAccess.getName());
		}
		else if(expression instanceof ThisExpression) {
			ThisExpression thisExpression = (ThisExpression)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.THIS, parent);
			parent.addExpression(parentExpression);
			if(thisExpression.getQualifier() != null)
				processExpression(parentExpression, thisExpression.getQualifier());
		}
		else if(expression instanceof TypeLiteral) {
			//TypeLiteral typeLiteral = (TypeLiteral)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.TYPE_LITERAL, parent);
			parent.addExpression(parentExpression);
		}
		else if(expression instanceof StringLiteral) {
			//StringLiteral stringLiteral = (StringLiteral)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.STRING_LITERAL, parent);
			parent.addExpression(parentExpression);
		}
		else if(expression instanceof NullLiteral) {
			//NullLiteral nullLiteral = (NullLiteral)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.NULL_LITERAL, parent);
			parent.addExpression(parentExpression);
		}
		else if(expression instanceof NumberLiteral) {
			//NumberLiteral numberLiteral = (NumberLiteral)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.NUMBER_LITERAL, parent);
			parent.addExpression(parentExpression);
		}
		else if(expression instanceof BooleanLiteral) {
			//BooleanLiteral booleanLiteral = (BooleanLiteral)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.BOOLEAN_LITERAL, parent);
			parent.addExpression(parentExpression);
		}
		else if(expression instanceof CharacterLiteral) {
			//CharacterLiteral characterLiteral = (CharacterLiteral)expression;
			AbstractExpression parentExpression = new AbstractExpression(expression, ExpressionType.CHARACTER_LITERAL, parent);
			parent.addExpression(parentExpression);
		}
	}
*/

	public List<TryStatementObject> getTryStatements() {
		return compositeStatement.getTryStatements();
	}

	public List<String> stringRepresentation() {
		return compositeStatement.stringRepresentation();
	}
}
