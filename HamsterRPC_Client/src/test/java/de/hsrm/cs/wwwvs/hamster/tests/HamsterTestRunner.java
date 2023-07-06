package de.hsrm.cs.wwwvs.hamster.tests;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import de.hsrm.cs.wwwvs.hamster.tests.suite.HamsterTestSuite;

public class HamsterTestRunner {

	private static void rtfm() {
		System.out.println("Usage:");
		System.out.println("java -jar HamsterTestRunner.jar {option}");
		System.out.println("\t\t-P [pathToHamsterServer]\t\t Path to the hamster_server executable. Default: " + HamsterTestDataStore.getInstance().getPathToHamsterServer());
		System.out.println("\t\t-H [pathToHamsterProgram]\t\t Path to the hamster executable. Default: " + HamsterTestDataStore.getInstance().getPathToHamsterExe());
		System.out.println("\t\t-F [hamsterFileName]\t\tPath to the storage information of the hamsterlib. Default: " + HamsterTestDataStore.getInstance().getHamsterFileName());
		System.out.println("\t\t-T [timount for testcases]\t\t Timeout for testcases im ms, Default: " + HamsterTestDataStore.getInstance().testcaseTimeoutms);
		System.out.println("\t\t-h \t\t\t This help text");
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
				
		for (int i = 0; i < args.length; i+= 2) {
			if (args[i].trim().matches("-h")) {
				rtfm();
				return;
			}
			
			if (args[i].trim().matches("-P")) {
				if (args.length < i + 2) {
					rtfm();
					return;
				}
				String path = args[i+1];
				// validate path?
				HamsterTestDataStore.getInstance().setPathToHamsterServer(path);
			}
			if (args[i].trim().matches("-F")) {
				if (args.length < i + 2) {
					rtfm();
					return;
				}
				String path = args[i+1];
				// validate path?
				HamsterTestDataStore.getInstance().setHamsterFileName(path);
			}
			if (args[i].trim().matches("-H")) {
				if (args.length < i + 2) {
					rtfm();
					return;
				}
				String path = args[i+1];
				// validate path?
				HamsterTestDataStore.getInstance().setPathToHamsterExe(path);
			}
			if (args[i].trim().matches("-T")) {
				int timeout = Integer.parseInt(args[i+1]);
				HamsterTestDataStore.getInstance().testcaseTimeoutms = timeout;
				
			}
			
			
		}		
		
		Result result = JUnitCore.runClasses(HamsterTestSuite.class);
		PrintWriter writer = new PrintWriter(System.out);
		writer.println();
		writer.println();
	
		writer.println("Test summary: ");
		writer.println("=============================================");
		writer.println();
		writer.println("\tran " + result.getRunCount() + " test cases");
		writer.println("\ttest failed " + result.getFailureCount());
		writer.println("\texecution time " + result.getRunTime()/1000 + "s");

		writer.println();
		writer.println();
		writer.println();
		
		writer.println("Fehlgeschlagene Testfälle, detailliert: ");
		writer.println("==============================================");
		writer.println();
	
		
		for (Failure failure : result.getFailures()) {
			
			String msg = new String();
			msg += failure.getTestHeader();
			
			if (failure.getMessage() != null) {
				msg += ": ";
				msg += failure.getMessage();
			} else {
				msg += " - Aufgetretende Exception - Trace: \n";
				msg += failure.getTrace();
			}
			
			writer.println(msg);
			
		}
		
		writer.close();
		
		
	}

}
