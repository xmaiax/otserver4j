package otserver4j.exception;

public abstract class GenericException extends IllegalStateException {
  private static final long serialVersionUID = -1L;
  public GenericException(String message) {
    super(message);
  }
}
