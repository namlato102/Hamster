package de.hsrm.cs.wwwvs.hamster.lib;

public class HamsterRefusedTreatException extends HamsterException {
    public static final String DEFAULT_MSG = "The hamster refused the treat.";

    public HamsterRefusedTreatException() {
        this(DEFAULT_MSG);
    }

    public HamsterRefusedTreatException(String msg) {
        super(msg);
    }
}
