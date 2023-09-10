package otserver4j.service.impl;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper;
import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper.CharacterOption;
import otserver4j.exception.AccountException;
import otserver4j.repository.AccountRepository;
import otserver4j.service.LoginService;
import otserver4j.structure.Account;
import otserver4j.utils.MD5Utils;

@Slf4j @Service
public class LoginServiceImpl implements LoginService {

  private AccountRepository accountRepository;

  private Integer version;
  private String messageOfTheDay;
  private String host;
  private Integer port;

  public LoginServiceImpl(AccountRepository accountRepository,
      @Value("${otserver.version}") Integer version,
      @Value("${otserver.motd}") String messageOfTheDay,
      @Value("${otserver.host}") String host,
      @Value("${otserver.port}") Integer port) {
    this.accountRepository = accountRepository;
    this.version = version;
    this.messageOfTheDay = messageOfTheDay;
    this.host = host;
    this.port = port;
  }

  private void validateAccountNumber(Integer accountNumber) throws AccountException {
    if(accountNumber == null || accountNumber < BigInteger.ONE.intValue() ||
       accountNumber > MAX_ACCOUNT_NUMBER) throw AccountException.INSERT_ACCOUNT_NUMBER_EXCEPTION;
  }

  private void validatePassword(String password) {
    if(password == null || password.isBlank() || password.length() > MAX_PASSWORD_SIZE)
      throw AccountException.INSERT_PASSWORD_EXCEPTION;
  }

  @Override
  public Account createNewAccount(Integer accountNumber, String password) {
    this.validateAccountNumber(accountNumber);
    this.validatePassword(password);
    if(this.accountRepository.existsById(accountNumber))
      throw AccountException.ACCOUNT_DOES_EXIST_EXCEPTION;
    return this.accountRepository.save(new Account().setAccountNumber(accountNumber)
      .setPasswordHash(MD5Utils.getInstance().str2md5(password))).setPasswordHash(null);
  }

  @Override
  public Account addPremiumTimeInDays(Integer accountNumber, Integer days) {
    this.validateAccountNumber(accountNumber);
    final Optional<Account> accountOpt = this.accountRepository.findById(accountNumber);
    if(accountOpt.isEmpty())
      throw AccountException.ACCOUNT_DOES_NOT_EXIST_EXCEPTION;
    Calendar premiumExpiration = accountOpt.get().getPremiumExpiration();
    if(premiumExpiration == null || premiumExpiration.before(Calendar.getInstance()))
      premiumExpiration = Calendar.getInstance();
    premiumExpiration.add(Calendar.DAY_OF_MONTH, days);
    return this.accountRepository.save(accountOpt.get()
      .setPremiumExpiration(premiumExpiration)).setPasswordHash(null);
  }

  @Override
  public Account findAccountToLogin(Integer accountNumber, String password) throws AccountException {
    this.validateAccountNumber(accountNumber);
    this.validatePassword(password);
    final Optional<Account> accountOpt = this.accountRepository.findById(accountNumber);
    if(accountOpt.isEmpty()) throw AccountException.ACCOUNT_DOES_NOT_EXIST_EXCEPTION;
    if(!MD5Utils.getInstance().str2md5(password).equals(accountOpt.get().getPasswordHash()))
      throw AccountException.INCORRECT_PASSWORD_EXCEPTION;
    return accountOpt.get().setPasswordHash(null);
  }

  @Override
  public void modifyCharacterListPacket(LoadCharacterListPacketWrapper loadCharacterListPacketWrapper) {
    try {
      if(!this.version.equals(loadCharacterListPacketWrapper.getClientVersion()))
        throw AccountException.WRONG_VERSION_NUMBER_EXCEPTION;
      final Account account = this.findAccountToLogin(
        loadCharacterListPacketWrapper.getAccountNumber(),
        loadCharacterListPacketWrapper.getPassword());
      loadCharacterListPacketWrapper.setFailed(Boolean.FALSE).setMotd(this.messageOfTheDay)
        .setPremiumExpiration(account.getPremiumExpiration()).setHost(this.host).setPort(this.port);
      /*----*/
      loadCharacterListPacketWrapper.setCharacterOptions(
        Collections.singletonList(new CharacterOption().setName("Maia").setVocation("Necromancer"))
      );
      /*----*/
      log.info("Successful login from account '{}'!", account.getAccountNumber());
    }
    catch(AccountException accountException) {
      loadCharacterListPacketWrapper.setFailed(Boolean.TRUE);
      loadCharacterListPacketWrapper.setErrorMessage(accountException.getMessage());
    }
  }

}
