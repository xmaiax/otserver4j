package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum PlayerCharacterSlot {
  HEAD(0x01), NECK(0x02), BACKPACK(0x03), CHEST(0x04),
  RIGHT_HAND(0x05), LEFT_HAND(0x06), LEGS(0x07), RING(0x08),
  FEET(0x09), AMMUNITION(0x0a), INVALID(-1);
  private final Integer code;
  public static PlayerCharacterSlot fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(PlayerCharacterSlot.values()).stream()
      .filter(sl -> sl.getCode().equals(code)).findFirst().orElse(INVALID); }
}
