package otserver4j.consumer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import otserver4j.configuration.AmqpQueueConfiguration;
import otserver4j.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper;

@Component
public class PacketInputConsumer {

  @Autowired private AmqpTemplate amqpTemplate;

  @RabbitListener(queues = { AmqpQueueConfiguration.PACKET_INPUT_QUEUE, })
  public void inputListener(PacketWrapper packetObject) throws IOException {
    if(packetObject instanceof LoadCharacterListPacketWrapper) {
      final LoadCharacterListPacketWrapper wrapper = (LoadCharacterListPacketWrapper) packetObject;
      final Calendar premiumExpiration = Calendar.getInstance();
      premiumExpiration.add(Calendar.DAY_OF_MONTH, 15);
      this.amqpTemplate.convertAndSend(AmqpQueueConfiguration.PACKET_OUTPUT_QUEUE, wrapper
        .setMotd("eae seu corno!")
        .setCharacterOptions(Collections.emptyList())
        .setHost("localhost")
        .setPort(7171)
        .setPremiumExpiration(premiumExpiration));
    }
  }

}
