package otserver4j.structure;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import otserver4j.structure.PlayerCharacter.Slot;

@lombok.AllArgsConstructor @lombok.Getter
public enum Item {

  //                Code  Name                 Type                      W8   AC    Res                                           Dmg   WpnType                 Attrs                                            Plural     Details
  BACKPACK(         2854, "backpack",          ItemType.BACKPACK,        10,  1,    Collections.emptyMap(),                       null,  null,                  Collections.emptyMap(),                          null,      null),
  MAGIC_PLATE_ARMOR(3366, "Magic Plate Armor", ItemType.ARMOR,           70,  50,   Collections.singletonMap(Resistance.ALL, 30), null,  null,                  Collections.singletonMap(ItemAttribute.ALL, 5),  null,      "This armor is pretty powerful"),
  WARLORD_SWORD(    3296, "Warlord Sword",     ItemType.TWO_HAND_WEAPON, 50,  15,   Collections.emptyMap(),                       60,    WeaponType.SWORD,      Collections.singletonMap(ItemAttribute.ALL, 10), null,      "Owned by a fierce Orc Warlord"),
  CARLIN_SWORD(     3283, "Carlin Sword",      ItemType.ONE_HAND_WEAPON, 20,  null, null,                                         15,    WeaponType.SWORD,      Collections.emptyMap(),                          null,      null),
  CROSSBOW(         3349, "Crossbow",          ItemType.TWO_HAND_WEAPON, 25,  null, null,                                         25,    WeaponType.DISTANCE,   Collections.emptyMap(),                          null,      null),
  ARROW(            3447, "arrow",             ItemType.AMMUNITION,       1,  null, null,                                         4,     WeaponType.AMMUNITION, Collections.emptyMap(),                          "arrows",  null),
  NO_ITEM(          -1,   null,                null,                   null,  null, null,                                         null,  null,                  null,                                            null,      null);

  private Integer code;
  private String _name;
  private ItemType type;
  private Integer weight;
  private Integer armorClass;
  private Map<Resistance, Integer> resistances;
  private Integer damage;
  private WeaponType weaponType;
  private Map<ItemAttribute, Integer> attributes;
  private String plural;
  private String details;

  public Boolean isStackable() { return this.plural != null; }

  @lombok.AllArgsConstructor @lombok.Getter
  public static enum ItemType {
    HELMET(new Slot[] { Slot.HEAD, }),
    ARMOR(new Slot[] { Slot.CHEST, }),
    LEGS(new Slot[] { Slot.LEGS, }),
    BOOTS(new Slot[] { Slot.FEET, }),
    ONE_HAND_WEAPON(new Slot[] { Slot.LEFT_HAND, Slot.RIGHT_HAND, }),
    SHIELD(new Slot[] { Slot.LEFT_HAND, Slot.RIGHT_HAND, }),
    TWO_HAND_WEAPON(new Slot[] { Slot.LEFT_HAND, Slot.RIGHT_HAND, }),
    BACKPACK(new Slot[] { Slot.BACKPACK, }),
    AMMUNITION(new Slot[] { Slot.AMMUNITION, }),
    AMULET(new Slot[] { Slot.NECK, }),
    RING(new Slot[] { Slot.RING, }),
    MISC(new Slot[] { Slot.INVALID, });
    private Slot[] slots;
  }

  public static enum Resistance { POISON, FIRE, ENERGY, ALL, }
  public static enum WeaponType { SWORD, AXE, CLUB, DISTANCE, MAGIC, SHIELD, AMMUNITION, }
  public static enum ItemAttribute { SWORD, AXE, CLUB, FIST, SHIELD, MAGIC, DISTANCE, FISHING, ALL, }

  private void addComma(StringBuilder sb, Boolean comma) {
    if(comma) sb.append(", ");
  }

  private String detailedDescription() {
    final StringBuilder sb = new StringBuilder();
    if(this.armorClass != null || this.damage != null || this.weaponType != null ||
        (this.attributes != null && !this.attributes.isEmpty()) ||
        (this.resistances != null && !this.resistances.isEmpty())) {
      Boolean comma = Boolean.FALSE;
      sb.append(" (");
      if(this.armorClass != null) {
        sb.append(String.format("armor: %d", this.armorClass));
        comma = Boolean.TRUE;
      }
      if(this.damage != null) {
        this.addComma(sb, comma);
        sb.append(String.format("damage: %d", this.damage));
        comma = Boolean.TRUE;
      }
      if(this.weaponType != null) {
        this.addComma(sb, comma);
        sb.append(String.format("type: %s", this.weaponType.name().toLowerCase()));
        comma = Boolean.TRUE;
      }
      sb.append(")");
    }
    if(this.details != null && !this.details.isBlank())
      sb.append(String.format(". %s", this.details));
    return sb.toString();
  }

  public String getDescription(Integer quantity) {
    return String.format("%d %s%s", quantity, this.plural, this.detailedDescription());
    }

  private static final Pattern STARTS_WITH_VOWEL =
    Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE);
  public String getDescription() { return String.format("a%s %s%s",
    STARTS_WITH_VOWEL.matcher(_name).find() ? "n" : "", this._name,
      this.detailedDescription());
  }

  public static Item fromCode(Integer code) {
    return java.util.Arrays.asList(Item.values()).stream()
      .filter(it -> it.getCode().equals(code) || NO_ITEM.equals(it))
        .findFirst().get(); }

  @lombok.Data @lombok.experimental.Accessors(chain = true)
  public static class ItemWithQuantity {
    private Item item;
    private Integer quantity;
  }

}
