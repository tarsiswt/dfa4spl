package br.ufal.cideei.soot.instrument.bitrep;

import java.util.Set;

import org.apache.commons.collections.BidiMap;
//import org.apache.commons.lang.builder.HashCodeBuilder;

import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

import cern.colt.bitvector.BitVector;

//XXX: implement equals/hashCode contract.
public class BitVectorFeatureRep implements IFeatureRep, Cloneable {
	private Set<String> features;
	private BidiMap atoms;
	private int id;
//	private int hashCode;
	private BitVector bits;

	public BitVectorFeatureRep(Set<String> features, BidiMap originalFeatureIds) {
		this.features = features;
		int accumulator = 0;
		for (String element : this.features) {
			Integer featId = (Integer) originalFeatureIds.get(element);
			if (featId != null) {
				accumulator += featId;
			}
		}
		this.id = accumulator;
//		this.hashCode = new HashCodeBuilder(17, 31).append(id).append(bits).toHashCode();
		this.atoms = originalFeatureIds;
	}

	private BitVectorFeatureRep(Set<String> features, BidiMap atoms, BitVector bits, int id) {
		this.features = features;
		this.atoms = atoms;
		this.id = id;
//		this.hashCode = new HashCodeBuilder(17, 31).append(id).toHashCode();
		this.bits = (BitVector) bits.clone();
	}

	public IFeatureRep clone() {
		return new BitVectorFeatureRep(this.features, this.atoms, this.bits, this.id);
	}

	public void generateBitVector(int highestId) {
		this.bits = new BitVector(highestId);
		for (int index = 0; index < highestId; index++) {
			if ((this.id & index) == this.id) {
				bits.set(index);
			}
		}
	}

	@Override
	public IFeatureRep addAll(IFeatureRep rep) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean belongsToConfiguration(IConfigRep config) {
		return config.belongsToConfiguration(this);
	}

	@Override
	public Set<String> getFeatures() {
		return features;
	}

	@Override
	public int size() {
		return features.size();
	}

	@Override
	public String toString() {
		return this.bits.toString();
	}

	public BitVector getBitVector() {
		return this.bits;
	}
}