package otserver4j.configuration;

import java.math.BigInteger;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import otserver4j.repository.AccountRepository;
import otserver4j.service.LoginService;

@AllArgsConstructor @Configuration
public class DatabaseDummyConfiguration {

  private final LoginService loginService;
  private final AccountRepository accountRepository;

  @PostConstruct public void initializeEmptyDatabase() {
    if(this.accountRepository.count() < BigInteger.ONE.longValue()) {
      final Integer defaultAccount = 123;
      final String defaultPassword = "abc";
      this.loginService.createNewAccount(defaultAccount, defaultPassword);
      this.loginService.addPremiumTimeInDays(defaultAccount, 20);
      
    }
  }

}
