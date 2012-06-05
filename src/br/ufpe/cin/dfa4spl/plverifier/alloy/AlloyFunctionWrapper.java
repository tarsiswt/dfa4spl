package br.ufpe.cin.dfa4spl.plverifier.alloy;

import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class AlloyFunctionWrapper {

	private Func func;
	private Sig sig;

	public AlloyFunctionWrapper(Func func, Sig sig) {
		super();
		this.func = func;
		this.sig = sig;
	}

	public Func getFunc() {
		return func;
	}
	
	public Sig getSig() {
		return sig;
	}
	
}