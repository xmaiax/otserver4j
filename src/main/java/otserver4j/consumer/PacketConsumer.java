package otserver4j.consumer;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.Collections;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import otserver4j.Application;
import otserver4j.consumer.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.consumer.converter.RawPacket;
import otserver4j.consumer.converter.wrapper.LoadCharacterListPacketWrapper;
import otserver4j.tcp.SessionManager;

@Component
public class PacketConsumer {

  @Autowired private AmqpTemplate amqpTemplate;

  @RabbitListener(queues = { Application.PACKET_INPUT_QUEUE, })
  public void inputListener(PacketWrapper packetObject) throws IOException {
    if(packetObject instanceof LoadCharacterListPacketWrapper) {
      final LoadCharacterListPacketWrapper wrapper = (LoadCharacterListPacketWrapper) packetObject;
      final Calendar premiumExpiration = Calendar.getInstance();
      premiumExpiration.add(Calendar.DAY_OF_MONTH, 15);
      this.amqpTemplate.convertAndSend(Application.PACKET_OUTPUT_QUEUE, wrapper
        .setMotd("eae seu corno!")
        .setCharacterOptions(Collections.emptyList())
        .setHost("localhost")
        .setPort(7171)
        .setPremiumExpiration(premiumExpiration));
    }
  }

  @Autowired private SessionManager sessionManager;

  @RabbitListener(queues = { Application.PACKET_OUTPUT_QUEUE, })
  public void outputListener(PacketWrapper packetObject) throws IOException {
    final SocketChannel socketChannel = this.sessionManager.getSocketChannelFromPacketWrapper(packetObject);
    final RawPacket rawPacket = packetObject.convertToRawPacket();
    rawPacket.sendAndClose(socketChannel);
  }

}
