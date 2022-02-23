package otserver4j.exception;

public final class LoginException extends GenericException {
  private static final long serialVersionUID = -1L;
  @lombok.Getter public static enum CommonError {
    INSERT_ACCOUNT_NUMBER("Please insert the account number."),
    INSERT_PASSWORD("Please insert the password."),
    ACCOUNT_DOES_NOT_EXIST("Account does not exist."),
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
