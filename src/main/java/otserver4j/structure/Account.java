package otserver4j.structure;

import lombok.Data;
import lombok.experimental.Accessors;
import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper.CharacterOption;

@Data @Accessors(chain = true) public class Account {
  private Integer accountNumber;
  private String passwordHash;
  private java.util.Calendar premiumExpiration;
  private java.util.List<CharacterOption> characters;
}
