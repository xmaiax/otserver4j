package otserver4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import otserver4j.converter.PacketType;
import otserver4j.converter.RawPacket;

public class ClientFake {

  private static final Long ACCOUNT_NUMBER = 2L;
  private static final String PASSWORD = "2";
  private static final String CHARACTER_NAME = "Stefane";
  private static final Integer PROTOCOL_VERSION = 760;
  
  public static void main(String[] args) throws IOException, InterruptedException {
    final SocketChannel clientSocket = SocketChannel.open(new InetSocketAddress("localhost", 7171));
    final RawPacket loginPacket = new RawPacket();
    loginPacket.writeByte(PacketType.LOGIN_SUCCESS.getCode());
    loginPacket.writeInt16(2);
    loginPacket.writeInt16(PROTOCOL_VERSION);
    loginPacket.writeByte(0x00);
    loginPacket.writeInt32(ACCOUNT_NUMBER);
    loginPacket.writeString(CHARACTER_NAME);
    loginPacket.writeString(PASSWORD);
    loginPacket.send(clientSocket);
    while(clientSocket.isConnected()) {
      final ByteBuffer serverResponse = ByteBuffer.allocate(RawPacket.MAX_SIZE);
      clientSocket.read(serverResponse);
      final String output = new String(serverResponse.array()).trim();
      if(!output.isBlank()) {
        final ByteBuffer buffer = ByteBuffer.wrap(output.getBytes());
        final StringBuilder message = new StringBuilder();
        while(buffer.hasRemaining()) {
          message.append(String.format("%d\t", buffer.get() /*Byte.toUnsignedInt(buffer.get())*/));
        }
        System.out.println(message.toString().trim());
      }
    }
  }
}
