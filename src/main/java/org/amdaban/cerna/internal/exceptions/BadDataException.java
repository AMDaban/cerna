package org.amdaban.cerna.internal.exceptions;

public class BadDataException extends Exception { 
    public BadDataException(String errorMessage) {
        super(errorMessage);
    }
}