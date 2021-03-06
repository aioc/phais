package com.ausinformatics.phais.utils;

public class Pair<F, S> {
	public F first;
	public S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}


	@Override
	public int hashCode() {
		return (first.hashCode() << 16) ^ second.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Pair<?, ?>) {
			return ((Pair<?, ?>) o).first.equals(first) && ((Pair<?, ?>) o).second.equals(second);
		} else {
			return false;
		}
	}
}
