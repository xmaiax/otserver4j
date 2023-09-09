package otserver4j.consumer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import otserver4j.configuration.AmqpQueueConfiguration;
import otserver4j.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.converter.RawPacket;
import otserver4j.tcp.SessionManager;

@Component
public class PacketOutputConsumer {

  @Autowired private SessionManager sessionManager;

  @RabbitListener(queues = { AmqpQueueConfiguration.PACKET_OUTPUT_QUEUE, })
  public void outputListener(PacketWrapper packetObject) throws IOException {
    final SocketChannel socketChannel = this.sessionManager.getSocketChannelFromPacketWrapper(packetObject);
    final RawPacket rawPacket = packetObject.convertToRawPacket();
    rawPacket.sendAndClose(socketChannel);
  }

}
