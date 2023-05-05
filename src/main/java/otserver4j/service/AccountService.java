package otserver4j.service;

public interface AccountService {
  otserver4j.structure.Account findAccount(Integer accountNumber, String password)
    throws otserver4j.exception.LoginException;
}
