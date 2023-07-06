package de.hsrm.cs.wwwvs.hamster.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Simple command-line interface for the hamsterlib server
 * 
 * @author hinkel
 */

@SpringBootApplication
public class HamsterServerCommandLine {

	/**
	 * The main command-line interface,
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(HamsterServerCommandLine.class);
		app.run();
	}

}