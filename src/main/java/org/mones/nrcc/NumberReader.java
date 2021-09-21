/**
 * Application for the NR Code Challenge
 * 30 Jun 2021 14:25:49
 */
package org.mones.nrcc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * Reader task.
 * 
 * Reads strings from socket matching {@link #DATA_REGEX}, and feeds the
 * DataKeeper instance with them. If the {@link #BYE_KEYWORD} is received
 * the application is terminated. If the string doesn't match the reader
 * finishes.
 */
public class NumberReader extends Thread {

	public final static String BYE_KEYWORD = "terminate"; // req-9
	private final static String DATA_REGEX = "\\d{9}"; // req-2, req-3
	private final static Pattern DATA_PATTERN = Pattern.compile(DATA_REGEX);

	private Socket socket;
	private InputStream input;
	private BufferedReader reader;
	private int clientId;

	// some getter for easier testing
	public int getClientId() {
		return clientId;
	}

	public boolean isClosed() {
		return (socket != null? socket.isClosed(): true);
	}

	/**
	 * Constructor for the number reader.
	 * 
	 * @param socket Client socket to read from.
	 * @param clientId Our identity as a distinguished reader.
	 * @throws IOException In case of problems getting socket input stream.
	 */
	public NumberReader(Socket socket, int clientId) throws IOException {
		super();
		this.socket = socket;
		this.clientId = clientId;
		this.input = this.socket.getInputStream();
		this.reader = new BufferedReader(new InputStreamReader(this.input));
	}

	/**
	 * Close all resources, disconnects client socket and ends thread.
	 * 
	 * @throws IOException If thread was blocked upon interruption.
	 */
	public void disconnect() throws IOException {
		if (socket != null) {
			// no more input please
			socket.shutdownInput();
		}
		// close in reverse opening order
		if (reader != null) {
			reader.close();
		}
		if (input != null) {
			input.close();
		}
		if (socket != null) {
			socket.close();
		}
		this.interrupt();
	}

	@Override
	public void run() {
		try {
			final DataKeeper keeper = DataKeeper.getInstance();
			final Application application = Application.getInstance();
			String token = reader.readLine();
			while (token != null) {
				if (BYE_KEYWORD.equals(token)) {
					application.terminate();; // will call disconnect() for us
					return;
				}
				if (DATA_PATTERN.matcher(token).matches()) {
					keeper.receive(token);
				} else {
					break; // req-7
				}
				token = reader.readLine();
			}
			application.endClient(clientId);
			disconnect();
		} catch (Exception e) {
			// ignore exceptions in readLine when closing the socket from disconnect()
		}
	}
}