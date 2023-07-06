package de.hsrm.cs.wwwvs.hamster.server;

/**
 * Simple command-line interface for the hamsterlib
 * 
 * @author hinkel
 */

public class HamsterServerCommandLine {

	private static int printRtfm() {
		System.out.println("Usage: java -jar hamsterServer.jar {<Option>} <param1> {<param2>}");
		System.out.println("Function: Hamster management server");
		System.out.println("Options:");
		System.out.println("     -p {<port>}		- port to run the server");
		System.out.println("     -h {<IP address>}	- IP address to run the server on (default: 127.0.0.1)");
		return 2;
	}

	/**
	 * The main command-line interface,
	 * TODO add your code here
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String hostName = "127.0.0.1";
		int port = 8880;

		if (args.length == 0) {
			System.out.println("No arguments.");
			System.exit(printRtfm());
		}

		for (int i = 0; i < args.length; i+= 2) {
			switch (args[i]) {
				case "-p":
					port = Integer.parseInt(args[i+1]);
					break;
				case "-h":
					hostName = args[i+1];
					break;
				default:
					System.out.println("Unknown commands.");
					System.exit(printRtfm());
			}
		}
		try {
			HamsterRPCServer hamsterServer = new HamsterRPCServer(hostName, port);
			System.exit(printRtfm());
		}
		catch (Exception ex) {
			System.err.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

}