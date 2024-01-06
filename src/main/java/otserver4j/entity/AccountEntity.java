package otserver4j.entity;

import java.time.LocalDate;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Data;
import lombok.experimental.Accessors;

@Entity @Data @Accessors(chain = true)
public class AccountEntity {
  @Id private Integer accountNumber;
  private String passwordHash;
  private LocalDate premiumExpiration;
  @OneToMany(mappedBy = "account") private Set<PlayerCharacterEntity> characterList;
}
