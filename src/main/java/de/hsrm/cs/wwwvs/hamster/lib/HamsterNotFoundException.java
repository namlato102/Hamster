package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterNotFoundException extends HamsterException {
	public static final String DEFAULT_MSG = "A hamster or hamster owner could not be found.";
	
	public HamsterNotFoundException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterNotFoundException(String msg) {
		super(msg);
	}
}
