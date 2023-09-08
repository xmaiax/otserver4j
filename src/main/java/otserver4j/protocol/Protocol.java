package otserver4j.protocol;

public interface Protocol {
  otserver4j.converter.RawPacket execute(
    java.nio.ByteBuffer buffer,
    java.nio.channels.SocketChannel socketChannel,
    otserver4j.structure.PlayerCharacter player,
    otserver4j.converter.PacketType type) throws otserver4j.exception.GenericException;
}
