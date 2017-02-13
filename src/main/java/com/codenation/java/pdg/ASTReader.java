package com.codenation.java.pdg;

import com.codenation.java.pdg.decomposition.AbstractExpression;
import com.codenation.java.pdg.decomposition.MethodBodyObject;
import com.codenation.java.pdg.decomposition.cfg.CFG;
import com.codenation.java.pdg.util.StatementExtractor;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.IDocument;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ASTReader {

    private static SystemObject systemObject;
    public static final int JLS = AST.JLS8;


    public static List<AbstractTypeDeclaration> getRecursivelyInnerTypes(AbstractTypeDeclaration typeDeclaration) {
        List<AbstractTypeDeclaration> innerTypeDeclarations = new ArrayList<AbstractTypeDeclaration>();
        StatementExtractor statementExtractor = new StatementExtractor();
        List<BodyDeclaration> bodyDeclarations = typeDeclaration.bodyDeclarations();
        for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
            if (bodyDeclaration instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
                if (methodDeclaration.getBody() != null) {
                    List<Statement> statements = statementExtractor.getTypeDeclarationStatements(methodDeclaration.getBody());
                    for (Statement statement : statements) {
                        TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) statement;
                        AbstractTypeDeclaration declaration = typeDeclarationStatement.getDeclaration();
                        if (declaration instanceof TypeDeclaration) {
                            innerTypeDeclarations.add((TypeDeclaration) declaration);
                        }
                    }
                }
            } else if (bodyDeclaration instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) bodyDeclaration;
                innerTypeDeclarations.add(type);
                innerTypeDeclarations.addAll(getRecursivelyInnerTypes(type));
            } else if (bodyDeclaration instanceof EnumDeclaration) {
                EnumDeclaration type = (EnumDeclaration) bodyDeclaration;
                innerTypeDeclarations.add(type);
                innerTypeDeclarations.addAll(getRecursivelyInnerTypes(type));
            }
        }
        return innerTypeDeclarations;
    }

    private List<ClassObject> parseAST(CompilationUnit compilationUnit) {

        List<ClassObject> classObjects = new ArrayList<ClassObject>();
        List<AbstractTypeDeclaration> topLevelTypeDeclarations = compilationUnit.types();
        for (AbstractTypeDeclaration abstractTypeDeclaration : topLevelTypeDeclarations) {
            if (abstractTypeDeclaration instanceof TypeDeclaration) {
                TypeDeclaration topLevelTypeDeclaration = (TypeDeclaration) abstractTypeDeclaration;
                List<AbstractTypeDeclaration> typeDeclarations = new ArrayList<AbstractTypeDeclaration>();
                typeDeclarations.add(topLevelTypeDeclaration);
                typeDeclarations.addAll(getRecursivelyInnerTypes(topLevelTypeDeclaration));
                for (AbstractTypeDeclaration typeDeclaration : typeDeclarations) {
                    if (typeDeclaration instanceof TypeDeclaration) {
                        final ClassObject classObject = processTypeDeclaration(null, null, (TypeDeclaration) typeDeclaration, null);
                        classObjects.add(classObject);
                    } else if (typeDeclaration instanceof EnumDeclaration) {
                        final ClassObject classObject = processEnumDeclaration(null, null, (EnumDeclaration) typeDeclaration, null);
                        classObjects.add(classObject);
                    }
                }
            } else if (abstractTypeDeclaration instanceof EnumDeclaration) {
                EnumDeclaration enumDeclaration = (EnumDeclaration) abstractTypeDeclaration;
                final ClassObject classObject = processEnumDeclaration(null, null, enumDeclaration, null);
                classObjects.add(classObject);
            }
        }
        return classObjects;
    }


    private ClassObject processTypeDeclaration(IFile iFile, IDocument document, TypeDeclaration typeDeclaration, List<Comment> comments) {
        final ClassObject classObject = new ClassObject();
        classObject.setIFile(iFile);
        ITypeBinding typeDeclarationBinding = typeDeclaration.resolveBinding();
        if (typeDeclarationBinding.isLocal()) {
            ITypeBinding declaringClass = typeDeclarationBinding.getDeclaringClass();
            String className = declaringClass.getQualifiedName() + "." + typeDeclarationBinding.getName();
            classObject.setName(className);
        } else {
            classObject.setName(typeDeclarationBinding.getQualifiedName());
        }
        classObject.setAbstractTypeDeclaration(typeDeclaration);

        if (typeDeclaration.isInterface()) {
            classObject.setInterface(true);
        }

        int modifiers = typeDeclaration.getModifiers();
        if ((modifiers & Modifier.ABSTRACT) != 0)
            classObject.setAbstract(true);

        if ((modifiers & Modifier.PUBLIC) != 0)
            classObject.setAccess(Access.PUBLIC);
        else if ((modifiers & Modifier.PROTECTED) != 0)
            classObject.setAccess(Access.PROTECTED);
        else if ((modifiers & Modifier.PRIVATE) != 0)
            classObject.setAccess(Access.PRIVATE);
        else
            classObject.setAccess(Access.NONE);

        if ((modifiers & Modifier.STATIC) != 0)
            classObject.setStatic(true);

        Type superclassType = typeDeclaration.getSuperclassType();
        if (superclassType != null) {
            ITypeBinding binding = superclassType.resolveBinding();
            String qualifiedName = binding.getQualifiedName();
            TypeObject typeObject = TypeObject.extractTypeObject(qualifiedName);
            classObject.setSuperclass(typeObject);
        }

        List<Type> superInterfaceTypes = typeDeclaration.superInterfaceTypes();
        for (Type interfaceType : superInterfaceTypes) {
            ITypeBinding binding = interfaceType.resolveBinding();
            String qualifiedName = binding.getQualifiedName();
            TypeObject typeObject = TypeObject.extractTypeObject(qualifiedName);
            classObject.addInterface(typeObject);
        }

        FieldDeclaration[] fieldDeclarations = typeDeclaration.getFields();
        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            processFieldDeclaration(classObject, fieldDeclaration);
        }

        MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            processMethodDeclaration(classObject, methodDeclaration);
        }
        return classObject;
    }

    private ClassObject processEnumDeclaration(IFile iFile, IDocument document, EnumDeclaration enumDeclaration, List<Comment> comments) {
        final ClassObject classObject = new ClassObject();
        classObject.setEnum(true);
        classObject.setIFile(iFile);
        classObject.setName(enumDeclaration.resolveBinding().getQualifiedName());
        classObject.setAbstractTypeDeclaration(enumDeclaration);

        int modifiers = enumDeclaration.getModifiers();
        if ((modifiers & Modifier.ABSTRACT) != 0)
            classObject.setAbstract(true);

        if ((modifiers & Modifier.PUBLIC) != 0)
            classObject.setAccess(Access.PUBLIC);
        else if ((modifiers & Modifier.PROTECTED) != 0)
            classObject.setAccess(Access.PROTECTED);
        else if ((modifiers & Modifier.PRIVATE) != 0)
            classObject.setAccess(Access.PRIVATE);
        else
            classObject.setAccess(Access.NONE);

        if ((modifiers & Modifier.STATIC) != 0)
            classObject.setStatic(true);

        List<Type> superInterfaceTypes = enumDeclaration.superInterfaceTypes();
        for (Type interfaceType : superInterfaceTypes) {
            ITypeBinding binding = interfaceType.resolveBinding();
            String qualifiedName = binding.getQualifiedName();
            TypeObject typeObject = TypeObject.extractTypeObject(qualifiedName);
            classObject.addInterface(typeObject);
        }

        List<EnumConstantDeclaration> enumConstantDeclarations = enumDeclaration.enumConstants();
        for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarations) {
            EnumConstantDeclarationObject enumConstantDeclarationObject = new EnumConstantDeclarationObject(enumConstantDeclaration.getName().getIdentifier());
            enumConstantDeclarationObject.setEnumName(classObject.getName());
            enumConstantDeclarationObject.setEnumConstantDeclaration(enumConstantDeclaration);
            List<Expression> arguments = enumConstantDeclaration.arguments();
            for (Expression argument : arguments) {
                AbstractExpression abstractExpression = new AbstractExpression(argument);
                enumConstantDeclarationObject.addArgument(abstractExpression);
            }
            classObject.addEnumConstantDeclaration(enumConstantDeclarationObject);
        }

        List<BodyDeclaration> bodyDeclarations = enumDeclaration.bodyDeclarations();
        for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
            if (bodyDeclaration instanceof MethodDeclaration) {
                processMethodDeclaration(classObject, (MethodDeclaration) bodyDeclaration);
            } else if (bodyDeclaration instanceof FieldDeclaration) {
                processFieldDeclaration(classObject, (FieldDeclaration) bodyDeclaration);
            }
        }
        return classObject;
    }

    private void processFieldDeclaration(final ClassObject classObject, FieldDeclaration fieldDeclaration) {
        Type fieldType = fieldDeclaration.getType();
        ITypeBinding binding = fieldType.resolveBinding();
        int fieldDeclarationStartPosition = fieldDeclaration.getStartPosition();
        int fieldDeclarationEndPosition = fieldDeclarationStartPosition + fieldDeclaration.getLength();

        List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
        for (VariableDeclarationFragment fragment : fragments) {
            String qualifiedName = binding.getQualifiedName();
            TypeObject typeObject = TypeObject.extractTypeObject(qualifiedName);
            typeObject.setArrayDimension(typeObject.getArrayDimension() + fragment.getExtraDimensions());
            FieldObject fieldObject = new FieldObject(typeObject, fragment.getName().getIdentifier());
            fieldObject.setClassName(classObject.getName());
            fieldObject.setVariableDeclarationFragment(fragment);

            int fieldModifiers = fieldDeclaration.getModifiers();
            if ((fieldModifiers & Modifier.PUBLIC) != 0)
                fieldObject.setAccess(Access.PUBLIC);
            else if ((fieldModifiers & Modifier.PROTECTED) != 0)
                fieldObject.setAccess(Access.PROTECTED);
            else if ((fieldModifiers & Modifier.PRIVATE) != 0)
                fieldObject.setAccess(Access.PRIVATE);
            else
                fieldObject.setAccess(Access.NONE);

            if ((fieldModifiers & Modifier.STATIC) != 0)
                fieldObject.setStatic(true);

            classObject.addField(fieldObject);
        }
    }

    private void processMethodDeclaration(final ClassObject classObject, MethodDeclaration methodDeclaration) {
        String methodName = methodDeclaration.getName().getIdentifier();
        final ConstructorObject constructorObject = new ConstructorObject();
        constructorObject.setMethodDeclaration(methodDeclaration);
        constructorObject.setName(methodName);
        constructorObject.setClassName(classObject.getName());
        int methodDeclarationStartPosition = methodDeclaration.getStartPosition();
        int methodDeclarationEndPosition = methodDeclarationStartPosition + methodDeclaration.getLength();

        int methodModifiers = methodDeclaration.getModifiers();
        if ((methodModifiers & Modifier.PUBLIC) != 0)
            constructorObject.setAccess(Access.PUBLIC);
        else if ((methodModifiers & Modifier.PROTECTED) != 0)
            constructorObject.setAccess(Access.PROTECTED);
        else if ((methodModifiers & Modifier.PRIVATE) != 0)
            constructorObject.setAccess(Access.PRIVATE);
        else
            constructorObject.setAccess(Access.NONE);

        List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
        for (SingleVariableDeclaration parameter : parameters) {
            Type parameterType = parameter.getType();
            ITypeBinding binding = parameterType.resolveBinding();
            String qualifiedName = binding.getQualifiedName();
            TypeObject typeObject = TypeObject.extractTypeObject(qualifiedName);
            typeObject.setArrayDimension(typeObject.getArrayDimension() + parameter.getExtraDimensions());
            if (parameter.isVarargs()) {
                typeObject.setArrayDimension(1);
            }
            ParameterObject parameterObject = new ParameterObject(typeObject, parameter.getName().getIdentifier(), parameter.isVarargs());
            parameterObject.setSingleVariableDeclaration(parameter);
            constructorObject.addParameter(parameterObject);
        }

        Block methodBody = methodDeclaration.getBody();
        if (methodBody != null) {
            MethodBodyObject methodBodyObject = new MethodBodyObject(methodBody);
            constructorObject.setMethodBody(methodBodyObject);
        }

        if (methodDeclaration.isConstructor()) {
            classObject.addConstructor(constructorObject);
        } else {
            MethodObject methodObject = new MethodObject(constructorObject);
            List<IExtendedModifier> extendedModifiers = methodDeclaration.modifiers();
            for (IExtendedModifier extendedModifier : extendedModifiers) {
                if (extendedModifier.isAnnotation()) {
                    Annotation annotation = (Annotation) extendedModifier;
                    if (annotation.getTypeName().getFullyQualifiedName().equals("Test")) {
                        methodObject.setTestAnnotation(true);
                        break;
                    }
                }
            }
            Type returnType = methodDeclaration.getReturnType2();
            ITypeBinding binding = returnType.resolveBinding();
            String qualifiedName = binding.getQualifiedName();
            TypeObject typeObject = TypeObject.extractTypeObject(qualifiedName);
            methodObject.setReturnType(typeObject);

            if ((methodModifiers & Modifier.ABSTRACT) != 0)
                methodObject.setAbstract(true);
            if ((methodModifiers & Modifier.STATIC) != 0)
                methodObject.setStatic(true);
            if ((methodModifiers & Modifier.SYNCHRONIZED) != 0)
                methodObject.setSynchronized(true);
            if ((methodModifiers & Modifier.NATIVE) != 0)
                methodObject.setNative(true);

            classObject.addMethod(methodObject);
        }
    }

    public static SystemObject getSystemObject() {
        return systemObject;
    }

    public static AST getAST() {
        if (systemObject.getClassNumber() > 0) {
            return systemObject.getClassObject(0).getAbstractTypeDeclaration().getAST();
        }
        return null;
    }

    public static String getFileContents(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return FileUtils.readFileToString(file, Charset.defaultCharset());
        }
        return null;
    }

    public static CompilationUnit createCompilationUnit(String filePath, String[] sources, String[] classpathEntries) throws IOException {
        String fileContents = getFileContents(filePath);

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(fileContents.toCharArray());
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);
        parser.setUnitName("CompilationUnit");

        String[] encoding = new String[sources.length];
        for (int i = 0; i < sources.length; i++)
            encoding[i] = "UTF-8";

        parser.setEnvironment(classpathEntries, sources, encoding, true);

        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        return unit;
    }


    public static List<File> getAllFilesInDirectory(String directory, String[] extensions) throws IOException {
        File file = new File(directory);
        if (file.exists() && file.isDirectory()) {
            return (List<File>) FileUtils.listFiles(file, extensions, true);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {

        String folderPath = "/Users/Ferooz/Git_Src/jOOQ";
//        String folderPath = "/Users/Ferooz/Downloads/crawler4j-master";

        List<File> javaFiles = getAllFilesInDirectory(folderPath, new String[]{"java"});

        System.out.println(javaFiles.size());

        ASTReader astReader = new ASTReader();

        int i = 1;
        for (File file2 : javaFiles) {

//            File file2 = new File("/Users/Ferooz/Downloads/crawler4j-master/src/main/java/edu/uci/ics/crawler4j/crawler/Page.java");
            CompilationUnit compilationUnit = createCompilationUnit(file2.getAbsolutePath(), new String[]{folderPath}, null);
            CompilationUnitCache.compilationUnitList.add(compilationUnit);
            ASTInformationGenerator.setCurrentCompilationUnit(compilationUnit);
            List<ClassObject> classObjects = astReader.parseAST(compilationUnit);

            System.out.println(i++ + "   " + file2.getAbsolutePath());
            for (ClassObject classObject : classObjects) {
                for (MethodObject methodObject : classObject.getMethodList()) {
                    CFG cfg = new CFG(methodObject);
//                    System.out.println(cfg.getEdges());
                }
            }

        }

    }
}