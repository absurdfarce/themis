package com.datastax.themis;

public class ThemisException extends Exception {

    public ThemisException(String msg) { super(msg); }

    public ThemisException(String msg, Object... args) { super(String.format(msg,args)); }

    public ThemisException(Throwable root, String msg) { super(msg, root); }

    public ThemisException(Throwable root, String msg, Object... args) { super(String.format(msg,args), root); }
}
