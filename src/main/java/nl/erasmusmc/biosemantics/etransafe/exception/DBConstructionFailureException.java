package nl.erasmusmc.biosemantics.etransafe.exception;

public class DBConstructionFailureException extends RuntimeException {

    public DBConstructionFailureException(String msg) {
        super(msg);
    }

    public DBConstructionFailureException(Exception e) {
        super(e);
    }
}
