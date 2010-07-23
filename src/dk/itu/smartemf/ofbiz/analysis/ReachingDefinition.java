package dk.itu.smartemf.ofbiz.analysis;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class ReachingDefinition {
	ReachingDefinitionAnalysis analysis;
	public ReachingDefinition(MethodDeclaration method) {
		ControlFlowGraph cfg = new ControlFlowGraph(method);
		analysis = new ReachingDefinitionAnalysis(cfg);
		analysis.computeFixPoint();
	}
	
	public Set<Pair<String,ASTNode>> getReachingDefsAt(Statement stmt){
		return analysis.getInSet(stmt);
	}
}
