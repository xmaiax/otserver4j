package otserver4j.factory;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.entity.AccountEntity;
import otserver4j.exception.AccountException;
import otserver4j.packet.AbstractPacketFactory;
import otserver4j.service.LoginService;
import otserver4j.structure.PacketType;
import otserver4j.structure.RawPacket;

@Slf4j
@org.springframework.stereotype.Component
public class LoadCharacterListPacketFactory extends AbstractPacketFactory<
    otserver4j.factory.LoadCharacterListPacketFactory.LoadCharacterListPacketRequest,
    otserver4j.factory.LoadCharacterListPacketFactory.LoadCharacterListPacketResponse> {

  public static final Integer
    SKIP_LOGIN_UNUSED_INFO = 0x0c, CHARACTERS_LIST_START = 0x64,
    LOGIN_CODE_OK = 0x14, LOGIN_CODE_NOK = 0x0a;

  private final Integer version;
  private final String messageOfTheDay;
  private final String host;
  private final Integer port;

  private final LoginService loginService;

  public LoadCharacterListPacketFactory(
      @Value("${otserver.version}") Integer version,
      @Value("${otserver.motd}") String messageOfTheDay,
      @Value("${otserver.host}") String host,
      @Value("${otserver.port}") Integer port,
      LoginService loginService) {
    this.version = version;
    this.messageOfTheDay = messageOfTheDay;
    this.host = host; this.port = port;
    this.loginService = loginService;
  }

  @Override public PacketType getPacketType() { return PacketType.LOAD_CHARACTER_LIST; }
  @Override public boolean thenDisconnect() { return Boolean.TRUE; }

  @AllArgsConstructor @Getter private enum OperatingSystem {
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

  @Accessors(chain = true) @Getter @Setter
  public static class LoadCharacterListPacketRequest
      extends otserver4j.packet.AbstractPacketFactory.PacketRequest {
    private OperatingSystem operatingSystem;
    private Integer clientVersion;
    private Integer accountNumber;
    private String password;
  }

  @Override
  public LoadCharacterListPacketRequest newPacketRequest(java.nio.ByteBuffer byteBuffer, Integer packetSize) {
    final OperatingSystem operatingSystem = OperatingSystem.fromCode(RawPacket.readInt16(byteBuffer));
    final Integer clientVersion = RawPacket.readInt16(byteBuffer);
    RawPacket.skip(byteBuffer, SKIP_LOGIN_UNUSED_INFO);
    return new LoadCharacterListPacketRequest()
      .setOperatingSystem(operatingSystem).setClientVersion(clientVersion)
      .setAccountNumber(RawPacket.readInt32(byteBuffer))
      .setPassword(RawPacket.readString(byteBuffer));
  }

  @Accessors(chain = true) @Getter @Setter
  public static class CharacterOption {
    private String name;
    private String details;
    private String host;
    private Integer port;
  }

  @Accessors(chain = true) @Getter @Setter
  public static class LoadCharacterListPacketResponse
      extends otserver4j.packet.AbstractPacketFactory.PacketResponse {
    private String errorMessage;
    private String messageOfTheDay;
    private java.util.List<CharacterOption> characterOptions;
    private Integer premiumDaysLeft;
  }

  @Override
  public LoadCharacterListPacketResponse generatePacketResponse(LoadCharacterListPacketRequest request) {
    try {
      if(!this.version.equals(request.getClientVersion()))
        throw AccountException.WRONG_VERSION_NUMBER_EXCEPTION;
      final AccountEntity account = this.loginService.findAccountToLogin(
        request.getAccountNumber(), request.getPassword());
      return new LoadCharacterListPacketResponse()
        .setMessageOfTheDay(this.messageOfTheDay)
        .setPremiumDaysLeft(10)

        .setCharacterOptions(Collections.singletonList(new CharacterOption()
          .setName("Maia")
          .setDetails("teste")
          .setHost(this.host)
          .setPort(this.port)))

      ;
    }
    catch(AccountException aexc) {
      return new LoadCharacterListPacketResponse().setErrorMessage(aexc.getMessage());
    }
  }

  @Getter @lombok.Setter private class MOTD {
    public static final String DEFAULT_MOTD_MESSAGE = "Welcome!";
    private String message; private Byte code;
    public MOTD(String message) {
      this.message = message == null || message.isBlank() ? DEFAULT_MOTD_MESSAGE : message;
      this.code = 0x01;
    }
    @Override public String toString() { return String.format("%d\n%s", this.code, this.message); }
  }

  @Override
  public RawPacket generateRawPacketResponse(LoadCharacterListPacketResponse response) {
    if(response.getErrorMessage() != null && !response.getErrorMessage().isBlank())
      return new RawPacket().writeByte(LOGIN_CODE_NOK).writeString(response.getErrorMessage());
    final RawPacket rawPacket = new RawPacket().writeByte(LOGIN_CODE_OK)
      .writeString(new MOTD(response.getMessageOfTheDay()).toString())
      .writeByte(CHARACTERS_LIST_START);
    if(response.getCharacterOptions() == null || response.getCharacterOptions().isEmpty())
      rawPacket.writeByte(BigInteger.ZERO.intValue());
    else {
      rawPacket.writeByte(response.getCharacterOptions().size());
      response.getCharacterOptions().stream().forEach(characterOptions -> {
        rawPacket.writeString(characterOptions.getName())
                 .writeString(characterOptions.getDetails());
        try {
          java.util.Arrays.stream(java.net.InetAddress.getByName(characterOptions.getHost())
            .getHostAddress().split("[.]")).forEach(ipPart -> {
              rawPacket.writeByte(Integer.valueOf(ipPart)); });
          rawPacket.writeInt16(characterOptions.getPort());
        }
        catch(java.net.UnknownHostException uhe) {
          log.error("Unable to reach host '{}': ", characterOptions.getHost(), uhe);
        }
      });
    }
    return rawPacket.writeInt16(response.getPremiumDaysLeft() == null ||
        response.getPremiumDaysLeft() < BigInteger.ZERO.intValue() ?
      BigInteger.ZERO.intValue() : response.getPremiumDaysLeft());
  }

  @Override
  public Set<String> sessionsToSendFrom(String session) {
    return Collections.singleton(session);
  }

}
