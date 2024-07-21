package otserver4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;

public class ClientFake {

  private static final Long ACCOUNT_NUMBER = 1L;
  private static final String PASSWORD = "1";
  private static final String CHARACTER_NAME = "Maia";
  private static final Integer PROTOCOL_VERSION = 760;
  
  public static void main(String[] args) throws IOException, InterruptedException {
    final SocketChannel clientSocket = SocketChannel.open(new InetSocketAddress("localhost", 7171));
    final Packet loginPacket = new Packet();
    loginPacket.writeByte(PacketType.LOGIN_SUCCESS.getCode());
    loginPacket.writeInt16(2);
    loginPacket.writeInt16(PROTOCOL_VERSION);
    loginPacket.writeByte(0x00);
    loginPacket.writeInt32(ACCOUNT_NUMBER);
    loginPacket.writeString(CHARACTER_NAME);
    loginPacket.writeString(PASSWORD);
    loginPacket.send(clientSocket);
    while(clientSocket.isConnected()) {
      final ByteBuffer serverResponse = ByteBuffer.allocate(Packet.MAX_SIZE);
      clientSocket.read(serverResponse);
      final String output = new String(serverResponse.array(), StandardCharsets.ISO_8859_1).trim();
      if(!output.isBlank()) {
        final ByteBuffer buffer = ByteBuffer.wrap(output.getBytes());
        final StringBuilder message = new StringBuilder();
        while(buffer.hasRemaining()) {
          message.append(String.format("0x%02X\t", Packet.readByte(buffer) /*Byte.toUnsignedInt(buffer.get())*/));
        }
        System.out.println(message.toString().trim());
      }
    }
  }
}
