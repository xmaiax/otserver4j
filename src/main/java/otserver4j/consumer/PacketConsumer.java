package otserver4j.consumer;

import java.io.IOException;
import java.util.Collections;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import otserver4j.configuration.AmqpConfiguration;
import otserver4j.converter.RawPacket;
import otserver4j.converter.PacketMessageConverter.PacketWrapper;
import otserver4j.converter.wrapper.LoadCharacterListPacketWrapper;
import otserver4j.service.LoginService;

@Component
public class PacketConsumer {

  private SessionManager sessionManager;
  private AmqpTemplate amqpTemplate;
  private LoginService loginService;

  public PacketConsumer(
      SessionManager sessionManager,
      AmqpTemplate amqpTemplate,
      LoginService loginService) {
    this.sessionManager = sessionManager;
    this.amqpTemplate = amqpTemplate;
    this.loginService = loginService;
  }

  @RabbitListener(queues = { AmqpConfiguration.PACKET_OUTPUT_QUEUE, })
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

  @RabbitListener(queues = { AmqpConfiguration.PACKET_INPUT_QUEUE, })
  public void inputListener(PacketWrapper packetObject) {
    if(packetObject instanceof LoadCharacterListPacketWrapper) {
      this.loginService.modifyCharacterListPacket((LoadCharacterListPacketWrapper) packetObject);
    }
    this.amqpTemplate.convertAndSend(AmqpConfiguration.PACKET_OUTPUT_QUEUE, packetObject);
  }

}
