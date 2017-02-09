package gr.uom.java.ast;

import gr.uom.java.ast.decomposition.CatchClauseObject;
import gr.uom.java.ast.decomposition.TryStatementObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class ClassDeclarationObject {
	protected String name;
	protected List<MethodObject> methodList;
	protected List<FieldObject> fieldList;

	public ClassDeclarationObject() {
		this.methodList = new ArrayList<MethodObject>();
		this.fieldList = new ArrayList<FieldObject>();
	}

	public abstract CompilationUnit getCompilationUnit();
	public abstract ClassObject getClassObject();
	public abstract IFile getIFile();
	public abstract TypeObject getSuperclass();

	public boolean addMethod(MethodObject method) {
		return methodList.add(method);
	}

	public boolean addField(FieldObject f) {
		return fieldList.add(f);
	}

	public List<MethodObject> getMethodList() {
		return methodList;
	}

	public ListIterator<MethodObject> getMethodIterator() {
		return methodList.listIterator();
	}

	public ListIterator<FieldObject> getFieldIterator() {
		return fieldList.listIterator();
	}


	public int getNumberOfMethods() {
		return methodList.size();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean containsMethodWithTestAnnotation() {
    	for(MethodObject method : methodList) {
    		if(method.hasTestAnnotation())
    			return true;
    	}
    	return false;
    }

    public boolean extendsTestCase() {
    	TypeObject superclass = this.getSuperclass();
    	if(superclass == null)
    		return false;
    	else if(superclass.getClassType().equals("junit.framework.TestCase"))
    		return true;
    	else {
    		ClassObject superClassObject = ASTReader.getSystemObject().getClassObject(superclass.getClassType());
    		if(superClassObject != null)
    			return superClassObject.extendsTestCase();
    	}
    	return false;
    }

    public boolean hasFieldType(String className) {
        ListIterator<FieldObject> fi = getFieldIterator();
        while(fi.hasNext()) {
            FieldObject fo = fi.next();
            if(fo.getType().getClassType().equals(className))
                return true;
        }
        return false;
    }


	public FieldObject getField(FieldInstructionObject fieldInstruction) {
		for(FieldObject field : fieldList) {
			if(field.equals(fieldInstruction)) {
				return field;
			}
		}
		return null;
	}
	
	protected FieldObject findField(FieldInstructionObject fieldInstruction) {
		FieldObject field = getField(fieldInstruction);
		if(field != null) {
			return field;
		}
		else {
			TypeObject superclassType = getSuperclass();
			if(superclassType != null) {
				ClassObject superclassObject = ASTReader.getSystemObject().getClassObject(superclassType.toString());
				if(superclassObject != null) {
					return superclassObject.findField(fieldInstruction);
				}
			}
		}
		return null;
	}
}
