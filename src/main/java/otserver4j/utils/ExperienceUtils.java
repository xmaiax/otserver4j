package otserver4j.utils;

public class ExperienceUtils {
  public static final ExperienceUtils INSTANCE = new ExperienceUtils();
  private ExperienceUtils() { }

  public Integer calculateLevelFromExperience(final long experience) {
    return 4;
  }

  public Integer calculateNextLevelPercentFromExperience(final long experience) {
    return 50;
  }

}
