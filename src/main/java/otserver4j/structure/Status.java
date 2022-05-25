package otserver4j.structure;

import lombok.Getter;

public interface Status {

  @Getter
  public static enum Skull {
    NONE(0x00), YELLOW(0x01), GREEN(0x02), WHITE(0x03), RED(0x04);
    private Integer code;
    Skull(Integer code) { this.code = code; }
    public static Skull fromCode(Integer code) {
      return java.util.Arrays.asList(Skull.values()).stream()
        .filter(d -> d.getCode().equals(code) || d == NONE).findFirst().get();
    }
  }

  @Getter
  public static enum Party {
    NONE(0x00), REQUESTED(0x02), ACCEPTED(0x03), LEADER(0x04);
    private Integer code;
    Party(Integer code) { this.code = code; }
    public static Party fromCode(Integer code) {
      return java.util.Arrays.asList(Party.values()).stream()
        .filter(d -> d.getCode().equals(code) || d == NONE).findFirst().get();
    }
  }

}
