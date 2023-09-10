package otserver4j.consumer;

import java.io.IOException;
import java.util.Collections;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import otserver4j.configuration.AmqpQueueConfiguration;
import otserver4j.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.converter.RawPacket;
import otserver4j.tcp.SessionManager;

@Component
public class PacketOutputConsumer {

  private SessionManager sessionManager;

  public PacketOutputConsumer(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @RabbitListener(queues = { AmqpQueueConfiguration.PACKET_OUTPUT_QUEUE, })
  public void outputListener(PacketWrapper packetWrapper) {
    if(packetWrapper.getToSessions() == null)
      packetWrapper.setToSessions(Collections.singletonList(packetWrapper.getFromSession()));
    final RawPacket rawPacket = packetWrapper.convertToRawPacket();
    packetWrapper.getToSessions().stream().map(session -> this.sessionManager
        .getSocketChannelFromSession(session)).forEach(socketChannel -> {
      try {
        rawPacket.send(socketChannel);
        if(packetWrapper.thenDisconnect()) socketChannel.close();
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    });
  }

}
