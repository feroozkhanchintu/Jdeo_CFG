package com.codenation.java.pdg.util;

import org.eclipse.jdt.core.dom.Expression;

public class InstanceOfExpression implements ExpressionInstanceChecker {

	public boolean instanceOf(Expression expression) {
		return true;
	}

}
