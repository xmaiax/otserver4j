package otserver4j.protocol.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import otserver4j.exception.InGameException;
import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;
import otserver4j.protocol.Protocol;
import otserver4j.structure.Chat;
import otserver4j.structure.Direction;
import otserver4j.structure.PlayerCharacter;

@Component @Slf4j
public class InGameProtocol implements Protocol {

  private static final Integer SNAPBACK_CODE = 0xb5;

  private Packet snapback(SocketChannel channel, PlayerCharacter player) {
    final Packet packet = new Packet();
    packet.writeByte(SNAPBACK_CODE);
    packet.writeByte(player.getDirection().getCode());
    try { packet.send(channel); }
    catch (IOException ioex) {
      log.error("Tururuuuuu: {}", ioex.getMessage(), ioex);
    }
    return new Packet().writeByte(Packet.CODE_SYSTEM_MESSAGE)
      .writeByte(Chat.MessageType.DISCREET_STATUS.getCode())
      .writeString(" ");
  }

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key,
      SocketChannel channel, PacketType type) throws InGameException {
    final PlayerCharacter player = (PlayerCharacter) key.attachment();
    switch(type) {
      case MOVE_NORTH: player.setDirection(Direction.NORTH); break;
      case MOVE_SOUTH: player.setDirection(Direction.SOUTH); break;
      case MOVE_EAST:  player.setDirection(Direction.EAST); break;
      case MOVE_WEST:  player.setDirection(Direction.WEST); break;
      default: break;
    }
    return this.snapback(channel, player);
  }

}
