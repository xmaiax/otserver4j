package otserver4j.service;

import otserver4j.exception.LoginException;
import otserver4j.structure.Account;

public interface AccountService {
  Account findAccount(Integer accountNumber, String password) throws LoginException;
}
