package de.mainwetten.security.usage;

public class UsageLimitExceededException
        extends RuntimeException {

    public UsageLimitExceededException(String message) {
        super(message);
    }
}
