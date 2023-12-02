package otserver4j.structure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter public enum PlayerCharacterClass {

 // Class       Code  Name            HP   HP/Lvl   MP  MP/Lvl   Cap  Cap/Lvl  Soul
    ROOKIE     (0x00, "Rookie",      100,       5,  25,      5,  250,       5,  100),
    NECROMANCER(0x01, "Necromancer", 100,      10,  50,     20,  300,      10,  150),
    WARRIOR    (0x02, "Warrior",     200,      25,  20,      5,  450,      15,  100),
    MONK       (0x03, "Monk",        175,      20,  25,     10,  400,      10,  125),
    MAGE       (0X04, "Mage",        125,       5,  75,     25,  250,       5,  150);

  private final Integer code;
  private final String _name;
  private final Integer lifeBase;
  private final Integer lifePerLevel;
  private final Integer manaBase;
  private final Integer manaPerLevel;
  private final Integer capBase;
  private final Integer capPerLevel;
  private final Integer soulBase;

  @Override public String toString() { return this._name; }

}
