package otserver4j.configuration;

import java.math.BigInteger;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import otserver4j.entity.AccountEntity;
import otserver4j.entity.PlayerCharacterEntity;
import otserver4j.repository.AccountRepository;
import otserver4j.repository.PlayerCharacterRepository;
import otserver4j.service.LoginService;
import otserver4j.structure.PlayerCharacterVocation;
import otserver4j.structure.Position;

@AllArgsConstructor @Configuration
public class DatabaseDummyConfiguration {

  private final LoginService loginService;
  private final AccountRepository accountRepository;
  private final PlayerCharacterRepository playerCharacterRepository;

  @PostConstruct public void initializeEmptyDatabase() {
    if(this.accountRepository.count() < BigInteger.ONE.longValue()) {
      final Integer defaultAccount = 123;
      final String defaultPassword = "abc";
      final AccountEntity account = this.loginService.createNewAccount(defaultAccount, defaultPassword);
      this.loginService.addPremiumTimeInDays(account.getAccountNumber(), 20);
      this.playerCharacterRepository.save(new PlayerCharacterEntity(account,
        "Stéfane", PlayerCharacterVocation.WARRIOR, new Position().setX(40).setY(50).setZ(6)));
    }
  }

}
