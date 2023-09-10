package otserver4j.exception;

public final class AccountException extends GenericException {
  private static final long serialVersionUID = -1L;
  public static final AccountException
    INSERT_ACCOUNT_NUMBER_EXCEPTION = new AccountException("Please insert a valid account number.")
   ,INSERT_PASSWORD_EXCEPTION = new AccountException("Please insert a valid password.")
   ,ACCOUNT_DOES_NOT_EXIST_EXCEPTION = new AccountException("The given account does not exist.")
   ,ACCOUNT_DOES_EXIST_EXCEPTION = new AccountException("Account already exists, pick another account number.")
   ,INCORRECT_PASSWORD_EXCEPTION = new AccountException("Incorrect password.")
   ,WRONG_VERSION_NUMBER_EXCEPTION = new AccountException("Wrong client version.")
   ,INVALID_SELECTED_CHARACTER_EXCEPTION = new AccountException("Invalid selected character.")
   ;
  public AccountException(String message) { super(message); }
}
