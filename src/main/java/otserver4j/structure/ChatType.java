package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum ChatType {
  SAY(0x01), WHISPER(0x02), YELL(0x03),
  YELLOW_CHAT(0x05), REPORT_CHANNEL(0x06),
  REPORT_RESPONSE(0x07), REPORT(0x08),
  BROADCAST(0x09), PRIVATE(0x04), RED_CHANNEL(0x0a),
  PRIVATE_RED(0x0b), ORANGE_CHANNEL(0x0c),
  ANONYMOUS_RED(0x0d), MONSTER(0x10),
  SCREAMING_MONSTER(0x11), INVALID(-1);
  private final Integer code;
  public static ChatType fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(ChatType.values()).stream()
      .filter(ct -> ct.getCode().equals(code)).findFirst().orElse(INVALID);
  }
}
