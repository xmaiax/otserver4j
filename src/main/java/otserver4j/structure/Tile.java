package otserver4j.structure;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import otserver4j.structure.Item.ItemWithQuantity;

@lombok.Getter
public enum Tile {
  NULL(0x00),
  GRASS(0x66),
  GROUND(0x67),
  FANCY_GRASS(0x6a);
  private Integer code;
  Tile(Integer code) { this.code = code; }
  public static Tile fromCode(Integer code) {
    return java.util.Arrays.asList(Tile.values()).stream()
      .filter(d -> d.getCode().equals(code)).findFirst().get();
  }
  @Data @Accessors(chain = true)
  public static final class TileWithItems {
    private Tile tile;
    private List<ItemWithQuantity> items;
  }
}
