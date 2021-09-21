/**
 * Application for the NR Code Challenge
 * 30 Jun 2021 19:01:28
 */
package org.mones.nrcc.client;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Iterator;

import org.mones.nrcc.common.Const;

/**
 * Minimalistic client task.
 * 
 * Writes and array of strings to an application server.The array can be
 * repeated for longer runs. Some delay may be introduced between writes.
 */
public class NumberWriter implements Runnable {

	private String host = "localhost";
	private int port = Const.SERVER_PORT;
	private int delayBetweenWrites = 0;
	private int messageLimit = 0;
	private boolean replayForever = false;
	private boolean verbose = false;
	private Iterable<String> data = null;

	public NumberWriter() {
		// already initialized
	}

	public NumberWriter(String host) {
		this.host = host;
	}

	public NumberWriter(Iterable<String> data) {
		this.data = data;
	}

	public NumberWriter(String host, int port, Iterable<String> data) {
		this.host = host;
		this.port = port;
		this.data = data;
	}

	public NumberWriter(String host, Iterable<String> data) {
		this.host = host;
		this.data = data;
	}

	public void setDelayBetweeeWrites(int millis) {
		this.delayBetweenWrites = millis;
	}

	public void setReplayForever(boolean replay) {
		this.replayForever = replay;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setMessageLimit(int count) {
		this.messageLimit = count;
	}

	@Override
	public void run() {
		try {
			final Socket socket = new Socket(host, port);
			// System.err.println("Connected to " + host + ":" + port);
			final OutputStream output = socket.getOutputStream();
			final PrintWriter writer = new PrintWriter(output, true);
			if (data != null) {
				do {
					Iterator<String> iter = data.iterator();
					while (iter.hasNext()) {
						final String token =  iter.next();
						writer.println(token);
						if (delayBetweenWrites > 0) {
							if (verbose) {
								System.out.println("Sent " + token + ", waiting " + delayBetweenWrites + " ms");
							}
							Thread.sleep(delayBetweenWrites);
						} else {
							if (verbose) {
								System.out.println("Sent " + token);
							}
						}
						if (messageLimit > 0) {
							messageLimit--;
							if (messageLimit == 0) {
								break;
							}
						}
					}
				} while (replayForever || messageLimit > 0);
			}
			socket.close();
		} catch (ConnectException e) {
			System.err.println("can't connect to server on " + host + ":" + port);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}		
	}
}
