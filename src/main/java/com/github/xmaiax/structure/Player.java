package com.github.xmaiax.structure;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter
@Accessors(chain = true)
public class Player {
  private String name;
  @Override
  public String toString() {
    return this.name;
  }
}
