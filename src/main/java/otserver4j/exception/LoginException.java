package otserver4j.exception;

public final class LoginException extends GenericException {
  private static final long serialVersionUID = -1L;
  public static final int UNEXPECTED_ERROR_CODE = 0x0a;
  @lombok.Getter public static enum CommonError {
    INSERT_ACCOUNT_NUMBER("Please insert the account number."),
    INSERT_PASSWORD("Please insert the password."),
    INCORRECT_PASSWORD("Incorrect password.");
    private String message;
    CommonError(String message) {
      this.message = message;
    }
  }
  public LoginException(String message) {
    super(message);
  }
  public LoginException(CommonError commonError) {
    super(commonError.getMessage());
  }
}
