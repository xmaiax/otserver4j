package otserver4j.action.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import otserver4j.configuration.AmqpConfiguration;
import otserver4j.converter.PacketType;
import otserver4j.converter.RawPacket;
import otserver4j.entity.PlayerCharacter;
import otserver4j.structure.Chat;
import otserver4j.structure.Position;

@org.springframework.stereotype.Component
public class TalkAction implements otserver4j.action.Action {

  @org.springframework.beans.factory.annotation.Autowired
  private otserver4j.action.EventQueue eventQueue;

  @Autowired private AmqpTemplate amqpTemplate;

  public static final Integer TALK_SERVER_CODE = 0xaa;
  
  public RawPacket speak(String name, Chat.ChatType chatType, Position position, String message) {
    return new RawPacket().writeByte(TALK_SERVER_CODE).writeString(name).writeByte(chatType.getCode())
      .writeInt16(position.getX()).writeInt16(position.getY()).writeByte(position.getZ())
      .writeString(Chat.ChatType.YELL.equals(chatType) ? message.toUpperCase() : message);
  }

  @Override public RawPacket execute(PacketType type, ByteBuffer buffer,
      SocketChannel channel, PlayerCharacter player) {
    final Chat.ChatType chatType = Chat.ChatType.fromCode(RawPacket.readByte(buffer));
    final String message = RawPacket.readString(buffer);
    this.eventQueue.addNewEvent(this.speak(player.getName(), chatType,
      player.getPosition(), message), player.getPosition());
    this.amqpTemplate.convertAndSend(AmqpConfiguration.PACKET_OUTPUT_QUEUE, message);
    return /*new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.YELLOW.getCode())
      .writeString(String.format("[%s] %s %s: %s",
        otserver4j.utils.DateFormatUtils.getInstance().timeNow(), player.getName(),
          Chat.ChatType.SAY.equals(chatType) ? "says" :
            Chat.ChatType.YELL.equals(chatType) ? "yells" : "whispers", message))*/ null;
  }

}
