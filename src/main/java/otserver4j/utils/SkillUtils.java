package otserver4j.utils;

import otserver4j.entity.PlayerCharacterEntity.Skill;

public class SkillUtils {
  public static final SkillUtils INSTANCE = new SkillUtils();
  private SkillUtils() { }

  public Skill calculateSkillFromCount(final long count, final double factor) {
    return new Skill().setLevel(10).setPercent(0).setCount(count);
  }

}
