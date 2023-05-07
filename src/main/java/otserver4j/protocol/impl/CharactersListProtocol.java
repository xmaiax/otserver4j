package otserver4j.protocol.impl;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;
import otserver4j.exception.LoginException;
import otserver4j.packet.Packet;

@lombok.extern.slf4j.Slf4j @org.springframework.stereotype.Component
public class CharactersListProtocol implements otserver4j.protocol.Protocol {

  public static Integer SKIP_CLIENT_UNUSED_INFO = 0x0c;

  @lombok.AllArgsConstructor @Getter private enum OperatingSystem {
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

  @Getter @lombok.Setter private class MOTD {
    public static final String DEFAULT_MOTD_MESSAGE = "Welcome!";
    private String message; private Byte code;
    public MOTD(String message) {
      this.message = message == null || message.isBlank() ? DEFAULT_MOTD_MESSAGE : message;
      this.code = 0x01;
    }
    @Override public String toString() { return String.format("%d\n%s", this.code, this.message); }
  }

  @org.springframework.beans.factory.annotation.Autowired
  private otserver4j.service.AccountService accService;

  @Value("${otserver.host}") private String host;
  @Value("${otserver.port}") private Integer port;
  @Value("${otserver.version}") private Integer version;
  @Value("${otserver.motd}") private String motd;

  public String formatClientVersion(Integer version) {
    return String.format(java.util.Locale.US, "%s", Float.valueOf(version) / 100.0f); }

  private void writeHostPort2Packet(Packet packet) throws LoginException {
    try {
      java.util.Arrays.stream(java.net.InetAddress.getByName(this.host).getHostAddress().split("[.]"))
        .forEach(ipPart -> packet.writeByte(Integer.valueOf(ipPart)));
      packet.writeInt16(this.port);
    }
    catch(java.net.UnknownHostException uhe) {
      log.error("Unable to reach host '{}': ", this.host, uhe);
      throw new LoginException(String.format("Unable to reach host '%s'.", this.host));
    }
  }

  @Override
  public Packet execute(java.nio.ByteBuffer buffer, java.nio.channels.SelectionKey key,
      java.nio.channels.SocketChannel channel, otserver4j.structure.PlayerCharacter _null,
      otserver4j.packet.PacketType type) throws LoginException {
    final OperatingSystem os = OperatingSystem.fromCode(Packet.readInt16(buffer));
    final Integer clientVersion = Packet.readInt16(buffer);
    Packet.skip(buffer, SKIP_CLIENT_UNUSED_INFO);
    final Integer accountNumber = Packet.readInt32(buffer);
    final String password = Packet.readString(buffer);
    if(!this.version.equals(clientVersion))
      throw new LoginException(String.format("Expected client %s, got client %s.",
        this.formatClientVersion(this.version), formatClientVersion(clientVersion)));
    final otserver4j.structure.Account account = this.accService.findAccount(accountNumber, password);
    log.info("Login attemp from account number '{}' [OS: {}]", accountNumber, os);
    final Packet characterListPacket = new Packet().writeByte(Packet.LOGIN_CODE_OK)
      .writeString(new MOTD(this.motd).toString())
      .writeByte(Packet.CHARACTERS_LIST_START);
    if(account.getCharacters() != null) {
      characterListPacket.writeByte(account.getCharacters().size());
      account.getCharacters().forEach(ch -> {
        characterListPacket.writeString(ch.getName());
        characterListPacket.writeString(ch.getProfession());
        this.writeHostPort2Packet(characterListPacket);
      });
    }
    else {
      //FIXME Lançar exception sobre lista de personagens inválida.
    }
    return characterListPacket.writeInt16(
        account.getPremiumExpiration() != null && account.getPremiumExpiration()
      .after(Calendar.getInstance()) ? (int) TimeUnit.DAYS.convert(account.getPremiumExpiration()
        .getTimeInMillis() - Calendar.getInstance().getTimeInMillis(),
          TimeUnit.MILLISECONDS) : java.math.BigInteger.ZERO.intValue());
  }

}
