package otserver4j.structure;

import lombok.Getter;

public interface Status {

  @Getter
  public static enum Skull {
    NONE(0x00), WHITE(0x01), YELLOW(0x02),
    RED(0x03), INVALID(-1);
    private Integer code;
    Skull(Integer code) { this.code = code; }
    public static Skull fromCode(Integer code) {
      return java.util.Arrays.asList(Skull.values()).stream()
        .filter(d -> d.getCode().equals(code) || d == INVALID).findFirst().get();
    }
  }

  @Getter
  public static enum StatusShield {
    NONE(0x00), INVALID(-1);
    private Integer code;
    StatusShield(Integer code) { this.code = code; }
    public static StatusShield fromCode(Integer code) {
      return java.util.Arrays.asList(StatusShield.values()).stream()
        .filter(d -> d.getCode().equals(code) || d == INVALID).findFirst().get();
    }
  }

}
