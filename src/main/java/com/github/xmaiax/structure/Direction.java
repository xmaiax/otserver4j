package com.github.xmaiax.structure;

@lombok.Getter
public enum Direction {
  NORTH(0x00), EAST(0x01),
  SOUTH(0x02), WEST(0x03),
  NORTHEAST(0x04), NORTHWEST(0x05),
  SOUTHEAST(0x06), SOUTHWEST(0x07);
  private Integer code;
  Direction(Integer code) { this.code = code; }
  public static Direction fromCode(Integer code) {
    return java.util.Arrays.asList(Direction.values()).stream()
      .filter(d -> d.getCode().equals(code)).findFirst().get();
  }
}
