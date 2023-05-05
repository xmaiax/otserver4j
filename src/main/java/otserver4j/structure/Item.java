package otserver4j.structure;

@lombok.Getter
public enum Item {
  BACKPACK(0xb26, Boolean.FALSE),
  MAGIC_PLATE_ARMOR(0x9a8, Boolean.FALSE),
  CROSSBOW(0x997, Boolean.FALSE),
  BOLTS(0x9ef, Boolean.FALSE),
  NO_ITEM(-1, null);
  private Integer code;
  private Boolean stackable;
  Item(Integer code, Boolean stackable) {
    this.code = code; this.stackable = stackable; }
  public static Item fromCode(Integer code) {
    return java.util.Arrays.asList(Item.values()).stream()
      .filter(d -> d.getCode().equals(code) || d == NO_ITEM)
        .findFirst().get(); }
  @lombok.Data @lombok.experimental.Accessors(chain = true)
  public static class ItemWithQuantity {
    private Item item;
    private byte quantity;
  }
}
