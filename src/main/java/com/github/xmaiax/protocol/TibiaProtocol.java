package com.github.xmaiax.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.xmaiax.errors.OTJException;
import com.github.xmaiax.packet.Packet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public interface TibiaProtocol {

  Packet executeProtocol(ByteBuffer buffer) throws OTJException;

  @Getter
  public static enum OperatingSystem {
    UNIX_LIKE("Unix-like"), WINDOWS("Windows"), OTHER("Other");
    private String type;
    OperatingSystem(String type) { this.type = type; }
    @Override public String toString() { return this.type; }
    public static OperatingSystem fromCode(Integer code) {
      switch(code) {
        case 0x01: return UNIX_LIKE;
        case 0x02: return WINDOWS;
        default: return OTHER;
      }
    }
  }

  @Getter
  public static enum LoginRequestType {
    LOAD_CHARACTER_LIST(0x01),
    LOGIN_SUCCESS(0x0a),
    INVALID(-1);
    private Integer code;
    LoginRequestType(Integer code) {
      this.code = code;
    }
    @Override public String toString() { return this.name(); }
    public static LoginRequestType fromCode(Integer code) {
      return Arrays.asList(LoginRequestType.values()).stream()
        .filter(lrt -> lrt.getCode() == code || lrt == INVALID)
          .findFirst().get();
    }
  }

  @Getter @Setter
  @Accessors(chain = true)
  public class MOTD {
    private String message;
    private Byte code = 0x01;
    @Override public String toString() {
      return String.format("%d\n%s", this.code, this.message);
    }
  }

}
