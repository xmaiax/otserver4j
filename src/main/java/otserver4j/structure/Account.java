package otserver4j.structure;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true) public class Account {
  @Data @lombok.ToString
  @Accessors(chain = true)
  public static class CharacterOption {
    private String name;
    private String profession;
  }
  private Integer accountNumber;
  private String passwordMD5;
  private java.util.Calendar premiumExpiration;
  private java.util.List<CharacterOption> characters;
}
