package com.datastax.themis;

public class ThemisException extends RuntimeException {

    public ThemisException(String msg) { super(msg); }

    public ThemisException(String msg, Throwable cause) { super(msg, cause); }
}
