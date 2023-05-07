package otserver4j.protocol;

public interface Protocol {
  otserver4j.packet.Packet execute(java.nio.ByteBuffer buffer, java.nio.channels.SelectionKey key,
    java.nio.channels.SocketChannel channel, otserver4j.structure.PlayerCharacter player,
    otserver4j.packet.PacketType type) throws otserver4j.exception.GenericException;
}
