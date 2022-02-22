package com.github.xmaiax.structure;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true)
public class Player {

  @Data @Accessors(chain = true)
  public static class Skill {
    private SkillType type;
    private byte level = 0;
    private byte percent = 0;
  }

  @Data @Accessors(chain = true)
  public static class Outfit {
    private Integer type;
    private Byte head, body,
      legs, feet ,extra;
  }

  public static enum SkillType {
    MAGIC, FIST, CLUB, SWORD,
    AXE, DISTANCE, SHIELD, FISHING;
    @Override public String toString() {
      return this.name();
    }
  }

  @Getter
  public static enum Profession {
    NECROMANCER(0x01, "Necromancer", 100, 10, 50, 20, 300, 10, 150),
    WARRIOR    (0x02, "Warrior",     200, 25, 20, 5,  450, 15, 100),
    MONK       (0x03, "Monk",        175, 20, 25, 10, 400, 10, 125),
    MAGE       (0X04, "Mage",        125, 5 , 75, 25, 250, 5,  150);
    private Integer code;
    private String _name;
    private Integer soulBase;
    private Integer lifeBase;
    private Integer lifePerLevel;
    private Integer manaBase;
    private Integer manaPerLevel;
    private Integer capBase;
    private Integer capPerLevel;
    Profession(Integer code, String _name,
        Integer lifeBase, Integer lifePerLevel,
        Integer manaBase, Integer manaPerLevel,
        Integer capBase, Integer capPerLevel, Integer soulBase) {
      this.code = code;
      this._name = _name;
      this.lifeBase = lifeBase;
      this.lifePerLevel = lifePerLevel;
      this.manaBase = manaBase;
      this.manaPerLevel = manaPerLevel;
      this.capBase = capBase;
      this.capPerLevel = capPerLevel;
      this.soulBase = soulBase;
    }
    @Override public String toString() { return this._name; }
  }

  @Getter
  public static enum Slot {
    HEAD(0x01), NECK(0x02), BACKPACK(0x03), CHEST(0x04),
    RIGHT_HAND(0x05), LEFT_HAND(0x06), LEGS(0x07), FEET(0x08),
    RING(0x09), EXTRA(0x0a), LAST(0x0b), INVALID(-1);
    private Integer code;
    Slot(Integer code) { this.code = code; }
    public static Slot fromCode(Integer code) {
      return java.util.Arrays.asList(Slot.values()).stream()
        .filter(d -> d.getCode().equals(code) || d == INVALID).findFirst().get();
    }
  }

  private Long identifier;
  private String name;
  private Integer level;
  private Long experience;
  private Byte percentNextLevel;
  private Profession profession;
  private Integer life, maxLife, mana, maxMana;
  private Integer capacity, maxCapacity, soul;
  private Position position;
  private Direction direction;
  private Outfit outfit;
  private Skill magicSkill, fistSkill, clubSkill, swordSkill,
    axeSkill, distanceSkill, shieldSkill, fishingSkill;

}
