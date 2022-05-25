package otserver4j.protocol;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import otserver4j.exception.GenericException;
import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;

public interface Protocol {

  Packet execute(ByteBuffer buffer, SelectionKey key,
    SocketChannel channel, PacketType type) throws GenericException;

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
