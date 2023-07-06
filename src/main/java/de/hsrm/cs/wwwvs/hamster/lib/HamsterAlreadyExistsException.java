package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterAlreadyExistsException extends HamsterException {
	public static final String DEFAULT_MSG = "a hamster by that owner/name already exists";
	
	public HamsterAlreadyExistsException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterAlreadyExistsException(String msg) {
		super(msg);
	}
}
