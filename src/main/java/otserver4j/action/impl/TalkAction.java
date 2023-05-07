package otserver4j.action.impl;

import otserver4j.packet.Packet;
import otserver4j.structure.Chat;

@org.springframework.stereotype.Component
public class TalkAction implements otserver4j.action.Action {

  @Override public Packet execute(
      otserver4j.packet.PacketType type,
      java.nio.ByteBuffer buffer,
      java.nio.channels.SocketChannel channel,
      otserver4j.structure.PlayerCharacter player) {
    final Chat.ChatType chatType = Chat.ChatType.fromCode(Packet.readByte(buffer));
    final String message = Packet.readString(buffer);
    return new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.YELLOW.getCode())
      .writeString(String.format("[%s] %s %s: %s",
        otserver4j.utils.DateFormatUtils.getInstance().timeNow(), player.getName(),
          Chat.ChatType.SAY.equals(chatType) ? "says" :
            Chat.ChatType.YELL.equals(chatType) ? "yells" : "whispers", message));
  }

}
