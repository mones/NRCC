/**
 * Application for the NR Code Challenge
 * 30 Jun 2021 19:11:10
 */
package org.mones.nrcc.client;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mones.nrcc.common.Const;

/**
 * Client main class.
 */
public class Client {

	private static String host = "localhost";
	private static int port = Const.SERVER_PORT;
	private static Iterable<String> data = null;
	private static int count = 0;
	private static int interval = 0;

	/**
	 * Parses command line options.
	 * Exits on error or invalid options.
	 * 
	 * @param args Arguments received by main()
	 */
	public static void parseArgs(String[] args) {
		Options options = new Options();
		Option help = Option.builder("h")
				.longOpt("help").desc("shows command line help").build();
		Option server = Option.builder("s")
				.longOpt("server").hasArg().argName("address")
				.desc("host name or IPv4 address of server").build();
		Option port = Option.builder("p")
				.longOpt("port").hasArg().argName("number")
				.desc("port on server").build();
		Option data = Option.builder("d")
				.longOpt("data").hasArgs().argName("number")
				.desc("data to sent to server").build();
		Option count = Option.builder("c")
				.longOpt("count").hasArgs().argName("number")
				.desc("number of total messages to send").build();
		Option interval = Option.builder("i")
				.longOpt("interval").hasArgs().argName("millis")
				.desc("wait period between messages").build();
		Option range = Option.builder("r")
				.longOpt("range").hasArgs().argName("start:end")
				.desc("generate data with the given range").build();
		options.addOption(help);
		options.addOption(server);
		options.addOption(port);
		options.addOption(data);
		options.addOption(count);
		options.addOption(interval);
		options.addOption(range);
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
			formatter.printHelp("java -jar nrcc-<version>-client.jar [options]", options);
			System.exit(0);
		}
		if (cl.hasOption('d')) {
			Client.data = Arrays.asList(cl.getOptionValues('d'));
		}
		if (cl.hasOption('r')) {
			if (cl.hasOption('d')) {
				System.err.println("Only -d or -r are allowed, try -h for help");
				System.exit(1);
			}
			String sr = cl.getOptionValue('r');
			try {
				final String[] se = sr.split(":");
				final int start = Integer.parseInt(se[0]);
				final int end = Integer.parseInt(se[1]);
				Client.data = new RangeGenerator(start, end);
			} catch (Exception e) {
				System.err.println("Invalid range, try -h for help");
				System.exit(1);
			}
		}
		if (Client.data == null) { // neither data nor range options
			System.err.println("Missing required option: -d or -r, try -h for help");
			System.exit(1);
		}
		if (cl.hasOption('s')) {
			Client.host = cl.getOptionValue('s');
		}
		if (cl.hasOption('p')) {
			Client.port = Integer.parseInt(cl.getOptionValue('p'));
			if (Client.port < 1 || Client.port > 65535) {
				System.err.println("Invalid port number, try -h for help");
				System.exit(1);
			}
		}
		if (cl.hasOption('c')) {
			Client.count  = Integer.parseInt(cl.getOptionValue('c'));
			if (Client.count < 1) {
				System.err.println("Invalid count, try -h for help");
				System.exit(1);
			}
		}
		if (cl.hasOption('i')) {
			Client.interval  = Integer.parseInt(cl.getOptionValue('i'));
			if (Client.interval < 1) {
				System.err.println("Invalid interval, try -h for help");
				System.exit(1);
			}
		}
	}

	/**
	 * Client entry point.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		try {
			Client.parseArgs(args);
			NumberWriter client = new NumberWriter(host, port, data);
			if (Client.interval > 0) {
				client.setDelayBetweeeWrites(Client.interval);
			}
			if (Client.count > 0) {
				client.setMessageLimit(Client.count);
			}
			client.run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
