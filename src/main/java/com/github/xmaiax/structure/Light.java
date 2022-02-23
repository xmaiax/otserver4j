package com.github.xmaiax.structure;

@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class Light {
  private byte radius = (byte) 0xff;
  private byte color = (byte) 0xd7;
}
