package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterNameTooLongException extends HamsterException {
	public static final String DEFAULT_MSG = "the specified name is too long";
	
	public HamsterNameTooLongException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterNameTooLongException(String msg) {
		super(msg);
	}
}
