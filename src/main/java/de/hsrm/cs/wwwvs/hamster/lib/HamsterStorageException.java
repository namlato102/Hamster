package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterStorageException extends HamsterException {
	public static final String DEFAULT_MSG = "storage error";

	public HamsterStorageException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterStorageException(String msg) {
		super(msg);
	}
}
