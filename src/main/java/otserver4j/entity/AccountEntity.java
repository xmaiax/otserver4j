package otserver4j.entity;

@javax.persistence.Entity @javax.persistence.Table(name = AccountEntity.TABLE_NAME)
@lombok.Getter @lombok.Setter @lombok.ToString
@lombok.experimental.Accessors(chain = true) public class AccountEntity {
  static final String TABLE_NAME = "account";
  @javax.persistence.Column(name = TABLE_NAME + "_id") @javax.persistence.Id private Integer accountNumber;
  @javax.persistence.Column(nullable = false, length = 32) private String passwordHash;
  private java.time.LocalDate premiumExpiration;
  @javax.persistence.OneToMany(mappedBy = "account", fetch = javax.persistence.FetchType.EAGER)
  private java.util.Set<PlayerCharacterEntity> characterList;
}
