package br.ufal.cideei.util;

import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import soot.SootMethod;
import soot.Type;
import soot.jimple.Jimple;

// TODO: Auto-generated Javadoc
/**
 * The Class MethodDeclarationSootMethodBridge.
 */
public class MethodDeclarationSootMethodBridge {
	
	/** The method declaration. */
	private MethodDeclaration methodDeclaration;
	
	/**
	 * Instantiates a new method declaration soot method bridge.
	 */
	private MethodDeclarationSootMethodBridge(){}

	/**
	 * Instantiates a new method declaration soot method bridge.
	 *
	 * @param methodDeclaration the method declaration
	 */
	public MethodDeclarationSootMethodBridge(MethodDeclaration methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}
	
	/**
	 * Gets the soot method sub signature.
	 *
	 * @return the soot method sub signature
	 */
	public String getSootMethodSubSignature(){
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] argumentsBinding = methodBinding.getParameterTypes();
		ITypeBinding returnBinding = methodBinding.getReturnType();
		
		StringBuilder stringMethodBuilder = new StringBuilder();
		
		stringMethodBuilder.append(returnBinding.getQualifiedName());
		stringMethodBuilder.append(" ");
		if (methodDeclaration.isConstructor()){
			stringMethodBuilder.append("<init>");
		} else {
			stringMethodBuilder.append(methodBinding.getName());
		}
		stringMethodBuilder.append("(");
		
		for(int index = 0; index < argumentsBinding.length ; index++){
			stringMethodBuilder.append(argumentsBinding[index].getQualifiedName());
			if (!(index == argumentsBinding.length -1)){
				stringMethodBuilder.append(",");
			}
		}

		stringMethodBuilder.append(")");
//		System.out.println(stringMethodBuilder.toString());
		return stringMethodBuilder.toString();
	}
	
	/**
	 * Gets the soot method signature.
	 *
	 * @return the soot method signature
	 */
	public String getSootMethodSignature(){
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] argumentsBinding = methodBinding.getParameterTypes();
		ITypeBinding declaringTypeBinding = methodBinding.getDeclaringClass();
		ITypeBinding returnBinding = methodBinding.getReturnType();
		
		StringBuilder stringMethodBuilder = new StringBuilder("<");
		stringMethodBuilder.append(declaringTypeBinding.getQualifiedName());
		stringMethodBuilder.append(": ");
		stringMethodBuilder.append(returnBinding.getQualifiedName());
		stringMethodBuilder.append(" ");
		stringMethodBuilder.append(methodBinding.getName());
		stringMethodBuilder.append("(");
		
		for(int index = 0; index < argumentsBinding.length ; index++){
			stringMethodBuilder.append(argumentsBinding[index].getQualifiedName());
			if (!(index == argumentsBinding.length -1)){
				stringMethodBuilder.append(",");
			}
		}

		stringMethodBuilder.append(")>");
		return stringMethodBuilder.toString();
	}

}
