package de.hsrm.cs.wwwvs.hamster.lib;

@SuppressWarnings("serial")
public class HamsterDatabaseCorruptException extends HamsterException {
	public static final String DEFAULT_MSG = "database is corrupted";

	public HamsterDatabaseCorruptException() {
		this(DEFAULT_MSG);
	}
	
	public HamsterDatabaseCorruptException(String msg) {
		super(msg);
	}
}
