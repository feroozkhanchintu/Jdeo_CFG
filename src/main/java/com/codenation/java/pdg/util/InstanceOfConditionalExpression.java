package com.codenation.java.pdg.util;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;

public class InstanceOfConditionalExpression implements ExpressionInstanceChecker {

	public boolean instanceOf(Expression expression) {
		if(expression instanceof ConditionalExpression)
			return true;
		else
			return false;
	}

}
