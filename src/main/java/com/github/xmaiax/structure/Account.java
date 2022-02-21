package com.github.xmaiax.structure;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true)
public class Account {
  @Data @ToString
  @Accessors(chain = true)
  public static class CharacterOption {
    private String name;
    private String _class;
  }
  private Integer accountNumber;
  private String passwordMD5;
  private java.util.Calendar premiumExpiration;
  private java.util.List<CharacterOption> characters;
}
