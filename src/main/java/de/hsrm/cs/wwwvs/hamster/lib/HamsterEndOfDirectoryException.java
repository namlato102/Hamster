package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterEndOfDirectoryException extends HamsterException {
	public static final String DEFAULT_MSG = "The directory method reached the end.";
	
	public HamsterEndOfDirectoryException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterEndOfDirectoryException(String msg) {
		super(msg);
	}
}
