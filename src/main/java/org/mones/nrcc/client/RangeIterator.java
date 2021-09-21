/**
 * Application for the NR Code Challenge
 * 2 Jul 2021 20:25:09
 */
package org.mones.nrcc.client;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator of formatted numbers for simple numeric ranges.
 */
public final class RangeIterator implements Iterator<String> {

	private int start;
	private int end;
	
	public RangeIterator(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public boolean hasNext() {
		return start < end;
	}

	@Override
	public String next() {
		if (start < end) {
			return String.format("%09d", start++);
		}
		throw new NoSuchElementException("No more items in range");
	}
}