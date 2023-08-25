package otserver4j.action.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import otserver4j.config.AMQPConfiguration;
import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;
import otserver4j.structure.Chat;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.Position;

@org.springframework.stereotype.Component
public class TalkAction implements otserver4j.action.Action {

  @org.springframework.beans.factory.annotation.Autowired
  private otserver4j.action.EventQueue eventQueue;

  @Autowired private AmqpTemplate amqpTemplate;

  public static final Integer TALK_SERVER_CODE = 0xaa;
  
  public Packet speak(String name, Chat.ChatType chatType, Position position, String message) {
    return new Packet().writeByte(TALK_SERVER_CODE).writeString(name).writeByte(chatType.getCode())
      .writeInt16(position.getX()).writeInt16(position.getY()).writeByte(position.getZ())
      .writeString(Chat.ChatType.YELL.equals(chatType) ? message.toUpperCase() : message);
  }

  @Override public Packet execute(PacketType type, ByteBuffer buffer,
      SocketChannel channel, PlayerCharacter player) {
    final Chat.ChatType chatType = Chat.ChatType.fromCode(Packet.readByte(buffer));
    final String message = Packet.readString(buffer);
    this.eventQueue.addNewEvent(this.speak(player.getName(), chatType,
      player.getPosition(), message), player.getPosition());
    
    this.amqpTemplate.convertAndSend(AMQPConfiguration.IN_GAME_ACTIONS_QUEUE, message);
    
    return /*new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.YELLOW.getCode())
      .writeString(String.format("[%s] %s %s: %s",
        otserver4j.utils.DateFormatUtils.getInstance().timeNow(), player.getName(),
          Chat.ChatType.SAY.equals(chatType) ? "says" :
            Chat.ChatType.YELL.equals(chatType) ? "yells" : "whispers", message))*/ null;
  }

}
