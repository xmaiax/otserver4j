package otserver4j.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.configuration.AmqpConfiguration;
import otserver4j.exception.GenericException;
import otserver4j.packet.AbstractPacketFactory.PacketRequest;
import otserver4j.packet.AbstractPacketFactory.PacketResponse;
import otserver4j.repository.SessionManager;
import otserver4j.structure.PacketType;
import otserver4j.structure.RawPacket;

@Slf4j @Component
public class PacketMessageConverter implements MessageConverter {

  @Data @Accessors(chain = true)
  public static class RawPacketAmqpMessage {
    private Integer packetSize;
    private PacketType packetType;
    private ByteBuffer buffer;
    private String session;
  }

  private final ObjectMapper objectMapper;
  private final Map<String, AbstractPacketFactory<?, ?>> packetFactories;
  private final SessionManager sessionManager;
  
  public PacketMessageConverter(ObjectMapper objectMapper, 
      Map<String, AbstractPacketFactory<?, ?>> packetFactories,
      SessionManager sessionManager) {
    this.objectMapper = objectMapper;
    this.packetFactories = packetFactories;
    this.sessionManager = sessionManager;
  }

  public static class InvalidPacketTypeException extends GenericException {
    private static final long serialVersionUID = -1L;
    public InvalidPacketTypeException() { super("Invalid packet type."); }
  }

  public static class NoPacketFactoryFoundException extends GenericException {
    private static final long serialVersionUID = -1L;
    public NoPacketFactoryFoundException(PacketType packetType) {
      super(String.format("No packet factory of type '%s' found.", packetType));
    }
  }

  private AbstractPacketFactory<?, ?> getPacketFactoryFromPacketType(PacketType packetType) {
    if(packetType == null || PacketType.INVALID.equals(packetType))
      throw new InvalidPacketTypeException();
    final Optional<AbstractPacketFactory<?, ?>> packetFactory = this.packetFactories.values().stream()
      .filter(pf -> pf.getPacketType().equals(packetType)).findFirst();
    if(packetFactory.isEmpty()) throw new NoPacketFactoryFoundException(packetType);
    return packetFactory.get();
  }

  @Override
  public Message toMessage(Object object, MessageProperties messageProperties)
      throws MessageConversionException {
    if(object instanceof RawPacketAmqpMessage) {
      final RawPacketAmqpMessage rawPacketAmqpMessage = (RawPacketAmqpMessage) object;
      messageProperties.setType(rawPacketAmqpMessage.getPacketType().name());
      try {
        final AbstractPacketFactory<?, ?> packetFactory = this
          .getPacketFactoryFromPacketType(rawPacketAmqpMessage.getPacketType());
        final PacketRequest packetRequest = packetFactory.newPacketRequest(
          rawPacketAmqpMessage.getBuffer(), rawPacketAmqpMessage.getPacketSize());
        if(packetRequest == null) 
          throw new IllegalStateException("Invalid packet request.");
        packetFactory.addSessionAndType((PacketRequest) packetRequest,
          rawPacketAmqpMessage.getSession(), rawPacketAmqpMessage.getPacketType());
        return new Message(this.objectMapper.writeValueAsBytes(packetRequest), messageProperties);
      }
      catch(IOException ioex) {
        log.error("Unable to parse raw packet to packet request: {}", ioex.getMessage(), ioex);
        throw new MessageConversionException(ioex.getMessage(), ioex);
      }
    }
    if(object instanceof PacketRequest) {
      try {
        final PacketRequest packetRequest = (PacketRequest) object;
        messageProperties.setType(packetRequest.getPacketType().name());
        final AbstractPacketFactory<?, ?> packetFactory = this
          .getPacketFactoryFromPacketType(packetRequest.getPacketType());
        return new Message(this.objectMapper.writeValueAsBytes(packetFactory
          .convertObjectRequestToCustomPacketResponse(packetRequest)), messageProperties);
      }
      catch(IOException ioex) {
        log.error("Unable to parse packet request to packet response: {}", ioex.getMessage(), ioex);
        throw new MessageConversionException(ioex.getMessage(), ioex);
      }
    }
    else {
      log.error("Unknown instance type: {}", object.getClass().getName());
      throw new MessageConversionException(
        String.format("Unknown instance type: %s", object.getClass()));
    }
  }

  private Class<?> getClassToConvertFromQueueName(
      AbstractPacketFactory<?, ?> packetFactory, String queueName) throws IOException {
    switch(queueName) {
      case AmqpConfiguration.PACKET_INPUT_QUEUE: return packetFactory.getRequestClass();
      case AmqpConfiguration.PACKET_OUTPUT_QUEUE: return packetFactory.getResponseClass();
      default: throw new IOException(String.format(
        "Attemp to parse message from unknown queue '%s'.", queueName));
    }
  }

  @Override
  public Object fromMessage(Message message) throws MessageConversionException {
    try {
      final AbstractPacketFactory<?, ?> packetFactory = this.getPacketFactoryFromPacketType(
        PacketType.valueOf(message.getMessageProperties().getType()));
      return this.objectMapper.readValue(message.getBody(), this.getClassToConvertFromQueueName(
        packetFactory, message.getMessageProperties().getConsumerQueue()));
    }
    catch(IOException ioex) {
      log.error("Failed to convert AMQP message to Object: {}", ioex.getMessage(), ioex);
      throw new MessageConversionException(ioex.getMessage(), ioex);
    }
  }

  @RabbitListener(queues = { AmqpConfiguration.PACKET_OUTPUT_QUEUE, })
  public void outputListener(PacketResponse packetResponse) {
    if(packetResponse == null || packetResponse.getPacketType() == null ||
       packetResponse.getSession() == null || packetResponse.getSession().isBlank())
      throw new IllegalStateException("Invalid packet response.");
    final AbstractPacketFactory<?, ?> packetFactory = this
      .getPacketFactoryFromPacketType(packetResponse.getPacketType());
    final RawPacket rawPacket = packetFactory
      .convertPacketResponseToCustomPacketResponse(packetResponse);
    if(rawPacket == null)
      throw new IllegalStateException("Invalid raw packet response.");
    packetFactory.sessionsToSendFrom(packetResponse.getSession()).stream().forEach(session -> {
      try {
        final SocketChannel socketChannel = this.sessionManager.getSocketChannelFromSession(session);
        rawPacket.send(socketChannel); if(packetFactory.thenDisconnect()) socketChannel.close();
      }
      catch(IllegalArgumentException | IOException exc) {
        log.error("Failed to send raw packet ({}) to session '{}': {}",
          packetFactory.getPacketType(), session, exc.getMessage(), exc);
      }
    });
  }

}
