package de.hsrm.cs.wwwvs.hamster.tests;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import de.hsrm.cs.wwwvs.hamster.tests.client.HamsterClient;
import de.hsrm.cs.wwwvs.hamster.tests.suite.TestResilience;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import de.hsrm.cs.wwwvs.hamster.tests.suite.HamsterTestSuite;

public class ResilienceRunner {

    private static void rtfm() {
        System.out.println("Usage:");
        System.out.println("java -jar HamsterTestRunner.jar {option}");
        System.out.println("\t\t-C [pathToHamsterClient]\t\t Path to the hamster_client executable. Default: " + HamsterClient.getPathToHamsterClient());
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
                continue;
            }
            if (args[i].trim().matches("-F")) {
                if (args.length < i + 2) {
                    rtfm();
                    return;
                }
                String path = args[i+1];
                // validate path?
                HamsterTestDataStore.getInstance().setHamsterFileName(path);
                continue;
            }
            if (args[i].trim().matches("-H")) {
                if (args.length < i + 2) {
                    rtfm();
                    return;
                }
                String path = args[i+1];
                // validate path?
                HamsterTestDataStore.getInstance().setPathToHamsterExe(path);
                continue;
            }
            if (args[i].trim().matches("-T")) {
                int timeout = Integer.parseInt(args[i+1]);
                HamsterTestDataStore.getInstance().testcaseTimeoutms = timeout;
                continue;
            }
            if (args[i].trim().matches("-C")) {
                String path = args[i+1];
                HamsterClient.setPathToHamsterClient(path);
            }
        }

        var test = new TestResilience();
        try {
            test.setUp();
            test.feedingHamsterCausesSickness();
            System.out.println();
            System.out.println("Found " + test.getErrors() + " problems");
        } catch (Exception ex) {
            System.out.println("Error executing tests: " + ex.getMessage());
        } finally {
            test.tearDown();
        }
    }

}
