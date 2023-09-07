package otserver4j.action;

public interface Action {
  otserver4j.consumer.converter.RawPacket execute(otserver4j.consumer.converter.PacketType type, java.nio.ByteBuffer buffer,
    java.nio.channels.SocketChannel channel, otserver4j.structure.PlayerCharacter player);
}
