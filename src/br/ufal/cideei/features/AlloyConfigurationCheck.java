package br.ufal.cideei.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufpe.cin.dfa4spl.plverifier.alloy.AlloyFunctionWrapper;
import br.ufpe.cin.dfa4spl.plverifier.alloy.CannotFindBooleanSig;
import br.ufpe.cin.dfa4spl.plverifier.alloy.CannotFindFunc;
import br.ufpe.cin.dfa4spl.plverifier.alloy.Constants;
import br.ufpe.cin.dfa4spl.plverifier.alloy.io.CannotReadAlloyFileException;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class AlloyConfigurationCheck implements FeatureSetChecker {

	private A4Reporter rep;
	private CompModule module;
	
	private static final String PRED_PREFIX = "<b>pred</b> this/";
	private static final String SIG_PREFIX =  "<b>sig</b> this/";
	private static final String SIG_SUFFIX =  " <i>{this/Bool}</i>";
	
	public AlloyConfigurationCheck(String filePath) throws CannotReadAlloyFileException {
		rep = new A4Reporter() {
			// For example, here we choose to display each "warning" by printing it to System.out
			@Override
			public void warning(ErrorWarning msg) {
				System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
				System.out.flush();
			}
		};
		
		try {
			module = CompUtil.parseEverything_fromFile(rep, null, filePath);
		} catch (Err e) {
			throw new CannotReadAlloyFileException("File: " + filePath, e);
		}
	}

	private Expr makeAND(Func func, List<Expr> sigNames) {
		List<Expr> args = new ArrayList<Expr>();
		args.add(sigNames.get(0));

		if (sigNames.size() == 1) {
			return ExprCall.make(Pos.UNKNOWN, Pos.UNKNOWN, func, args, 0);
		} else {
			Expr expressionCall = ExprCall.make(Pos.UNKNOWN, Pos.UNKNOWN, func, args, 0);
			sigNames.remove(0);
			return ExprBinary.Op.AND.make(Pos.UNKNOWN, Pos.UNKNOWN, expressionCall, makeAND(func, sigNames));
		}
	}

	private Expr makeAND(List<AlloyFunctionWrapper> functions) {
		List<Expr> args = new ArrayList<Expr>();
		args.add(functions.get(0).getSig());

		if (functions.size() == 1) {
			return ExprCall.make(Pos.UNKNOWN, Pos.UNKNOWN, functions.get(0).getFunc(), args, 0);
		} else {
			Expr expressionCall = ExprCall.make(Pos.UNKNOWN, Pos.UNKNOWN, functions.get(0).getFunc(), args, 0);
			functions.remove(0);
			return ExprBinary.Op.AND.make(Pos.UNKNOWN, Pos.UNKNOWN, expressionCall, makeAND(functions));
		}
	}
	
	private void setFuncBody(Func func, Expr newBody) {
		try {
			func.setBody(newBody);
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	private Func getFunc(String funcName) throws CannotFindFunc {
		SafeList<Func> allFunc = module.getAllFunc();
		Func resultFunc = null;
		
		for (Func func : allFunc) {
			if (func.getDescription().equals(AlloyConfigurationCheck.PRED_PREFIX + funcName)) {
				resultFunc = func;
			}
		}
		
		if (resultFunc == null) {
			throw new CannotFindFunc("Func name: " + funcName);
		}
		
		return resultFunc;
	}

	private Sig getBooleanSig(String booleanSigName) throws CannotFindBooleanSig {
		SafeList<Sig> allSigs = module.getAllSigs();
		Sig resultSig = null;
		
		for (Sig sig : allSigs) {
			if (sig.getDescription().equals(AlloyConfigurationCheck.SIG_PREFIX + booleanSigName + AlloyConfigurationCheck.SIG_SUFFIX)) {
				resultSig = sig;
			}
		}
		
		if (resultSig == null) {
			throw new CannotFindBooleanSig("Boolean sig name: " + booleanSigName);
		}
		
		return resultSig;
	}

	private boolean executeCommand(String commandName, String type) {
		boolean result = false;
		
		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.SAT4J;
		
		for (Command command : module.getAllCommands()) {
			// Execute the command
			A4Solution ans = null;
			try {
				ans = TranslateAlloyToKodkod.execute_command(rep, module.getAllReachableSigs(), command, options);
			} catch (Err e) {
				e.printStackTrace();
			}
			
			// If satisfiable...
			if (ans.satisfiable()) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	private boolean isValid(Set<String> trueFeatures, Set<String> falseFeatures) throws CannotFindFunc, CannotFindBooleanSig {
		
		List<AlloyFunctionWrapper> functions = new ArrayList<AlloyFunctionWrapper>();
		
		Func isTrueFunc = getFunc("isTrue");
		Func isFalseFunc = getFunc("isFalse");
		
		for (String feature : trueFeatures) {
			functions.add(new AlloyFunctionWrapper(isTrueFunc, getBooleanSig(feature)));			
		}
		
		for (String feature : falseFeatures) {
			functions.add(new AlloyFunctionWrapper(isFalseFunc, getBooleanSig(feature)));			
		}
		
		Expr andExpression = makeAND(functions);

		System.out.println(andExpression);

		Func testFunc = getFunc("testConfiguration");
		
		setFuncBody(testFunc, andExpression);
		
		System.out.println(testFunc.getBody());
		
		boolean result = executeCommand("verify", Constants.RUN_COMMAND);
		
		if (result) {
			System.out.println("VALID!");
		} else {
			System.out.println("INVALID!");
		}
		
		return result;
	}

	@Override
	public boolean check(Set<String> trueSet, Set<String> falseSet) {
		if (trueSet.size() == 0)
			return true;
		try {
			return isValid(trueSet, falseSet);
		} catch (CannotFindFunc e) {
			e.printStackTrace();
		} catch (CannotFindBooleanSig e) {
			e.printStackTrace();
		}
		return false;
	}

}