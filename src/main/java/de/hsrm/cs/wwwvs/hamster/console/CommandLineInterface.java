package de.hsrm.cs.wwwvs.hamster.console;

import de.hsrm.cs.wwwvs.hamster.lib.*;


/**
 * Simple command-line interface for the hamsterlib
 *
 * @author hinkel
 */
public class CommandLineInterface {

    private static void printRtfm() {
        System.out.println("Usage: hamster {<Option>} <param1> {<param2>}");
        System.out.println("Function: Hamster management");
        System.out.println("Verbs:");
        System.out.println("     list {<owner>}                   - show current list of hamsters");
        System.out.println("     add <owner> <hamster> [<treats>] - add new hamster");
        System.out.println("     feed <owner> <hamster> <treats>  - feed treats to hamster");
        System.out.println("     state <owner> <hamster>          - how is my hamster doing?");
        System.out.println("     bill <owner>                     - the bill please!");
    }

    /**
     * The main command-line interface,
     *
     * @param args parameters from console
     */
    public static void main(String[] args) {
        HamsterLib hamsterLib = new HamsterLib();
        HamsterManagement hamsterManagement = new HamsterManagement();

        while (true) {
            if (args.length == 0) {
                System.err.println("Unknown command: No arguments");
                System.exit(2);
            }
            if (args[0].equals("list")
                || args[0].equals("add")
                    || args[0].equals("feed")
                    || args[0].equals("state")
                    || args[0].equals("bill")
            ) {
                try {
                    switch (args[0]) {
                        case "list":
                            if (args.length > 2) {
                                System.err.println("Unknown command (list)");
                                System.exit(2);
                            } else if (args.length == 1) {
                                hamsterManagement.listEverything(hamsterLib);
                            } else {
                                hamsterManagement.listHamster(hamsterLib, args[1]);
                            }
                            break;
                        case "add":
                            if (args.length > 4) {
                                System.err.println("Unknown command (add)");
                                System.exit(2);
                            }
                            if (args.length == 3)
                                hamsterManagement.addHamster(hamsterLib, args[1], args[2], (short) 0);
                            else
                                hamsterManagement.addHamster(hamsterLib, args[1], args[2], Short.parseShort(args[3]));
                            break;
                        case "feed":
                            if (args.length > 4) {
                                System.err.println("Unknown command (feed)");
                                System.exit(2);
                            }
                            hamsterManagement.feed(hamsterLib, args[1], args[2], Short.parseShort(args[3]));
                            break;
                        case "state":
                            if (args.length > 3) {
                                System.err.println("Unknown command (state)");
                                System.exit(2);
                            }
                            hamsterManagement.checkState(hamsterLib, args[1], args[2]);
                            break;
                        case "bill":
                            if (args.length > 2) {
                                System.err.println("Unknown command (bill)");
                                System.exit(2);
                            }
                            hamsterManagement.bill(hamsterLib, args[1]);
                            break;
                        default:
                            System.err.println("Unknown command: " + args[0]);
                            System.exit(2);
                            break;
                    }
                    System.exit(0);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    System.exit(2);
                }
            } else {
                System.err.println("Unknown command");
                printRtfm();
                System.exit(2);
            }
        }
    }

}
/*
        try {
            hamsterManagement.addHamster(hamsterLib, "Sascha", "Bokken", (short) 50);
            hamsterManagement.addHamster(hamsterLib, "Beni", "Bossen", (short) 10);
            hamsterManagement.listEverything(hamsterLib);
            hamsterManagement.listHamster(hamsterLib, "Sascha");
            //hamsterManagement.feed(hamsterLib, "Beni", "Bossen", (short) 12);
            //Thread.sleep(10000);
            //hamsterManagement.checkState(hamsterLib,  "Beni", "Bossen");
            //hamsterManagement.bill(hamsterLib, "Beni");
        } catch (HamsterNameTooLongException e) {
            throw new RuntimeException(e);
        } catch (HamsterStorageException e) {
            throw new RuntimeException(e);
        } catch (HamsterAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (HamsterDatabaseCorruptException e) {
            throw new RuntimeException(e);

        }
*/














