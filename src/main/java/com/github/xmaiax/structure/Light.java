package com.github.xmaiax.structure;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true)
public class Light {
  private byte radius = (byte) 0xff;
  private byte color = (byte) 0xd7;
}
