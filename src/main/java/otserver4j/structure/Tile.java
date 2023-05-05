package otserver4j.structure;

@lombok.AllArgsConstructor @lombok.Getter
public enum Tile {
  NULL(0x00)
  //----
  ,GRASS(0x66)
  ,GROUND(0x67)
  ,FANCY_GRASS(0x6a)
  //----
  ;

  private Integer code;
  public static Tile fromCode(Integer code) {
    return java.util.Arrays.asList(Tile.values()).stream()
      .filter(d -> d.getCode().equals(code)).findFirst().get();
  }
  @lombok.Data @lombok.experimental.Accessors(chain = true)
  public static final class TileWithItems {
    private Tile tile;
    private java.util.List<otserver4j.structure.Item.ItemWithQuantity> items;
  }
}
