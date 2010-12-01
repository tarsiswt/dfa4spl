package br.ufal.cideei.util;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

public class ExecutionResultWrapper<T extends Number> {
	private ArrayList<T> al;

	public ExecutionResultWrapper(int size) {
		al = new ArrayList<T>(size);
	}

	public ExecutionResultWrapper() {
		al = new ArrayList<T>();
	}

	public void add(T obj) {
		al.add(obj);
	}

	public double mean() {
		Double sum = new Double(0);
		for (T t : al) {
			sum += t.doubleValue();
		}
		return sum / al.size();
	}

	public String toString() {
		int size = al.size();
		StringBuilder builder;
		if (size == 0) {
			builder = new StringBuilder(0);
		} else {
			builder = new StringBuilder(size + size - 1);
		}
		builder.append("[");
		for (T t : al) {
			Formatter formatter = new Formatter();
			builder.append(formatter.format(Locale.FRANCE, "%10.4f", t).out());
			builder.append("\t");
		}
		builder.append("]");
		return builder.toString();
	}
}
