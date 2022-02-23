package com.github.xmaiax.utils;

//TODO: Implementar lógica de cálculo de level e experiência
public class ExperienceUtils {
  private ExperienceUtils() { }
  private static final ExperienceUtils INSTANCE = new ExperienceUtils();
  public static ExperienceUtils getInstance() { return INSTANCE; }
  
  public Integer levelFromExp(Long experience) {
    return 1;
  }

  public long experienceQuantity(int level) {
    return 75;
  }

  public int nextLevelPercent(long experience) {
    return 50;
  }

}
