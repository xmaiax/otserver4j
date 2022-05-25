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
import otserver4j.structure.Chat.ChatType;
import otserver4j.structure.Direction;
import otserver4j.structure.PlayerCharacter;
import otserver4j.utils.DateFormatUtils;

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
    return new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.DISCREET_STATUS.getCode())
      .writeString(" ");
  }

  private Packet addMessageToClientLog(PlayerCharacter player,
      ChatType chatType, String message) {
    return new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.YELLOW.getCode())
      .writeString(String.format("[%s] %s %s: %s",
        DateFormatUtils.getInstance().timeNow(), player.getName(),
          ChatType.SAY.equals(chatType) ? "says" :
            ChatType.YELL.equals(chatType) ? "yells" : "whispers", message));
  }

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key,
      SocketChannel channel, PacketType type) throws InGameException {
    Packet packet = null;
    final PlayerCharacter player = (PlayerCharacter) key.attachment();
    switch(type) {
      case TALK:
        packet = this.addMessageToClientLog(player,
          ChatType.fromCode(Packet.readByte(buffer)), Packet.readString(buffer));
        break;
      case TURN_NORTH:
        packet = this.snapback(channel, player.setDirection(Direction.NORTH));
        break;
      case MOVE_NORTH:
        //...
        packet = this.snapback(channel, player.setDirection(Direction.NORTH));
        break;
      case TURN_SOUTH:
        packet = this.snapback(channel, player.setDirection(Direction.SOUTH));
        break;
      case MOVE_SOUTH:
        //...
        packet = this.snapback(channel, player.setDirection(Direction.SOUTH));
        break;
      case TURN_EAST:
        packet = this.snapback(channel, player.setDirection(Direction.EAST));
        break;
      case MOVE_EAST:
        //...
        packet = this.snapback(channel, player.setDirection(Direction.EAST));
        break;
      case TURN_WEST:
        packet = this.snapback(channel, player.setDirection(Direction.WEST));
        break;
      case MOVE_WEST:
        //...
        packet = this.snapback(channel, player.setDirection(Direction.WEST));
        break;
      default:
        packet = this.snapback(channel, player);
        break;
    }
    return packet;
  }

}
