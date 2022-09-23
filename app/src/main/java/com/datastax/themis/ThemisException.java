package com.datastax.themis;

public class ThemisException extends Exception {

    public ThemisException(String msg) { super(msg); }

    public ThemisException(String msg, Throwable root) { super(msg, root); }
}
