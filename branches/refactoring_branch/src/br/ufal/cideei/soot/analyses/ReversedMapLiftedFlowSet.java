/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;

public class ReversedMapLiftedFlowSet extends AbstractFlowSet {
	protected HashMap<FlowSet, IConfigRep> map;

	public HashMap<FlowSet, IConfigRep> getMapping() {
		return map;
	}

	protected ReversedMapLiftedFlowSet(Map<FlowSet, IConfigRep> map) {
		this.map = new HashMap<FlowSet, IConfigRep>(map);
	}

	public ReversedMapLiftedFlowSet(Collection<IConfigRep> configs) {
		map = new HashMap<FlowSet, IConfigRep>();
		for (IConfigRep config : configs) {
			map.put(new ArraySparseSet(), config);
		}
	}

	public ReversedMapLiftedFlowSet(IConfigRep seed) {
		map = new HashMap<FlowSet, IConfigRep>();
		map.put(new ArraySparseSet(), seed);
	}

//	public FlowSet getLattice(IConfigRep config) {
//		return this.map.get(config);
//	}

	public ReversedMapLiftedFlowSet() {
		map = new HashMap<FlowSet, IConfigRep>();
	}

	@Override
	public ReversedMapLiftedFlowSet clone() {
		Set<Entry<FlowSet, IConfigRep>> entrySet = map.entrySet();
		Map<FlowSet, IConfigRep> newMap = new HashMap<FlowSet, IConfigRep>();
		for (Entry<FlowSet, IConfigRep> entry : entrySet) {
			newMap.put(entry.getKey().clone(), entry.getValue());
		}
		return new ReversedMapLiftedFlowSet(newMap);
	}

	@Override
	public void copy(FlowSet dest) {
		ReversedMapLiftedFlowSet destLifted = (ReversedMapLiftedFlowSet) dest;
		dest.clear();
		Set<Entry<FlowSet, IConfigRep>> entrySet = map.entrySet();
		for (Entry<FlowSet, IConfigRep> entry : entrySet) {
			FlowSet key = entry.getKey();
			IConfigRep value = entry.getValue();
			destLifted.map.put(key.clone(), value);
		}
	}

//	public Set<IConfigRep> getConfigurations() {
//		return map.keySet();
//	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof ReversedMapLiftedFlowSet))
			return false;
		ReversedMapLiftedFlowSet that = (ReversedMapLiftedFlowSet) o;
		return new EqualsBuilder().append(this.map, that.map).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(this.map).toHashCode();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public void union(FlowSet aOther, FlowSet aDest) {
		ReversedMapLiftedFlowSet other = (ReversedMapLiftedFlowSet) aOther;
		ReversedMapLiftedFlowSet dest = (ReversedMapLiftedFlowSet) aDest;

		Set<Entry<FlowSet, IConfigRep>> entrySet = this.map.entrySet();
		Set<Entry<FlowSet, IConfigRep>> otherEntrySet = other.map.entrySet();

		HashMap<FlowSet, IConfigRep> destMap = new HashMap<FlowSet, IConfigRep>();

		for (Entry<FlowSet, IConfigRep> entry : entrySet) {
			for (Entry<FlowSet, IConfigRep> otherEntry : otherEntrySet) {
				ILazyConfigRep key = (ILazyConfigRep) entry.getValue();
				ILazyConfigRep otherKey = (ILazyConfigRep) otherEntry.getValue();

				ILazyConfigRep intersection = key.intersection(otherKey);
				if (intersection.size() != 0) {
					FlowSet otherFlowSet = otherEntry.getKey();
					ArraySparseSet destFlowSet = new ArraySparseSet();
					entry.getKey().union(otherFlowSet, destFlowSet);
					destMap.put(destFlowSet, intersection);
				}
			}
		}

		dest.map = destMap;
	}

	@Override
	public void intersection(FlowSet aOther, FlowSet aDest) {
		ReversedMapLiftedFlowSet other = (ReversedMapLiftedFlowSet) aOther;
		ReversedMapLiftedFlowSet dest = (ReversedMapLiftedFlowSet) aDest;

		Set<Entry<FlowSet, IConfigRep>> entrySet = this.map.entrySet();
		Set<Entry<FlowSet, IConfigRep>> otherEntrySet = other.map.entrySet();

		HashMap<FlowSet, IConfigRep> destMap = new HashMap<FlowSet, IConfigRep>();

		for (Entry<FlowSet, IConfigRep> entry : entrySet) {
			for (Entry<FlowSet, IConfigRep> otherEntry : otherEntrySet) {
				ILazyConfigRep key = (ILazyConfigRep) entry.getValue();
				ILazyConfigRep otherKey = (ILazyConfigRep) otherEntry.getValue();

				ILazyConfigRep intersection = key.intersection(otherKey);
				if (intersection.size() != 0) {
					FlowSet otherFlowSet = otherEntry.getKey();
					ArraySparseSet destFlowSet = new ArraySparseSet();
					entry.getKey().intersection(otherFlowSet, destFlowSet);
					destMap.put(destFlowSet, intersection);
				}
			}
		}

		dest.map = destMap;
	}

	public FlowSet add(FlowSet flow, IConfigRep config) {
		return (FlowSet) map.put(flow, config);
	}

	@Override
	public void add(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public List toList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
