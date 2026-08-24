package dev.excsi.quickshare.exception;

public class UserAlreadyRegisteredException extends RuntimeException {

    public UserAlreadyRegisteredException(String email) {
        super(email);
    }
}
