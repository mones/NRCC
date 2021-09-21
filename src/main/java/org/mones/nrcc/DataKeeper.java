/**
 * Application for the NR Code Challenge
 * 30 Jun 2021 19:52:02
 */
package org.mones.nrcc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

/**
 * Reporter task and data keeper singleton.
 * 
 * Receives the numbers from the {@link NumberReader} tasks and keeps an
 * thread-safe map with them, updating the stats accordingly.
 * Prints the stats report when invoked.
 */
public final class DataKeeper implements Runnable {

	private static final int INITIAL_CAPACITY = 1024 * 1024;

	private static DataKeeper instance = new DataKeeper();

	private long lastUnique;
	private long lastDuplicates;
	private long lastSize;
	private long unique;
	private long duplicates;
	private String logName;

	private Map<String, String> numbers;

	// some getter for easier testing
	public long getLastUnique() {
		return lastUnique;
	}

	public long getLastDuplicates() {
		return lastDuplicates;
	}

	public long getUnique() {
		return unique;
	}

	public long getDuplicates() {
		return duplicates;
	}

	public Map<String, String> getNumbers() {
		return numbers;
	}

	/**
	 * Initializes stats and numbers map.
	 */
	private DataKeeper() {
		reset();
	}

	/**
	 * Sets log name for saving.
	 *
	 * @param filename The name of the numbers log.
	 */
	public void setLogName(String filename) {
		logName = filename;
	}

	/**
	 * Reset instance.
	 */
	public void reset() {
		lastSize = lastUnique = lastDuplicates = 0L;
		unique = duplicates = 0L;
		numbers = new Hashtable<String, String>(INITIAL_CAPACITY);
	}

	/**
	 * @return The single instance of this class.
	 */
	public static DataKeeper getInstance() {
		return DataKeeper.instance;
	}

	/**
	 * Receives a number from a reader task.
	 * 
	 * @param number The number to add to our numbers map.
	 */
	public void receive(String number) {
		synchronized (instance) {
			if (!numbers.containsKey(number)) {
				unique++;
				numbers.put(number, number);
			} else {
				duplicates++;
			}
		}
	}

	/**
	 * Saves captured numbers.
	 * 
	 * @throws FileNotFoundException If the file can't be created or written.
	 */
	public void save() {
		// System.err.println("saving to " + logName);
		try {
			PrintWriter writer = new PrintWriter(logName);
			for (String number: numbers.keySet()) {
				writer.println(number);
			}
			writer.close();
		} catch (Exception e) {
			System.err.println(String.format("Can't save numbers log: %s", logName));
			e.printStackTrace(System.err);
		}
	}

	private static final String REPORT_FORMAT =
			"Received %d unique numbers, %d duplicates. Unique total: %d" ; // req-8;

	/**
	 * How many unique numbers between two reporting periods are allowed
	 * without saving the numbers log again.
	 */
	private static final long CHANGE_THRESHOLD = 100;

	/**
	 * How many unique numbers since the last time we saved are allowed
	 * without saving the numbers log again.
	 */
	private static final long GROW_THRESHOLD = 100;

	/**
	 * Returns report string with current stats.
	 * 
	 * @return The formatted report string.
	 */
	public String report() {
		long u, d, t, g;
		synchronized (instance) {
			u = unique - lastUnique;
			d = duplicates - lastDuplicates;
			t = numbers.size();
			g = t - lastSize;
			lastUnique = unique;
			lastDuplicates = duplicates;
			if (u > CHANGE_THRESHOLD || g > GROW_THRESHOLD) {
				save();
				lastSize = t;
			}
		}
		return String.format(REPORT_FORMAT, u, d, t);
	}

	/**
	 * Prints the report to standard output.
	 */
	@Override
	public void run() {
		try {
			System.out.println(report()); // req-8
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}