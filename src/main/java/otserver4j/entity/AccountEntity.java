package otserver4j.entity;

import java.time.LocalDate;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Getter @Setter @ToString @Accessors(chain = true)
public class AccountEntity {
  @Id private Integer accountNumber;
  private String passwordHash;
  private LocalDate premiumExpiration;
  @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
  private Set<PlayerCharacterEntity> characterList;
}
