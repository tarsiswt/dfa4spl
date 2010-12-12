package br.ufal.cideei.soot.instrument;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import br.ufal.cideei.features.IFeatureExtracter;

public class CachedLineNumberMapper {
	private LineNumberColorMapper colorMapper = null;

	// Caching key
	private ASTNode visitee = null;

	// Cached target
	private Map<Integer, Set<String>> cachedResult = null;

	public Map<Integer, Set<String>> makeAccept(CompilationUnit compilationUnit, IFile file, IFeatureExtracter extracter, ASTNode node) {
		// itialized and cached: HIT
		if (visitee != null && node.equals(visitee)) {
			return cachedResult;
		} else {
			//MISS
//			System.out.println("CachedLineNumberMapper: miss");
			colorMapper = new LineNumberColorMapper(compilationUnit, file, extracter);
			visitee = node;
			visitee.accept(colorMapper);
			return this.cachedResult = colorMapper.getLineToColors();
		}
	}
}
