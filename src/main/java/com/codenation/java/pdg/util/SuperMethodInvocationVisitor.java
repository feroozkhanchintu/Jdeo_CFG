package com.codenation.java.pdg.util;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import java.util.ArrayList;
import java.util.List;

public class SuperMethodInvocationVisitor extends ASTVisitor {
	private List<SuperMethodInvocation> superMethodInvocations = new ArrayList<SuperMethodInvocation>();

	public boolean visit(SuperMethodInvocation node) {
		superMethodInvocations.add(node);
		return super.visit(node);
	}

	public List<SuperMethodInvocation> getSuperMethodInvocations() {
		return superMethodInvocations;
	}
}
