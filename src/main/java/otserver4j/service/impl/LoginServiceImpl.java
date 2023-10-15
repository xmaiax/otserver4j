package otserver4j.service.impl;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import otserver4j.entity.AccountEntity;
import otserver4j.exception.AccountException;
import otserver4j.repository.AccountRepository;
import otserver4j.service.LoginService;
import otserver4j.utils.MD5Utils;

@Service
public class LoginServiceImpl implements LoginService {

  private AccountRepository accountRepository;

  public LoginServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
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
  public AccountEntity createNewAccount(Integer accountNumber, String password) {
    this.validateAccountNumber(accountNumber);
    this.validatePassword(password);
    if(this.accountRepository.existsById(accountNumber))
      throw AccountException.ACCOUNT_DOES_EXIST_EXCEPTION;
    return this.accountRepository.save(new AccountEntity().setAccountNumber(accountNumber)
      .setPasswordHash(MD5Utils.getInstance().str2md5(password))).setPasswordHash(null);
  }

  @Override
  public AccountEntity addPremiumTimeInDays(Integer accountNumber, Integer days) {
    this.validateAccountNumber(accountNumber);
    final Optional<AccountEntity> accountOpt = this.accountRepository.findById(accountNumber);
    if(accountOpt.isEmpty())
      throw AccountException.ACCOUNT_DOES_NOT_EXIST_EXCEPTION;
    return this.accountRepository.save(accountOpt.get().setPremiumExpiration((
      accountOpt.get().getPremiumExpiration() == null || accountOpt.get().getPremiumExpiration()
        .isBefore(LocalDate.now()) ? LocalDate.now() : accountOpt.get().getPremiumExpiration())
          .plusDays(days))).setPasswordHash(null);
  }

  @Override
  public AccountEntity findAccountToLogin(Integer accountNumber, String password) throws AccountException {
    this.validateAccountNumber(accountNumber);
    this.validatePassword(password);
    final Optional<AccountEntity> accountOpt = this.accountRepository.findById(accountNumber);
    if(accountOpt.isEmpty()) throw AccountException.ACCOUNT_DOES_NOT_EXIST_EXCEPTION;
    if(!MD5Utils.getInstance().str2md5(password).equals(accountOpt.get().getPasswordHash()))
      throw AccountException.INCORRECT_PASSWORD_EXCEPTION;
    return accountOpt.get().setPasswordHash(null);
  }

  /*
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
      //----
      loadCharacterListPacketWrapper.setCharacterOptions(
        Collections.singletonList(new CharacterOption().setName("Maia").setVocation("Necromancer"))
      );
      //----
      log.info("Successful login from account '{}'!", account.getAccountNumber());
    }
    catch(AccountException accountException) {
      loadCharacterListPacketWrapper.setFailed(Boolean.TRUE);
      loadCharacterListPacketWrapper.setErrorMessage(accountException.getMessage());
    }
  }
  */

}
