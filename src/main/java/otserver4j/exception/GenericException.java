package otserver4j.exception;

public abstract class GenericException extends RuntimeException {
  private static final long serialVersionUID = -1L;
  public GenericException(String message) { super(message); }
}
