package otserver4j.entity;

import javax.persistence.Column;

import org.hibernate.annotations.Parameter;

@javax.persistence.Entity @javax.persistence.Table(name = AccountEntity.TABLE_NAME)
@lombok.Getter @lombok.Setter @lombok.ToString
@lombok.experimental.Accessors(chain = true) public class AccountEntity {
  static final String TABLE_NAME = "account";
  
  @Column(name = TABLE_NAME + "_id") @javax.persistence.Id
  @javax.persistence.GeneratedValue(generator = TABLE_NAME + "_sequence")
  @org.hibernate.annotations.GenericGenerator(strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      name = TABLE_NAME + "_sequence", parameters = {
    @Parameter(name = "sequence_name", value = TABLE_NAME + "_sequence"),
    @Parameter(name = "initial_value", value = "1"),
    @Parameter(name = "increment_size", value = "1"),
  }) private Integer accountNumber;
  @Column(nullable = false, length = 32) private String passwordHash;
  private java.time.LocalDate premiumExpiration;
  @javax.persistence.OneToMany(mappedBy = "account", fetch = javax.persistence.FetchType.EAGER)
  private java.util.Set<PlayerCharacterEntity> characterList;
}
