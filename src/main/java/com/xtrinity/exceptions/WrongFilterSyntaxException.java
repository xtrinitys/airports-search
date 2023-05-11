package com.xtrinity.exceptions;

public class WrongFilterSyntaxException extends Exception{
    public WrongFilterSyntaxException() {
    }

    public WrongFilterSyntaxException(String message) {
        super(message);
    }

    public WrongFilterSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongFilterSyntaxException(Throwable cause) {
        super(cause);
    }
}
