package otserver4j.action;

public interface Action {
  otserver4j.converter.RawPacket execute(otserver4j.converter.PacketType type, java.nio.ByteBuffer buffer,
    java.nio.channels.SocketChannel channel, otserver4j.entity.PlayerCharacter player);
}
