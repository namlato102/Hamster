package de.hsrm.cs.wwwvs.hamster.server;

import de.hsrm.cs.wwwvs.hamster.lib.HamsterException;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterLib;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterState;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.HashMap;

/**
 * Simple command-line interface for the hamsterlib server
 * 
 * @author hinkel
 */

@SpringBootApplication
public class HamsterServerCommandLine {

	private static int printRtfm() {
		System.out.println("Usage: java -jar hamsterServer.jar {<Option>} <param1> {<param2>}");
		System.out.println("Function: Hamster management server");
		System.out.println("Options:");
		System.out.println("     -p {<port>}		- port to run the server");
		System.out.println("     -h {<IP address>}	- IP address to run the server on (default: 127.0.0.1)");
		System.out.println("     -f {Feed fail prob}- probability for feeding failure (default: 0.5)");
		return 2;
	}

	/**
	 * The main command-line interface,
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String hostName = "127.0.0.1";
		int port = 9000;
		double feedFailProbability = 0.5;

		for (int i = 0; i < args.length; i+= 2) {
			switch (args[i]) {
				case "-p":
					port = Integer.parseInt(args[i+1]);
					break;
				case "-h":
					hostName = args[i+1];
					break;
				case "-f":
					feedFailProbability = Double.parseDouble(args[i+1]);
					break;
				default:
					System.exit(printRtfm());
			}
		}
		HamsterLib.setFeedFailureProbability(feedFailProbability);
		SpringApplication app = new SpringApplication(HamsterServerCommandLine.class);
		var props = new HashMap<String, Object>();
		props.put("server.port", port);
		props.put("server.address", hostName);
		app.setDefaultProperties(props);
		app.run();
	}

}