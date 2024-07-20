package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum PlayerCharacterVocation {

  /*Class       Code  Name            HP   HP/Lvl   MP  MP/Lvl   Cap  Cap/Lvl  Soul  Magic   Fist   Club   Swrd    Axe  Dstnc   Shld   Fish */
    NO_CLASS   (0x00, "No class",    100,       5,  20,      5,  250,       5,  100, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d),
    NECROMANCER(0x01, "Necromancer", 100,      10,  50,     20,  300,      10,  150, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d),
    WARRIOR    (0x02, "Warrior",     200,      25,  20,      5,  450,      15,  100, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d),
    MONK       (0x03, "Monk",        175,      20,  25,     10,  400,      10,  125, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d),
    MAGE       (0X04, "Mage",        125,       5,  75,     25,  250,       5,  150, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d, 1.00d);

  @Override public String toString() { return this._name; }
  public static PlayerCharacterVocation fromCode(final Integer vocationCode) {
    return vocationCode == null ? null : java.util.Arrays.asList(PlayerCharacterVocation.values())
      .stream().filter(v -> v.getCode().equals(vocationCode)).findFirst().orElse(
        PlayerCharacterVocation.values()[java.math.BigInteger.ZERO.intValue()]); }

  private final Integer code;
  private final String _name;
  private final Integer lifeBase;
  private final Integer lifePerLevel;
  private final Integer manaBase;
  private final Integer manaPerLevel;
  private final Integer capacityBase;
  private final Integer capacityPerLevel;
  private final Integer soul;
  private final Double magicSkillFactor;
  private final Double fistSkillFactor;
  private final Double clubSkillFactor;
  private final Double swordSkillFactor;
  private final Double axeSkillFactor;
  private final Double distanceSkillFactor;
  private final Double shieldingSkillFactor;
  private final Double fishingSkillFactor;

}
