package com.github.xmaiax.protocol.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.xmaiax.errors.LoginException;
import com.github.xmaiax.packet.Packet;
import com.github.xmaiax.protocol.Protocol;
import com.github.xmaiax.structure.Player;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class LoginSuccessProtocol implements Protocol {

  @Value("${otserver.version}") private Integer version;

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key) throws LoginException {
    Packet.skip(buffer, 2);
    if(!this.version.equals(Packet.readInt16(buffer)))
      throw new LoginException("Wrong version number.");
    Packet.skip(buffer, 1);
    final Integer accountNumber = Packet.readInt32(buffer);
    final String selectedCharacterName = Packet.readString(buffer);
    final String password = Packet.readString(buffer);
    if(password == null || password.isBlank())
      throw new LoginException("Nice try, a**hole!");
    log.info("Successful login attemp from account number '{}': {}",
      accountNumber, selectedCharacterName);
    key.attach(new Player().setName(selectedCharacterName));
    return null; // Retornar SpawnProtocol.successfulLoginPacket
  }

}
