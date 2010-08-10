package br.ufal.cideei.util.graph;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import soot.Unit;
import soot.tagkit.SourceLnPosTag;

// TODO: Auto-generated Javadoc
/**
 * The Class VertexNameFilterProvider is a utility class used to name the
 * vertexes on a graph when transforming it to another serializes
 * representation, like a .DOT file.
 * 
 * @param <V>
 *            the value type
 */
public class VertexLineNameProvider<V extends Unit> implements VertexNameProvider<V> {

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/**
	 * Instantiates a new vertex name filter provider.
	 * 
	 * @param compilationUnit
	 *            the compilation unit
	 */
	public VertexLineNameProvider(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	/**
	 * Instantiates a new vertex name filter provider.
	 */
	private VertexLineNameProvider() {
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
			return tag.startLn()+"";
		}
		return "\"" + vertex.toString().replace("\"", "'") + "\"";
	}

}
