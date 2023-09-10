package otserver4j.consumer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import otserver4j.configuration.AmqpQueueConfiguration;
import otserver4j.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper;
import otserver4j.service.LoginService;

@Component
public class PacketInputConsumer {

  private AmqpTemplate amqpTemplate;
  private LoginService loginService;

  public PacketInputConsumer(
      AmqpTemplate amqpTemplate,
      LoginService loginService) {
    this.amqpTemplate = amqpTemplate;
    this.loginService = loginService;
  }

  @RabbitListener(queues = { AmqpQueueConfiguration.PACKET_INPUT_QUEUE, })
  public void inputListener(PacketWrapper packetObject) {
    if(packetObject instanceof LoadCharacterListPacketWrapper) {
      this.loginService.modifyCharacterListPacket((LoadCharacterListPacketWrapper) packetObject);
    }
    this.amqpTemplate.convertAndSend(AmqpQueueConfiguration.PACKET_OUTPUT_QUEUE, packetObject);
  }

}
