package com.github.xmaiax.protocol.impl;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.xmaiax.errors.OTJException;
import com.github.xmaiax.errors.OTJException.CommonError;
import com.github.xmaiax.packet.Packet;
import com.github.xmaiax.protocol.TibiaProtocol;
import com.github.xmaiax.security.MD5Utils;
import com.github.xmaiax.structure.Account;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class LoadCharactersProtocol implements TibiaProtocol {

  public static int SKIP_CLIENT_UNUSED_INFO = 0x0c;

  @Value("${otserver.host}") private String host;
  @Value("${otserver.port}") private Integer port;
  @Value("${otserver.version}") private Integer version;
  @Value("${otserver.motd}") private String motd;

  public String formatClientVersion(Integer version) {
    return String.format(Locale.US, "%s", Float.valueOf(version) / 100.0f);
  }

  private void writeHostPort2Packet(Packet packet) {
    try {
      Arrays.stream(java.net.InetAddress.getByName(this.host).getHostAddress().split("[.]"))
        .forEach(ipPart -> packet.writeByte(Integer.valueOf(ipPart)));
      packet.writeInt16(this.port);
    }
    catch (UnknownHostException uhe) {
      log.error("Unable to reach host '{}': ", this.host, uhe);
      System.exit(-1);
    }
  }

  @Override
  public Packet executeProtocol(ByteBuffer buffer) throws OTJException {
    final OperatingSystem os = OperatingSystem.fromCode(Packet.readInt16(buffer));
    final Integer clientVersion = Packet.readInt16(buffer);
    Packet.skip(buffer, SKIP_CLIENT_UNUSED_INFO);
    final int accountNumber = Packet.readInt32(buffer);
    if(accountNumber < 1) throw new OTJException(CommonError.INSERT_ACCOUNT_NUMBER);
    final String password = Packet.readString(buffer);
    if(password == null || password.isEmpty())
      throw new OTJException(CommonError.INSERT_PASSWORD);
    if(!this.version.equals(clientVersion))
      throw new OTJException(3, String.format("Expected client %s, got client %s.",
        this.formatClientVersion(this.version), formatClientVersion(clientVersion)));
    log.info("Login attemp from account number '{}' [OS: {}]", accountNumber, os);

    //TODO: Usar accountNumber + password para carregar lista de personagens + dias de premmy
    final Calendar premiumExpiration = Calendar.getInstance();
    premiumExpiration.add(Calendar.DAY_OF_MONTH, 13);
    final Account account = new Account()
      .setAccountNumber(accountNumber)
      .setPasswordMD5(MD5Utils.getInstance().str2md5(password))
      .setPremiumExpiration(premiumExpiration)
      .setCharacters(Arrays.asList(new Account.CharacterOption[] {
        new Account.CharacterOption().setName("Maia").set_class("Necromancer"),
        new Account.CharacterOption().setName("Stefane").set_class("Wizard"),
      }));

    final Packet characterListPacket = new Packet();
    characterListPacket.writeByte(Packet.LOGIN_CODE_OK);
    characterListPacket.writeString(new TibiaProtocol.MOTD()
      .setMessage(this.motd).toString());
    characterListPacket.writeByte(Packet.CHARACTERS_LIST_START);
    if(account.getCharacters() != null) {
      characterListPacket.writeByte(account.getCharacters().size());
      account.getCharacters().forEach(ch -> {
        characterListPacket.writeString(ch.getName());
        characterListPacket.writeString(ch.get_class());
        this.writeHostPort2Packet(characterListPacket);
      });
    }
    int premiumDuration = 0;
    if(account.getPremiumExpiration() != null && account.getPremiumExpiration()
        .after(Calendar.getInstance()))
      premiumDuration = (int) TimeUnit.DAYS.convert(account.getPremiumExpiration()
        .getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS);
    return characterListPacket.writeInt16(premiumDuration);
  }

}
