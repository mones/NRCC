/**
 * Application for the NR Code Challenge
 * 30 Jun 2021 13:53:12
 */
package org.mones.nrcc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mones.nrcc.common.Const;

/**
 * Server main class.
 * 
 * Performs all tasks detailed in the challenge.
 */
public final class Application implements Runnable {

	private final static int MAX_CLIENTS = 5; // req-1
	private final static int REPORT_SECONDS = 10; // req-8

	private static Boolean running = true;

	// threads control
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private int runningClients = 0;
	private int clientId = 0;
	private Map<Integer, NumberReader> clientThreads = new Hashtable<Integer, NumberReader>(MAX_CLIENTS);

	// listening socket
	private ServerSocket serverSocket;

	// numbers log file name
	private String logName = Const.LOG_NAME;

	// some getters for easier testing
	public int getRunningClients() {
		synchronized (running) {
			return runningClients;
		}
	}

	public int getClientThreadsSize() {
		synchronized (running) {
			return clientThreads.size();
		}
	}

	/**
	 * Closes server socket and sets running to false. 
	 */
	public void terminate() {
		synchronized (running) {
			running = false;
			// finish listening socket to avoid any other connection while terminating
			try {
				serverSocket.close();
			} catch (IOException e) {
				// ignore the SocketException if blocked in accept()
			}
		}
	}

	/**
	 * Cleans up running threads and saves the numbers log.
	 */
	private void cleanSave() {
		// finish current clients
		disconnectAllClients();
		// write final log
		DataKeeper.getInstance().save();
		// finish keeper periodic task
		try {
			executor.shutdown();
			if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				// given half second to finish gracefully, time to exit now
				System.exit(0);
			}
		} catch (InterruptedException e) {
			// ignore this one too, still terminating ^_^
		}
	}

	/**
	 * Closes the sockets of all client threads currently running.
	 */
	public void disconnectAllClients() {
		synchronized (running) {
			Iterator<NumberReader> clientIterator = clientThreads.values().iterator();
			while (clientIterator.hasNext()) {
				try {
					NumberReader client = clientIterator.next();
					client.disconnect();
				} catch (ConcurrentModificationException e) {
					// some client removed himself before us
				} catch (Exception e) {
					// ignore the exceptions, we're terminating
				}
			}
			// reset map
			clientThreads.clear();
			runningClients = 0;
		}
	}

	/**
	 * Create the given filename empty and save it for later.
	 * 
	 * @param filename File to empty.
	 * @throws IOException 
	 */
	private void initLog(String filename) throws IOException {
		File file = new File(filename);
		(new FileWriter(file)).close();
		logName = filename;
	}

	/**
	 * Creates and starts a new client reading numbers from the socket.
	 * 
	 * @param socket client socket to read the numbers from.
	 * @throws IOException if the new client can't be created.
	 */
	public void newClient(Socket socket) throws IOException {
		synchronized (running) {
			if (runningClients < MAX_CLIENTS) {
				runningClients++;
				clientId++;
				final NumberReader thread = new NumberReader(socket, clientId);
				clientThreads.put(clientId, thread);
				thread.start();
				// System.err.println("new client from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
			} else {
				// System.err.println("rejecting client from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				// can't accept more clients, close connection
				try {
					socket.close();
				} catch (IOException e) {
					// ignore exception closing client sockets we'll never attend 
				}
			}
		}
	}

	/**
	 * Removes the client from our client map.
	 * 
	 * @param clientId Client identifier.
	 */
	public void endClient(int clientId) {
		synchronized (running) {
			runningClients--;
			clientThreads.remove(clientId);
		}
	}

	private Application() { }
	
	private static Application instance = new Application(); 

	public static Application getInstance() {
		return instance;
	}

	@Override
	public void run() {
		try {
			final DataKeeper keeper = DataKeeper.getInstance();
			keeper.setLogName(logName);
			executor.scheduleAtFixedRate(
				keeper, REPORT_SECONDS, REPORT_SECONDS, TimeUnit.SECONDS); // req-8
			serverSocket = new ServerSocket(Const.SERVER_PORT);
			while (Application.running) {
				Socket socket = serverSocket.accept();
				newClient(socket);
			}
		} catch (SocketException e) {
			// no need to print "SocketException: Interrupted function call: accept failed"
		} catch (RejectedExecutionException e) {
			System.err.println("can't schedule the data keeper task");
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			cleanSave();
		}
	}

	/**
	 * Parses command line options.
	 * Exits on error or invalid options.
	 * 
	 * @param args Arguments received by main()
	 */
	public static String parseArgs(String[] args) {
		Options options = new Options();
		Option help = Option.builder("h")
				.longOpt("help").desc("shows command line help").build();
		Option directory = Option.builder("d")
				.longOpt("dir").hasArg().argName("directory")
				.desc("directory for numbers log file").build();
		options.addOption(help);
		options.addOption(directory);
		CommandLineParser parser = new DefaultParser();
		CommandLine cl = null;
		try {
			cl = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error parsing options, try -h for help");
			System.exit(1);
		}
		if (cl.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar nrcc-<version>-all.jar [options]", options);
			System.exit(0);
		}
		if (cl.hasOption('d')) {
			File dir = new File(cl.getOptionValue('d'));
			if (!dir.isDirectory()) {
				System.err.println("Invalid argument, try -h for help");
				System.exit(1);
			}
			File fullName = new File(dir, Const.LOG_NAME);
			return fullName.getPath();
		}
		return Const.LOG_NAME;
	}

	/**
	 * Server jar entry point.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		try {
			final String logFileName = Application.parseArgs(args);
			final Application server = Application.getInstance();
			server.initLog(logFileName); // req-4
			server.run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}