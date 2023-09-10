package otserver4j.service;

import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper;
import otserver4j.entity.Account;
import otserver4j.exception.AccountException;

public interface LoginService {
  public static final Integer MAX_ACCOUNT_NUMBER = 99999999;
  public static final Integer MAX_PASSWORD_SIZE = 12;
  Account createNewAccount(Integer accountNumber, String password);
  Account addPremiumTimeInDays(Integer accountNumber, Integer days);
  Account findAccountToLogin(Integer accountNumber, String password) throws AccountException;
  void modifyCharacterListPacket(LoadCharacterListPacketWrapper loadCharacterListPacketWrapper);
}
