package com.github.xmaiax.exception;

public abstract class GenericException extends Exception {
  private static final long serialVersionUID = -1L;
  public GenericException(String message) {
    super(message);
  }
}
