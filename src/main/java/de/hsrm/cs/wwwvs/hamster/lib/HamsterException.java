package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterException extends Exception {
	public static final String DEFAULT_MSG = "Generic HamsterRPCException";

	public HamsterException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterException(String msg) {
		super(msg);
	}
}
