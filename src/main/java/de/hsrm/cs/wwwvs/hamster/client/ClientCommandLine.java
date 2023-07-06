package de.hsrm.cs.wwwvs.hamster.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterException;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterLib;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterState;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggerConfiguration;

import java.io.Console;


public class ClientCommandLine {

    private static int printRtfm() {
        System.out.println("Usage: hamster {<Option>} <param1> {<param2>}");
        System.out.println("Function: Hamster management");
        System.out.println("Verbs:");
        System.out.println("     list {<owner>} [-p]                   - show current list of hamsters");
        System.out.println("     add <owner> <hamster> [<treats>] [-p] - add new hamster");
        System.out.println("     feed <owner> <hamster> <treats> [-p]  - feed treats to hamster");
        System.out.println("     state <owner> <hamster> [-p]          - how is my hamster doing?");
        System.out.println("     bill <owner> [-p]                     - the bill please!");
        return 2;
    }

    /**
     * The main command-line interface,
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printRtfm();
            System.exit(2);
        }

        String host = "127.0.0.1";
        int port = 9000;

        var argsLen = args.length;

        for (int i = argsLen - 2; i >= 0 ; i-= 2) {
            if ("-p".equals(args[i])) {
                argsLen -= 2;
                port = Integer.parseInt(args[i+1]);
            }
            else if ("-h".equals(args[i])) {
                argsLen -= 2;
                host = args[i+1];
            }
        }

        var client = new HamsterClient(host, port);
        var logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);

        try {
            switch (args[0]) {
                case "list":
                    String ownerName = null;
                    String hamsterName = null;
                    if (argsLen == 1) {
                        if (!client.list(null, null)) {
                            System.exit(1);
                        }
                        return;
                    }
                    if (argsLen != 3 && argsLen != 5) {
                        break;
                    }
                    if ("--hamster".equals(args[1])) {
                        hamsterName = args[2];
                    }
                    if ("--owner".equals(args[1])) {
                        ownerName = args[2];
                    }

                    if (argsLen == 5) {
                        if ("--hamster".equals(args[3])) {
                            hamsterName = args[4];
                        }
                        if ("--owner".equals(args[3])) {
                            ownerName = args[4];
                        }
                    }
                     if (!client.list(ownerName, hamsterName)) {
                         System.exit(1);
                     }
                    return;
                case "add":
                    if (argsLen == 3) {
                        client.add(args[1], args[2], (short)0);
                        return;
                    }
                    if (argsLen == 4) {
                        client.add(args[1], args[2], Short.parseShort(args[3]));
                        return;
                    }
                    break;
                case "feed":
                    if (argsLen == 4) {
                        client.feed(args[1], args[2], Short.parseShort(args[3]));
                        return;
                    }
                    break;
                case "state":
                    if (argsLen == 3) {
                        client.state(args[1], args[2]);
                        return;
                    }
                    break;
                case "bill":
                    if (argsLen == 2) {
                        client.bill(args[1]);
                        return;
                    }
                    break;
            }
            printRtfm();
            System.exit(2);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }
}
