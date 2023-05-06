package otserver4j.structure;

import java.util.Optional;

import otserver4j.structure.Item.ItemWithQuantity;

@lombok.AllArgsConstructor @lombok.Getter
public enum Tile {
  NOTHING(-1, "nothing")
  //----
  ,BRIGHT_GRASS(0x66, "bright grass")
  ,GROUND(0x67, "ground")
  ,GRASS(0x6a, "grass")
  //----
  ;

  private Integer code;
  private String description;
  public static Tile fromCode(Integer code) {
    return java.util.Arrays.asList(Tile.values()).stream()
      .filter(tl -> tl.getCode().equals(code)).findFirst().get();
  }
  @Override public String toString() { return this.description; }
  @lombok.Data @lombok.experimental.Accessors(chain = true)
  public static final class TileWithItems {
    private Tile tile;
    private java.util.List<Item.ItemWithQuantity> items;
    public String getLookedItemInfo(Item item) {
      if(item == null || this.items == null || this.items.isEmpty())
        return Tile.NOTHING.toString();
      final Optional<ItemWithQuantity> found = this.items.stream()
        .filter(_item -> item.equals(_item.getItem())).findAny();
      if(found.isEmpty()) return Tile.NOTHING.toString();
      return item.isStackable() ? item.getDescription(
        found.get().getQuantity()) : item.getDescription();
    }
  }
}
