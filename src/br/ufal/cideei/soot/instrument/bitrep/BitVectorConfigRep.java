package br.ufal.cideei.soot.instrument.bitrep;

import java.util.Collection;

import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;
import org.apache.commons.lang.builder.HashCodeBuilder;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;
import cern.colt.bitvector.BitVector;

public class BitVectorConfigRep implements ILazyConfigRep {

	private final BitVector bitVector;
	private final UnmodifiableBidiMap atoms;
	private final int hashCode;

	public BitVectorConfigRep(int size, UnmodifiableBidiMap atoms) {
		this.bitVector = new BitVector(size);
		this.atoms = atoms;
		this.hashCode = new HashCodeBuilder(17, 31).append(bitVector).toHashCode();
	}

	private BitVectorConfigRep(UnmodifiableBidiMap atoms, BitVector bitVector) {
		this.atoms = atoms;
		this.bitVector = bitVector;
		this.hashCode = new HashCodeBuilder(17, 31).append(bitVector).toHashCode();
	}

	public static BitVectorConfigRep localConfigurations(int highestId, UnmodifiableBidiMap atoms) {
		BitVectorConfigRep bvcr = new BitVectorConfigRep(highestId, atoms);
		bvcr.bitVector.not();
		return bvcr;
	}

	@Override
	public Collection<IConfigRep> belongsToConfigurations(IFeatureRep rep) {
		return null;
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep featureRep) {
		if (featureRep instanceof BitVectorFeatureRep) {
			BitVectorFeatureRep bvRep = (BitVectorFeatureRep) featureRep;
			BitVector bitVector = bvRep.getBitVector();

			BitVector cloneForLeft = this.bitVector.copy();
			cloneForLeft.and(bitVector);

			BitVector cloneForRight = bitVector.copy();
			cloneForRight.not();
			cloneForRight.and(this.bitVector);

			return new Pair<ILazyConfigRep, ILazyConfigRep>(new BitVectorConfigRep(atoms, cloneForLeft), new BitVectorConfigRep(atoms, cloneForRight));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs) {
		return null;
	}

	@Override
	public boolean belongsToConfiguration(IFeatureRep rep) {
		return false;
	}

	@Override
	public int size() {
		return bitVector.cardinality();
	}

	@Override
	public String toString() {
		return this.bitVector.toString();
	}

	public BitVectorConfigRep intersection(ILazyConfigRep aOther) {
		if (aOther instanceof BitVectorConfigRep) {
			BitVectorConfigRep other = (BitVectorConfigRep) aOther;
			BitVector copy = other.bitVector.copy();
			copy.and(this.bitVector);
			return new BitVectorConfigRep(this.atoms, copy);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof BitVectorConfigRep))
			return false;
		BitVectorConfigRep that = (BitVectorConfigRep) o;
		return this.bitVector.equals(that.bitVector);
	}
	

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public BitVectorConfigRep union(ILazyConfigRep aOther) {
		if (aOther instanceof BitVectorConfigRep) {
			BitVectorConfigRep other = (BitVectorConfigRep) aOther;
			BitVector copy = other.bitVector.copy();
			copy.or(this.bitVector);
			return new BitVectorConfigRep(this.atoms, copy);
		} else {
			throw new IllegalArgumentException();
		}
	}
}