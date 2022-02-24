package otserver4j.structure;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import otserver4j.structure.Item.ItemWithQuantity;

@lombok.Getter
public enum TileType {
  GRASS(0x6a);
  private Integer code;
  TileType(Integer code) { this.code = code; }
  public static TileType fromCode(Integer code) {
    return java.util.Arrays.asList(TileType.values()).stream()
      .filter(d -> d.getCode().equals(code)).findFirst().get();
  }
  @Data @Accessors(chain = true)
  public static final class TileWithItems {
    private TileType type;
    private List<ItemWithQuantity> items;
  }
}
