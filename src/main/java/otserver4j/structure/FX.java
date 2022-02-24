package otserver4j.structure;

@lombok.Getter
public enum FX {
  SPAWN(0x0a);
  private Integer code;
  FX(Integer code) { this.code = code; }
  public static FX fromCode(Integer code) {
    return java.util.Arrays.asList(FX.values()).stream()
      .filter(d -> d.getCode().equals(code)).findFirst().get();
  }
}
