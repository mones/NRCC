/**
 * Application for the NR Code Challenge
 * 2 Jul 2021 19:34:38
 */
package org.mones.nrcc.client;

import java.util.Iterator;

/**
 * Iterable for simple numeric ranges.
 */
public final class RangeGenerator implements Iterable<String> {

	private int start;
	private int end;

	public RangeGenerator(int start, int end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public Iterator<String> iterator() {
		return new RangeIterator(this.start, this.end);
	}
}