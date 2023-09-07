package otserver4j.service.impl;

import java.util.Calendar;

import org.springframework.stereotype.Service;

import otserver4j.exception.LoginException;
import otserver4j.exception.LoginException.CommonError;
import otserver4j.service.AccountService;
import otserver4j.structure.Account;
import otserver4j.utils.MD5Utils;

@Service
public class AccountServiceImpl implements AccountService {

  @Override
  public Account findAccount(Integer accountNumber, String password) throws LoginException {
    if(accountNumber < 1) throw new LoginException(CommonError.INSERT_ACCOUNT_NUMBER);
    if(password == null || password.isEmpty()) throw new LoginException(CommonError.INSERT_PASSWORD);
    if(accountNumber != 123) throw new LoginException(CommonError.ACCOUNT_DOES_NOT_EXIST);
    final String md5password = MD5Utils.getInstance().str2md5(password);
    if(!MD5Utils.getInstance().str2md5("abc").equals(md5password))
      throw new LoginException(CommonError.INCORRECT_PASSWORD);
    final Calendar premiumExpiration = Calendar.getInstance();
    premiumExpiration.add(Calendar.DAY_OF_MONTH, 15);
    return new Account()
      .setAccountNumber(accountNumber)
      .setPasswordMD5(MD5Utils.getInstance().str2md5(password))
      .setPremiumExpiration(premiumExpiration)
      .setCharacters(java.util.Arrays.asList(new Account.CharacterOption[] {
        new Account.CharacterOption().setName("Maia").setProfession("Necromancer"),
        new Account.CharacterOption().setName("Stefane").setProfession("Wizard"),
      }));
  }

}
