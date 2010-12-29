package br.ufal.cideei.util.graph;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jgrapht.ext.VertexNameProvider;

import soot.Unit;
import soot.tagkit.SourceLnPosTag;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Auto-generated Javadoc
/**
 * The Class VertexNameFilterProvider is a utility class used to name the
 * vertexes on a graph when transforming it to another serializes
 * representation, like a .DOT file.
 * 
 * @param <V>
 *            the value type
 */
public class VertexNameFilterProvider<V extends Unit> implements VertexNameProvider<V> {

	/**
	 * Instantiates a new vertex name filter provider.
	 * 
	 * @param compilationUnit
	 *            the compilation unit
	 */
	public VertexNameFilterProvider(CompilationUnit compilationUnit) {
	}

	/**
	 * Instantiates a new vertex name filter provider.
	 */
	@SuppressWarnings("unused")
	private VertexNameFilterProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgrapht.ext.VertexNameProvider#getVertexName(java.lang.Object)
	 */
	@Override
	public String getVertexName(V vertex) {
		if (vertex.hasTag("SourceLnPosTag")) {
			SourceLnPosTag tag = (SourceLnPosTag) vertex.getTag("SourceLnPosTag");
			String feat;
			try {
				FeatureTag ftag = (FeatureTag) vertex.getTag("FeatureTag");
				feat = ftag.getFeatures().toString();
			} catch (Exception ex) {
				ex.printStackTrace();
				feat = "";
			}

			// String feat = "";
			return "\"" + "(" + tag.startLn() + ")" + vertex.toString().replace("\"", "'") + feat + "\"";
		}
		return "\"" + vertex.toString().replace("\"", "'") + "\"";
	}

}
