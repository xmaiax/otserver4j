package otserver4j.factory;

import static java.math.BigInteger.ZERO;

import java.io.IOException;

import static java.math.BigInteger.ONE;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.entity.AccountEntity;
import otserver4j.exception.AccountException;
import otserver4j.repository.SessionManager;
import otserver4j.service.AbstractPacketFactory;
import otserver4j.service.LoginService;
import otserver4j.structure.PacketType;
import otserver4j.structure.RawPacket;

@Slf4j @Component public class LoadCharacterListPacketFactory extends AbstractPacketFactory<
    otserver4j.factory.LoadCharacterListPacketFactory.LoadCharacterListPacketRequest,
    otserver4j.factory.LoadCharacterListPacketFactory.LoadCharacterListPacketResponse> {

  public static final Integer
    SKIP_LOGIN_UNUSED_INFO = 0x0c, CHARACTERS_LIST_START = 0x64,
    LOGIN_CODE_OK = 0x14, LOGIN_CODE_NOK = 0x0a;

  private final Integer version;
  private final String messageOfTheDay;

  private final LoginService loginService;
  private final SessionManager sessionManager;

  public LoadCharacterListPacketFactory(LoginService loginService, SessionManager sessionManager,
      @Value("${otserver.version}") Integer version,
      @Value("${otserver.motd}") String messageOfTheDay) {
    this.loginService = loginService;
    this.sessionManager = sessionManager;
    this.version = version;
    this.messageOfTheDay = messageOfTheDay;
  }

  @Override public PacketType getPacketType() { return PacketType.LOAD_CHARACTER_LIST; }
  @Override public boolean thenDisconnect() { return Boolean.TRUE; }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter private enum OperatingSystem {
    UNIX_LIKE("Unix-like"), WINDOWS("Windows"), OTHER("Other");
    private final String type; @Override public String toString() { return this.type; }
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
      extends otserver4j.service.AbstractPacketFactory.PacketRequest {
    private OperatingSystem operatingSystem;
    private Integer clientVersion;
    private Integer accountNumber;
    private String password;
  }

  @Override public LoadCharacterListPacketRequest newPacketRequest(ByteBuffer byteBuffer, Integer packetSize) {
    final OperatingSystem operatingSystem = OperatingSystem.fromCode(RawPacket.readInt16(byteBuffer));
    final Integer clientVersion = RawPacket.readInt16(byteBuffer);
    RawPacket.skip(byteBuffer, SKIP_LOGIN_UNUSED_INFO);
    return new LoadCharacterListPacketRequest()
      .setOperatingSystem(operatingSystem).setClientVersion(clientVersion)
      .setAccountNumber(RawPacket.readInt32(byteBuffer))
      .setPassword(RawPacket.readString(byteBuffer));
  }

  @Accessors(chain = true) @Getter @Setter public static class CharacterOption {
    private String name;
    private String details;
    private String host;
    private Integer port;
  }

  @Accessors(chain = true) @Getter @Setter public static class LoadCharacterListPacketResponse
      extends otserver4j.service.AbstractPacketFactory.PacketResponse {
    private String errorMessage;
    private String messageOfTheDay;
    private List<CharacterOption> characterOptions;
    private Integer premiumDaysLeft;
  }

  @Override
  public LoadCharacterListPacketResponse generatePacketResponse(LoadCharacterListPacketRequest request) {
    try {
      if(!this.version.equals(request.getClientVersion()))
        throw AccountException.WRONG_VERSION_NUMBER_EXCEPTION;
      final AccountEntity account = this.loginService.findAccountToLogin(
        request.getAccountNumber(), request.getPassword());
      final SocketChannel socketChannel = this.sessionManager.getSocketChannelFromSession(request.getSession());
      return new LoadCharacterListPacketResponse()
        .setMessageOfTheDay(this.messageOfTheDay)
        .setPremiumDaysLeft(10)
        .setCharacterOptions(account.getCharacterList().stream().map(pc ->  {
          try {
            return new CharacterOption().setName(pc.getName()).setDetails(pc.getVocation().toString())
              .setHost(socketChannel.getRemoteAddress().toString().split("/")[ZERO.intValue()])
              .setPort(Integer.parseInt(socketChannel.getLocalAddress().toString().split(":")[ONE.intValue()]));
          }
          catch(IOException | NumberFormatException e) {
            e.printStackTrace();
          }
          return null;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList()));
    }
    catch(AccountException accexc) {
      return new LoadCharacterListPacketResponse().setErrorMessage(accexc.getMessage());
    }
  }

  @Getter @Setter private class MOTD {
    public static final String DEFAULT_MOTD_MESSAGE = "Welcome!";
    private String message; private Integer code;
    public MOTD(String message, Integer code) {
      this.message = message == null || message.isBlank() ? DEFAULT_MOTD_MESSAGE : message;
      this.code = code;
    }
    public MOTD(String message) { this(message, ONE.intValue()); }
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
      rawPacket.writeByte(ZERO.intValue());
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
        response.getPremiumDaysLeft() < ZERO.intValue() ?
      ZERO.intValue() : response.getPremiumDaysLeft());
  }

  @Override public Set<String> sessionsToSendFrom(String session) {
    return Collections.singleton(session); }

}
