package otserver4j.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface Status {

  @AllArgsConstructor @Getter
  public static enum Condition {
    POISONED(0b00000001), ON_FIRE(0b00000010), STRUCK_BY_ENERGY(0b00000100),
    DRUNK(0b00001000), MAGIC_SHIELD_BUFF(0b00010000), PARALYZED(0b00100000),
    HASTE_BUFF(0b01000000), IN_BATTLE(0b10000000);
    private Integer code;
    public static Integer getIconCodeFromStatuses(java.util.List<Condition> conditions) {
      return conditions.stream().mapToInt(condition -> condition.getCode()).sum();
    }
  }

  @AllArgsConstructor @Getter
  public static enum Skull {
    NONE(0x00), YELLOW(0x01), GREEN(0x02), WHITE(0x03), RED(0x04);
    private Integer code;
    public static Skull fromCode(Integer code) {
      return java.util.Arrays.asList(Skull.values()).stream()
        .filter(sk -> sk.getCode().equals(code) || NONE.equals(sk)).findFirst().get();
    }
  }

  @AllArgsConstructor @Getter
  public static enum Party {
    NONE(0x00), REQUESTED(0x02), ACCEPTED(0x03), LEADER(0x04);
    private Integer code;
    public static Party fromCode(Integer code) {
      return java.util.Arrays.asList(Party.values()).stream()
        .filter(pt -> pt.getCode().equals(code) || NONE.equals(pt)).findFirst().get();
    }
  }

}
