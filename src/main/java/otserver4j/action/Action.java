package otserver4j.action;

public interface Action {
  otserver4j.packet.Packet execute(otserver4j.packet.PacketType type, java.nio.ByteBuffer buffer,
    java.nio.channels.SocketChannel channel, otserver4j.structure.PlayerCharacter player);
}
