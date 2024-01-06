package otserver4j.entity;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
  @JoinColumn(name = "accountNumber", nullable = false)
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

  public static enum PlayerSkillType {
    MAGIC, FIST, CLUB, SWORD, AXE, DISTANCE, SHIELD, FISHING,
  }

  @Data @Accessors(chain = true)
  public static class Skill {
    private Integer level;
    private Integer percent;
  }

  @JsonIgnore @Column(nullable = true) private Long totalManaSpent;
  @JsonIgnore public Integer getMagicLevel() { return 0; }

  @JsonIgnore @Column(nullable = true) private Long fistHitCount;
  @JsonIgnore public Skill getFistSkill() { return null; }

  @JsonIgnore @Column(nullable = true) private Long clubHitCount;
  @JsonIgnore public Skill getClubSkill() { return null; }

  @JsonIgnore @Column(nullable = true) private Long swordHitCount;
  @JsonIgnore public Skill getSwordSkill() { return null; }

  @JsonIgnore @Column(nullable = true) private Long axeHitCount;
  @JsonIgnore public Skill axeSkill() { return null; }

  @JsonIgnore @Column(nullable = true) private Long distanceHitCount;
  @JsonIgnore public Skill distanceSkill() { return null; }

  @JsonIgnore @Column(nullable = true) private Long blockedHitCount;
  @JsonIgnore public Skill shieldSkill() { return null; }

  @JsonIgnore @Column(nullable = true) private Long fishingTries;
  @JsonIgnore public Skill fishingSkill() { return null; }

  static final String OUTFIT_PREFIX = "outfit_";
  @javax.persistence.AttributeOverrides({
    @AttributeOverride(name = "type",  column = @Column(name = OUTFIT_PREFIX + "type",  nullable = true)),
    @AttributeOverride(name = "head",  column = @Column(name = OUTFIT_PREFIX + "head",  nullable = true)),
    @AttributeOverride(name = "body",  column = @Column(name = OUTFIT_PREFIX + "body",  nullable = true)),
    @AttributeOverride(name = "legs",  column = @Column(name = OUTFIT_PREFIX + "legs",  nullable = true)),
    @AttributeOverride(name = "feet",  column = @Column(name = OUTFIT_PREFIX + "feet",  nullable = true)),
    @AttributeOverride(name = "extra", column = @Column(name = OUTFIT_PREFIX + "extra", nullable = true)),
  }) @javax.persistence.Embeddable @Data @Accessors(chain = true)
  public static class CharacterOutfit {
    private Integer type;
    private Integer head;
    private Integer body;
    private Integer legs;
    private Integer feet;
    private Integer extra;
  }
  private CharacterOutfit outfit;

  private transient List<PlayerCharacterCondition> conditions;
  private transient PlayerCharacterSkull skull;

  public PlayerCharacterEntity(AccountEntity accountEntity,
      String name, PlayerCharacterVocation vocation, Position position) {
    this.setName(name).setPosition(position).setVocation(vocation)
      .setDirection(Direction.SOUTH).setExperience(ZERO.longValue())
      .setAttributes(new Attributes()).setAccount(accountEntity);
  }

}
