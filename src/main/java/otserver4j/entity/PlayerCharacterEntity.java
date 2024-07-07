package otserver4j.entity;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import otserver4j.structure.Direction;
import otserver4j.structure.PlayerCharacterCondition;
import otserver4j.structure.PlayerCharacterSkull;
import otserver4j.structure.PlayerCharacterVocation;
import otserver4j.structure.Position;
import otserver4j.utils.ExperienceUtils;
import otserver4j.utils.SkillUtils;

@Table(name = PlayerCharacterEntity.TABLE_NAME, uniqueConstraints = {
  @UniqueConstraint(name = "unique_player_name", columnNames = { "name", }),
}) @Entity @NoArgsConstructor @Data @Accessors(chain = true) public class PlayerCharacterEntity {

  static final String TABLE_NAME = "player_character";
  static final int NAME_MAX_LENGTH = 16;

  @Column(name = TABLE_NAME + "_id") @javax.persistence.Id
  @GeneratedValue(generator = TABLE_NAME + "_sequence")
  @GenericGenerator(strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      name = TABLE_NAME + "_sequence", parameters = {
    @Parameter(name = "sequence_name", value = TABLE_NAME + "_sequence"),
    @Parameter(name = "initial_value", value = "1"),
    @Parameter(name = "increment_size", value = "1"),
  }) private Long identifier;
  @Column(nullable = false, length = NAME_MAX_LENGTH) private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accountNumber", nullable = false,
    foreignKey = @ForeignKey(name = "player_character_account_fk"))
  @JsonIgnore private AccountEntity account;

  private Position position;
  @Column(nullable = false, length = 2) private Direction direction;

  @JsonIgnore @Column(nullable = false) private Long experience;
  @Column(nullable = false) private PlayerCharacterVocation vocation;

  @Data @Accessors(chain = true) public static class AttributeWrapper {
    private Integer value;
    private Integer maxValue;
  }

  @Data @Accessors(chain = true) public static class Attributes {
    private Long experience;
    private Integer level;
    private Integer nextLevelPercent;
    private AttributeWrapper life;
    private AttributeWrapper mana;
    private AttributeWrapper soul;
    private AttributeWrapper capacity;
  }

  @JsonIgnore @Column(nullable = false) private Integer currentLife;
  @JsonIgnore @Column(nullable = false) private Integer currentMana;
  @JsonIgnore @Column(nullable = false) private Integer currentSoul;

  @JsonIgnore public Integer getLevel() { return this.experience == null ? null :
    ExperienceUtils.INSTANCE.calculateLevelFromExperience(this.experience); }
  @JsonIgnore public Integer getNextLevelPercent() { return this.experience == null ? null :
    ExperienceUtils.INSTANCE.calculateNextLevelPercentFromExperience(this.experience); }

  private Integer maxLifeForLevel(Integer level) { return level == null ||
    this.vocation == null ? null : this.vocation.getLifeBase() + this.vocation.getLifePerLevel() * level; }
  @JsonIgnore public Integer getMaxLife() { return this.maxLifeForLevel(this.getLevel()); }

  private Integer maxManaForLevel(Integer level) { return level == null ||
    this.vocation == null ? null : this.vocation.getManaBase() + this.vocation.getManaPerLevel() * level; }
  @JsonIgnore public Integer getMaxMana() { return this.maxManaForLevel(this.getLevel()); }

  private Integer maxCapacityForLevel(Integer level) { return level == null ||
    this.vocation == null ? null : this.vocation.getCapacityBase() + this.vocation.getCapacityPerLevel() * level; }
  @JsonIgnore public Integer getMaxCapacity() { return this.maxCapacityForLevel(this.getLevel()); }

  public Attributes getAttributes() {
    if(this.vocation == null || this.experience == null) return null;
    final Integer
      currentLevel = this.getLevel(),
      maxLife = this.maxLifeForLevel(currentLevel),
      maxMana = this.maxManaForLevel(currentLevel),
      maxCapacity = this.maxCapacityForLevel(currentLevel);
    return new Attributes().setExperience(this.experience)
      .setLevel(currentLevel).setNextLevelPercent(this.getNextLevelPercent())
      .setLife(new AttributeWrapper().setValue(this.currentLife == null || this.currentLife > maxLife ?
        maxLife : this.currentLife).setMaxValue(maxLife))
      .setMana(new AttributeWrapper().setValue(this.currentMana == null || this.currentMana > maxMana ?
        maxMana : this.currentMana).setMaxValue(maxMana))
      .setSoul(new AttributeWrapper().setValue(this.currentSoul == null ||
        this.currentSoul > this.vocation.getSoul() ? this.vocation.getSoul() :
          this.currentSoul).setMaxValue(this.vocation.getSoul()))
      .setCapacity(new AttributeWrapper().setMaxValue(maxCapacity).setValue(
        //---- TODO: Calcular baseado no inventario
        java.math.BigInteger.TEN.intValue()
        //----
      ));
  }

  public PlayerCharacterEntity setAttributes(final Attributes attributes) {
    if(attributes != null) {
      this.experience = attributes.getExperience() == null ? ZERO.longValue() : attributes.getExperience();
      final Integer level = this.experience == null ? ONE.intValue() : this.getLevel();
      this.currentLife = attributes.getLife() == null || attributes.getLife().getValue() == null ?
        this.maxLifeForLevel(level) : attributes.getLife().getValue();
      this.currentMana = attributes.getMana() == null || attributes.getMana().getValue() == null ?
        this.maxManaForLevel(level) : attributes.getMana().getValue();
      this.currentSoul = attributes.getSoul() == null || attributes.getSoul().getValue() == null ?
        (this.vocation == null ? null : this.vocation.getSoul()) : attributes.getSoul().getValue(); 
    }
    return this;
  }

  @Data @Accessors(chain = true)
  public static class Skill {
    private Integer level;
    private Integer percent;
    private Long count;
  }

  private static final Skill EMPTY_SKILL = new Skill()
    .setLevel(BigInteger.ZERO.intValue())
    .setPercent(BigInteger.ZERO.intValue())
    .setCount(BigInteger.ZERO.longValue());

  @JsonIgnore @Column(nullable = false) private Long totalManaSpent = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getMagicSkill() { return SkillUtils.INSTANCE.calculateSkillFromCount(
    this.totalManaSpent, this.vocation.getMagicSkillFactor()); }

  @JsonIgnore @Column(nullable = false) private Long fistHitCount = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getFistSkill() {
    return this.fistHitCount == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.fistHitCount,
        this.vocation.getFistSkillFactor()); }

  @JsonIgnore @Column(nullable = false) private Long clubHitCount = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getClubSkill() {
    return this.clubHitCount == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.clubHitCount,
        this.vocation.getClubSkillFactor()); }

  @JsonIgnore @Column(nullable = false) private Long swordHitCount = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getSwordSkill() {
    return this.swordHitCount == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.swordHitCount,
        this.vocation.getSwordSkillFactor()); }

  @JsonIgnore @Column(nullable = true) private Long axeHitCount = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getAxeSkill() {
    return this.axeHitCount == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.axeHitCount,
        this.vocation.getAxeSkillFactor()); }

  @JsonIgnore @Column(nullable = true) private Long distanceHitCount = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getDistanceSkill() {
    return this.distanceHitCount == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.distanceHitCount,
        this.vocation.getDistanceSkillFactor()); }

  @JsonIgnore @Column(nullable = true) private Long blockedHitCount = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getShieldSkill() {
    return this.blockedHitCount == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.blockedHitCount,
        this.vocation.getShieldingSkillFactor()); }

  @JsonIgnore @Column(nullable = true) private Long fishingTries = BigInteger.ZERO.longValue();
  @JsonIgnore public Skill getFishingSkill() {
    return this.fishingTries == null || this.vocation == null ? EMPTY_SKILL :
      SkillUtils.INSTANCE.calculateSkillFromCount(this.fishingTries,
        this.vocation.getFishingSkillFactor()); }

  @Data @Accessors(chain = true)
  public static class AllSkills {
    private Skill magic;
    private Skill fist;
    private Skill club;
    private Skill sword;
    private Skill axe;
    private Skill distance;
    private Skill shield;
    private Skill fishing;
  }

  public AllSkills getSkills() {
    return new AllSkills().setMagic(this.getMagicSkill()).setFishing(this.getFishingSkill())
      .setFist(this.getFistSkill()).setClub(this.getClubSkill())
      .setSword(this.getSwordSkill()).setAxe(this.getAxeSkill())
      .setDistance(this.getDistanceSkill()).setShield(this.getShieldSkill());
  }

  public PlayerCharacterEntity setSkills(final AllSkills allSkills) {
    return this
      .setTotalManaSpent(allSkills.getMagic().getCount())
      .setFistHitCount(allSkills.getFist().getCount())
      .setClubHitCount(allSkills.getClub().getCount())
      .setSwordHitCount(allSkills.getSword().getCount())
      .setAxeHitCount(allSkills.getAxe().getCount())
      .setDistanceHitCount(allSkills.getDistance().getCount())
      .setBlockedHitCount(allSkills.getShield().getCount())
      .setFishingTries(allSkills.getFishing().getCount());
  }

  static final String OUTFIT_PREFIX = "outfit_";
  @javax.persistence.AttributeOverrides({
    @AttributeOverride(name = "type",  column = @Column(name = OUTFIT_PREFIX + "type",  nullable = false)),
    @AttributeOverride(name = "head",  column = @Column(name = OUTFIT_PREFIX + "head",  nullable = false)),
    @AttributeOverride(name = "body",  column = @Column(name = OUTFIT_PREFIX + "body",  nullable = false)),
    @AttributeOverride(name = "legs",  column = @Column(name = OUTFIT_PREFIX + "legs",  nullable = false)),
    @AttributeOverride(name = "feet",  column = @Column(name = OUTFIT_PREFIX + "feet",  nullable = false)),
    @AttributeOverride(name = "extra", column = @Column(name = OUTFIT_PREFIX + "extra", nullable = false)),
  }) @javax.persistence.Embeddable @Data @Accessors(chain = true)
  public static class CharacterOutfit {
    private Integer type = 0x80;
    private Integer head = 0x04;
    private Integer body = 0x03;
    private Integer legs = 0x02;
    private Integer feet = 0x01;
    private Integer extra = 0x00;
  }
  private CharacterOutfit outfit = new CharacterOutfit();

  private transient List<PlayerCharacterCondition> conditions;
  private transient PlayerCharacterSkull skull = PlayerCharacterSkull.NONE;

  public PlayerCharacterEntity(AccountEntity accountEntity,
      String name, PlayerCharacterVocation vocation, Position position) {
    this.setName(name).setPosition(position).setVocation(vocation)
      .setDirection(Direction.SOUTH).setAttributes(new Attributes())
        .setAccount(accountEntity);
  }

}
