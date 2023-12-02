package otserver4j.entity;

import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import otserver4j.structure.Position;

@Entity @Data @Accessors(chain = true)
public class PlayerCharacterEntity {

  @Id private Long identifier;
  private String name;

  private Long experience;
  public Integer getLevel() { return 1; }
  public Integer getPercentNextLevel() { return 50; }
  public Integer getSpeed() { return 10; }

  public static final String ATTRIBUTE_VALUE_PROPERTY_NAME = "value";
  public static final String ATTRIBUTE_MAX_PROPERTY_NAME = "maxValue";
  public static final String ATTRIBUTE_MAX_PROPERTY_PREFIX = "max_";

  @Embeddable @Data @Accessors(chain = true)
  public static class Attribute {
    private Integer value;
    private Integer maxValue;
  }

  public static final String ATTRIBUTE_LIFE_NAME = "life";
  @Embedded @AttributeOverrides({
    @AttributeOverride(name = ATTRIBUTE_VALUE_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_LIFE_NAME)),
    @AttributeOverride(name = ATTRIBUTE_MAX_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_MAX_PROPERTY_PREFIX + ATTRIBUTE_LIFE_NAME)
  )}) private Attribute life;

  public static final String ATTRIBUTE_MANA_NAME = "mana";
  @Embedded @AttributeOverrides({
    @AttributeOverride(name = ATTRIBUTE_VALUE_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_MANA_NAME)),
    @AttributeOverride(name = ATTRIBUTE_MAX_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_MAX_PROPERTY_PREFIX + ATTRIBUTE_MANA_NAME)
  )}) private Attribute mana;

  public static final String ATTRIBUTE_CAPACITY_NAME = "capacity";
  @Embedded @AttributeOverrides({
    @AttributeOverride(name = ATTRIBUTE_VALUE_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_CAPACITY_NAME)),
    @AttributeOverride(name = ATTRIBUTE_MAX_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_MAX_PROPERTY_PREFIX + ATTRIBUTE_CAPACITY_NAME)
  )}) private Attribute capacity;

  public static final String ATTRIBUTE_SOUL_NAME = "soul";
  @Embedded @AttributeOverrides({
    @AttributeOverride(name = ATTRIBUTE_VALUE_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_SOUL_NAME)),
    @AttributeOverride(name = ATTRIBUTE_MAX_PROPERTY_NAME,
      column = @Column(name = ATTRIBUTE_MAX_PROPERTY_PREFIX + ATTRIBUTE_SOUL_NAME)
  )}) private Attribute soul;

  public static enum SkillType {
    MAGIC, FIST, CLUB, SWORD, AXE, DISTANCE, SHIELD, FISHING;
    @Override public String toString() { return this.name(); }
  }

  public static final String SKILL_ = "skill_";

  @Embeddable @Data @Accessors(chain = true)
  public static class Skill {
    private Integer level;
    private Integer percent;
  }

  @Embeddable @Data @Accessors(chain = true)
  public static class Outfit {
    private Integer type;
    private Integer head;
    private Integer body;
    private Integer legs;
    private Integer feet;
    private Integer extra;
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter public static enum Slot {
    HEAD(0x01), NECK(0x02), BACKPACK(0x03), CHEST(0x04),
    RIGHT_HAND(0x05), LEFT_HAND(0x06), LEGS(0x07), RING(0x08),
    FEET(0x09), AMMUNITION(0x0a), INVALID(-1);
    private final Integer code;
    public static Slot fromCode(Integer code) {
      return Arrays.asList(Slot.values()).stream()
        .filter(sl -> sl.getCode().equals(code) || INVALID.equals(sl)).findFirst().get();
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter public static enum Condition {
    POISONED(0b00000001), ON_FIRE(0b00000010), STRUCK_BY_ENERGY(0b00000100),
    DRUNK(0b00001000), MAGIC_SHIELD_BUFF(0b00010000), PARALYZED(0b00100000),
    HASTE_BUFF(0b01000000), IN_BATTLE(0b10000000);
    private final Integer code;
    public static Integer getIconCodeFromStatuses(List<Condition> conditions) {
      return conditions.stream().mapToInt(condition -> condition.getCode()).sum();
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter public static enum Skull {
    NONE(0x00), YELLOW(0x01), GREEN(0x02), WHITE(0x03), RED(0x04);
    private final Integer code;
    public static Skull fromCode(Integer code) {
      return Arrays.asList(Skull.values()).stream().filter(sk -> sk.getCode().equals(code) ||
        NONE.equals(sk)).findFirst().get();
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter public static enum Party {
    NONE(0x00), REQUESTED(0x02), ACCEPTED(0x03), LEADER(0x04);
    private final Integer code;
    public static Party fromCode(Integer code) {
      return Arrays.asList(Party.values()).stream().filter(pt -> pt.getCode().equals(code) ||
        NONE.equals(pt)).findFirst().get();
    }
  }

//  private Profession profession;

//  private Skill magicSkill;
//  private Skill fistSkill;
//  private Skill clubSkill;
//  private Skill swordSkill;
//  private Skill axeSkill;
//  private Skill distanceSkill;
//  private Skill shieldSkill;
//  private Skill fishingSkill;

//  private Outfit outfit;

  private Position position;
//  private Direction direction;
  //private Map<Slot, Item.ItemWithQuantity> inventory;
  //private transient List<Condition> conditions;
  //private transient Skull skull;

}

