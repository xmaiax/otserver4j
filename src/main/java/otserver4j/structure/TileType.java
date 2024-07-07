package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE) @lombok.Getter
public enum TileType {
   VOID(100)
  ,EARTH(101)
  ,DIRT(103)
  ,GRASS1(106)
  ,GRASS2(194)
  ,SAND(231)
  ,GRASS3(280)
  ,GRASS4(293)
  ;
  private final Integer code;
  public static TileType fromCode(final Integer code) {
    return code == null ? TileType.VOID : java.util.Arrays.asList(TileType.values())
      .stream().filter(t -> t.getCode().equals(code)).findFirst().orElse(VOID);
  }
}
