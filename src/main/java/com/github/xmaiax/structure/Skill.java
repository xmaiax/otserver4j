package com.github.xmaiax.structure;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true)
public class Skill {
  public static enum SkillType {
    MAGIC, FIST, CLUB, SWORD,
    AXE, DISTANCE, SHIELD, FISHING
  }
  private SkillType type;
  private byte level = 0;
  private byte percent = 0;
}
