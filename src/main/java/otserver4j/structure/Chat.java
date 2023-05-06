package otserver4j.structure;

import lombok.Getter;

public interface Chat {

  @Getter
  public static enum MessageType {
    YELLOW(0x01), LIGHT_BLUE(0x04),
    EVENT(0x14), ORANGE(0x11), WARNING(0x02),
    INFO(0x16), IN_PROGRESS_EVENT(0x13),
    BLUE(0x18), RED(0x19), STATUS(0x15),
    DISCREET_STATUS(0x17), INVALID(-1);
    private Integer code;
    MessageType(Integer code) { this.code = code; }
    public static MessageType fromCode(Integer code) {
      return java.util.Arrays.asList(MessageType.values()).stream()
        .filter(mt -> mt.getCode().equals(code) || INVALID.equals(mt)).findFirst().get();
    }
  }

  @Getter
  public static enum Channel {
    CLAN("Clan", 0x00, Boolean.FALSE, Boolean.FALSE),
    REPORT("Report", 0x03, Boolean.FALSE, Boolean.TRUE),
    CHAT("Chat", 0x04, Boolean.TRUE, Boolean.FALSE),
    TRADE("Trade", 0x05, Boolean.TRUE, Boolean.FALSE),
    RL_CHAT("RL Chat", 0x06, Boolean.TRUE, Boolean.FALSE),
    HELP("Help", 0x07, Boolean.TRUE, Boolean.FALSE),
    DEVS("Maintainer", 0x08, Boolean.FALSE, Boolean.TRUE),
    TUTOR("Tutor", 0x09, Boolean.FALSE, Boolean.TRUE),
    GM("GM", 0x10, Boolean.FALSE, Boolean.TRUE),
    PRIVATE("Private", 0xff, Boolean.FALSE, Boolean.FALSE),
    INVALID(null, -1, Boolean.FALSE, Boolean.FALSE);
    private String identifier; private Integer code;
    private Boolean isPublic, isSpecial;
    Channel(String identifier, Integer code, Boolean isPublic, Boolean isSpecial) {
      this.identifier = identifier; this.code = code;
      this.isPublic = isPublic; this.isSpecial = isSpecial;
    }
    public static Channel fromCode(Integer code) {
      return java.util.Arrays.asList(Channel.values()).stream()
        .filter(ch -> ch.getCode().equals(code) || INVALID.equals(ch)).findFirst().get();
    }
  }

  @Getter
  public static enum ChatType {
    SAY(0x01), WHISPER(0x02), YELL(0x03),
    YELLOW_CHAT(0x05), REPORT_CHANNEL(0x06),
    REPORT_RESPONSE(0x07), REPORT(0x08),
    BROADCAST(0x09), PRIVATE(0x04), RED_CHANNEL(0x0a),
    PRIVATE_RED(0x0b), ORANGE_CHANNEL(0x0c),
    ANONYMOUS_RED(0x0d), MONSTER(0x10),
    SCREAMING_MONSTER(0x11), INVALID(-1);
    private Integer code;
    ChatType(Integer code) { this.code = code; }
    public static ChatType fromCode(Integer code) {
      return java.util.Arrays.asList(ChatType.values()).stream()
        .filter(ct -> ct.getCode().equals(code) || INVALID.equals(ct)).findFirst().get();
    }
  }

}
