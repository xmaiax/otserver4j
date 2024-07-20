package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum MessageType {
  YELLOW(0x01), LIGHT_BLUE(0x04), EVENT(0x14), ORANGE(0x11), WARNING(0x02), INFO(0x16),
  IN_PROGRESS_EVENT(0x13), BLUE(0x18), RED(0x19), STATUS(0x15), DISCREET_STATUS(0x17), INVALID(-1);
  private final Integer code;
  public static MessageType fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(MessageType.values()).stream()
      .filter(mt -> mt.getCode().equals(code)).findFirst().orElse(INVALID); }
}
