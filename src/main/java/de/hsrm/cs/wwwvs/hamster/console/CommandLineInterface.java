package de.hsrm.cs.wwwvs.hamster.console;

import de.hsrm.cs.wwwvs.hamster.lib.HamsterException;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterLib;
import de.hsrm.cs.wwwvs.hamster.lib.HamsterState;

/**
 * Simple command-line interface for the hamsterlib
 * 
 * @author hinkel
 */
public class CommandLineInterface {

	private static int printRtfm() {
		System.out.println("Usage: hamster {<Option>} <param1> {<param2>}");
		System.out.println("Function: Hamster management");
		System.out.println("Verbs:");
		System.out.println("     list {<owner>}                   - show current list of hamsters");
		System.out.println("     add <owner> <hamster> [<treats>] - add new hamster");
		System.out.println("     feed <owner> <hamster> <treats>  - feed treats to hamster");
		System.out.println("     state <owner> <hamster>          - how is my hamster doing?");
		System.out.println("     bill <owner>                     - the bill please!");
		return 2;
	}

	private static HamsterLib lib = new HamsterLib();

	private static int list(String owner) throws HamsterException {
		var it = lib.iterator();
		var hamster = lib.directory(it, owner, null);
		System.out.println("Owner\tName\tPrice\ttreats left");

		var name = lib.new OutString();
		var ownerName = lib.new OutString();
		var price = lib.new OutShort();

		do {
			var treats = lib.readentry(hamster, ownerName, name, price);
			System.out.println(String.format("%s\t%s\t%d €\t%d", ownerName.getValue(), name.getValue(), price.getValue(), treats));
			hamster = lib.directory(it, owner, null);
		} while (true);
	}

	private static int add(String owner, String hamster, String treats) throws HamsterException {
		lib.new_(owner, hamster, Short.parseShort(treats));
		System.out.println("Done!");
		return 0;
	}

	private static int feed(String owner, String hamster, String treats) throws HamsterException {
		var remaining = lib.givetreats(lib.lookup(owner, hamster), Short.parseShort(treats));
		System.out.printf("Done! %d treats remaining in store", remaining);
		return 0;
	}

	private static int state(String owner, String hamster) throws HamsterException {
		var state = new HamsterState();
		lib.howsdoing(lib.lookup(owner, hamster), state);
		System.out.println(String.format("%s's hamster %s has done > %d hamster wheel revolutions,\r\nand has %d treats left in store. Current price is %d €", 
			owner, 
			hamster, 
			state.getRounds(),
			state.getTreatsLeft(),
			state.getCost()));
		return 0;
	}

	private static int bill(String owner) throws HamsterException {
		var cost = lib.collect(owner);
		System.out.println(String.format("%s has to pay %d €", owner, cost));
		return 0;
	}

	/**
	 * The main command-line interface,
	 * TODO add your code here
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			printRtfm();
			System.exit(2);
		}
		try {
			switch (args[0]) {
				case "list":
					if (args.length == 0) {
						list(null);
						System.exit(0);
					}
					if (args.length == 1) {
						list(args[0]);
						System.exit(0);
					}
					break;
				case "add":
					if (args.length == 2) {
						add(args[0], args[1], "0");
						System.exit(0);
					}
					if (args.length == 3) {
						add(args[0], args[1], args[2]);
						System.exit(0);
					}
					break;
				case "feed":
					if (args.length == 3) {
						feed(args[0], args[1], args[2]);
						System.exit(0);
					}
					break;
				case "state":
					if (args.length == 2) {
						state(args[0], args[1]);
						System.exit(0);
					}
					break;
				case "bill":
					if (args.length == 1) {
						bill(args[0]);
						System.exit(0);
					}
					break;
			}
			printRtfm();
			System.exit(2);
		} catch (HamsterException exception) {
			System.err.println("Error: ".concat(exception.getMessage()));
			System.exit(1);
		}
	}

}
