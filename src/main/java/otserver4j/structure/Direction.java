package otserver4j.structure;

@lombok.Getter
public enum Direction {
  NORTH(0x00, Boolean.TRUE), EAST(0x01, Boolean.TRUE),
  SOUTH(0x02, Boolean.TRUE), WEST(0x03, Boolean.TRUE),
  NORTHEAST(0x04, Boolean.FALSE), NORTHWEST(0x05, Boolean.FALSE),
  SOUTHEAST(0x06, Boolean.FALSE), SOUTHWEST(0x07, Boolean.FALSE);
  private Integer code;
  private Boolean spawnable;
  Direction(Integer code, Boolean spawnable) {
    this.code = code;
    this.spawnable = spawnable;
  }
  public static Direction fromCode(Integer code) {
    return java.util.Arrays.asList(Direction.values()).stream()
      .filter(d -> d.getCode().equals(code)).findFirst().get();
  }
}
