package otserver4j.service;

import otserver4j.entity.AccountEntity;
import otserver4j.exception.AccountException;

public interface LoginService {
  public static final Integer MAX_ACCOUNT_NUMBER = 99999999;
  public static final Integer MAX_PASSWORD_SIZE = 16;
  AccountEntity createNewAccount(Integer accountNumber, String password);
  void addPremiumTimeInDays(Integer accountNumber, Integer days);
  AccountEntity findAccountToLogin(Integer accountNumber, String password) throws AccountException;
}
