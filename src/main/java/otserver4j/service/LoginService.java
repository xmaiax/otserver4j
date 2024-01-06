package otserver4j.service;

public interface LoginService {
  public static final Integer MAX_ACCOUNT_NUMBER = 99999999;
  public static final Integer MAX_PASSWORD_SIZE = 16;
  otserver4j.entity.AccountEntity createNewAccount(Integer accountNumber, String password);
  void addPremiumTimeInDays(Integer accountNumber, Integer days);
  otserver4j.entity.AccountEntity findAccountToLogin(Integer accountNumber, String password)
    throws otserver4j.exception.AccountException;
}
