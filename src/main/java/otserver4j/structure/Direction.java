package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum Direction {
  NORTH(0x00, Boolean.TRUE, "N"), EAST(0x01, Boolean.TRUE, "E"),
  SOUTH(0x02, Boolean.TRUE, "S"), WEST(0x03, Boolean.TRUE, "W"),
  NORTHEAST(0x04, Boolean.FALSE, "NE"), NORTHWEST(0x05, Boolean.FALSE, "NW"),
  SOUTHEAST(0x06, Boolean.FALSE, "SE"), SOUTHWEST(0x07, Boolean.FALSE, "SW");
  private final Integer code;
  private final Boolean spawnable;
  private final String dbCode;
  public static Direction fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(Direction.values())
      .stream().filter(dr -> dr.getCode().equals(code)).findFirst().orElse(SOUTH); }
  public static Direction fromDatabaseCode(final String dbCode) {
    return dbCode == null || dbCode.isBlank() ? null : java.util.Arrays.asList(
      Direction.values()).stream().filter(dr -> dr.getDbCode().equalsIgnoreCase(dbCode))
        .findFirst().orElse(SOUTH); }
}
