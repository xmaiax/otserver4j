package otserver4j.structure;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.experimental.Accessors;

@Entity @Data @Accessors(chain = true)
public class Account {
  @Id private Integer accountNumber;
  private String passwordHash;
  private java.util.Calendar premiumExpiration;
}
