package com.datastax.themis.config;

public class ConfigException extends Exception {

    public ConfigException(String msg) { super(msg); }

    public ConfigException(String msg, Throwable root) { super(msg, root); }
}
