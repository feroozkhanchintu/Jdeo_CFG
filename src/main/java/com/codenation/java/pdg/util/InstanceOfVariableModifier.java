package com.codenation.java.pdg.util;

import org.eclipse.jdt.core.dom.*;

public class InstanceOfVariableModifier implements ExpressionInstanceChecker {

	public boolean instanceOf(Expression expression) {
		if(expression instanceof Assignment || expression instanceof PrefixExpression || expression instanceof PostfixExpression || expression instanceof MethodInvocation)
			return true;
		else
			return false;
	}
}
