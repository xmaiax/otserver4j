package otserver4j.converter.wrapper;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import otserver4j.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.converter.PacketType;
import otserver4j.converter.RawPacket;
import otserver4j.exception.AccountException;

@Accessors(chain = true) @Getter @Setter @ToString
public class LoadCharacterListPacketWrapper extends PacketWrapper {

  public static final Integer
    SKIP_LOGIN_UNUSED_INFO = 0x0c,
    LOGIN_CODE_OK = 0x14, LOGIN_CODE_NOK = 0x0a,
    CHARACTERS_LIST_START = 0x64;

  @Override protected PacketType getPacketType() { return PacketType.LOAD_CHARACTER_LIST; }

  @AllArgsConstructor @Getter
  private enum OperatingSystem {
    UNIX_LIKE("Unix-like"), WINDOWS("Windows"), OTHER("Other");
    private String type; @Override public String toString() { return this.type; }
    public static OperatingSystem fromCode(Integer code) {
      switch(code) {
        case 0x01: return UNIX_LIKE;
        case 0x02: return WINDOWS;
        default: return OTHER;
      }
    }
  }

  private OperatingSystem operatingSystem;
  private Integer clientVersion;
  private Integer accountNumber;
  private String password;

  @Override
  protected Object modifyFromBuffer(ByteBuffer byteBuffer, Integer size) {
    this.setOperatingSystem(OperatingSystem.fromCode(RawPacket.readInt16(byteBuffer)))
        .setClientVersion(RawPacket.readInt16(byteBuffer));
    RawPacket.skip(byteBuffer, SKIP_LOGIN_UNUSED_INFO);
    return this.setAccountNumber(RawPacket.readInt32(byteBuffer))
               .setPassword(RawPacket.readString(byteBuffer));
  }

  @Getter @Setter
  private class MOTD {
    public static final String DEFAULT_MOTD_MESSAGE = "Welcome!";
    private String message; private Byte code;
    public MOTD(String message) {
      this.message = message == null || message.isBlank() ? DEFAULT_MOTD_MESSAGE : message;
      this.code = 0x01;
    }
    @Override public String toString() {
      return String.format("%d\n%s", this.code, this.message);
    }
  }

  private Boolean failed;
  private String errorMessage;

  private String motd;
  private List<CharacterOption> characterOptions;
  private String host;
  private Integer port;
  private Calendar premiumExpiration;

  @Accessors(chain = true) @Data @ToString
  public static class CharacterOption {
    private String name;
    private String vocation;
  }

  private void writeHostPort(RawPacket packet) throws AccountException {
    try {
      Arrays.stream(InetAddress.getByName(this.host).getHostAddress()
        .split("[.]")).forEach(ipPart -> packet.writeByte(Integer.valueOf(ipPart)));
      packet.writeInt16(this.port);
    }
    catch(UnknownHostException uhe) {
      throw new AccountException(String.format("Unable to reach host '%s'.", this.host));
    }
  }

  @Override
  public RawPacket convertToRawPacket() {
    if(this.failed == null || this.failed) {
      return new RawPacket()
        .writeByte(LOGIN_CODE_NOK)
        .writeString(this.errorMessage);
    }
    final RawPacket rawPacket = new RawPacket()
      .writeByte(LOGIN_CODE_OK)
      .writeString(new MOTD(this.motd).toString())
      .writeByte(CHARACTERS_LIST_START)
      .writeByte(this.characterOptions.size());
    this.characterOptions.forEach(characterOption -> {
      rawPacket.writeString(characterOption.getName());
      rawPacket.writeString(characterOption.getVocation());
      this.writeHostPort(rawPacket);
    });
    return rawPacket.writeInt16(this.premiumExpiration.after(Calendar.getInstance()) ?
      (int) TimeUnit.DAYS.convert(this.premiumExpiration.getTimeInMillis() -
        Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS) :
          BigInteger.ZERO.intValue());
  }

  @Override
  public Boolean thenDisconnect() {
    return Boolean.TRUE;
  }

}
