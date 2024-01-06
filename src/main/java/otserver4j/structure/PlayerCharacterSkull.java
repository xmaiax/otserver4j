package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum PlayerCharacterSkull {
  NONE(0x00), YELLOW(0x01), GREEN(0x02), WHITE(0x03), RED(0x04);
  private final Integer code;
  public static PlayerCharacterSkull fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(PlayerCharacterSkull.values()).stream()
      .filter(sk -> sk.getCode().equals(code)).findFirst().orElse(NONE); }
}
