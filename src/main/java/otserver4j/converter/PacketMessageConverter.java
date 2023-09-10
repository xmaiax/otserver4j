package otserver4j.converter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Component
public class PacketMessageConverter implements MessageConverter {

  @Data @Accessors(chain = true) public static class RawPacketAmqpMessage {
    private Integer packetSize;
    private PacketType packetType;
    private ByteBuffer buffer;
    private String session;
  }

  public static abstract class PacketWrapper {
    @Accessors(chain = true) @Getter @Setter private String session;
    protected abstract Object modifyFromBuffer(ByteBuffer byteBuffer, Integer size);
    protected abstract PacketType getPacketType();
    public abstract RawPacket convertToRawPacket();
  }

  private final ObjectMapper objectMapper;
  public PacketMessageConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private Object getWrapperFromRawPacketAmqpMessage(RawPacketAmqpMessage rawPacketAmqpMessage) {
    try {
      final Optional<Constructor<?>> emptyConstructor = Arrays.asList(
        rawPacketAmqpMessage.getPacketType().getObjectClass().getConstructors()).stream()
          .filter(constr -> constr.getParameterCount() < BigInteger.ONE.intValue()).findFirst();
      if(emptyConstructor.isEmpty()) {
        log.error("xxxxxxxxxxxxxxxxxxxxxx");
        throw new IllegalStateException(String.format("No empty constructor found in packet wrapper '%s'...",
          rawPacketAmqpMessage.getPacketType().getObjectClass().getCanonicalName()));
      }
      final PacketWrapper packetWrapper = (PacketWrapper)((PacketWrapper) emptyConstructor.get().newInstance())
        .modifyFromBuffer(rawPacketAmqpMessage.getBuffer(), rawPacketAmqpMessage.getPacketSize());
      return packetWrapper.setSession(rawPacketAmqpMessage.getSession());
    }
    catch(Exception exc) {
      log.error("xxxxxxxxxxxxxxxxxxxxxx");
      throw new IllegalStateException(exc);
    }
  }

  @Override
  public Message toMessage(Object object, MessageProperties messageProperties)
      throws MessageConversionException {
    if(object instanceof RawPacketAmqpMessage) {
      final RawPacketAmqpMessage rawPacketAmqpMessage = (RawPacketAmqpMessage) object;
      messageProperties.setType(rawPacketAmqpMessage.getPacketType().name());
      try {
        return new Message(this.objectMapper.writeValueAsBytes(
          this.getWrapperFromRawPacketAmqpMessage(rawPacketAmqpMessage)), messageProperties);
      }
      catch(JsonProcessingException jpex) {
        log.error("xxxxxxxxxxxxxxxxxxxxxx");
        throw new MessageConversionException(jpex.getMessage(), jpex);
      }
    }
    else if(object instanceof PacketWrapper) {
      final PacketWrapper packetWrapper = (PacketWrapper) object;
      messageProperties.setType(packetWrapper.getPacketType().name());
      try { return new Message(this.objectMapper.writeValueAsBytes(packetWrapper), messageProperties); }
      catch(JsonProcessingException jpex) {
        log.error("xxxxxxxxxxxxxxxxxxxxxx");
        throw new MessageConversionException(jpex.getMessage(), jpex);
      }
    }
    else {
      log.error("xxxxxxxxxxxxxxxxxxxxxx");
      throw new MessageConversionException(
        String.format("Unknown instance type: %s", object.getClass()));
    }
  }

  @Override
  public Object fromMessage(Message message) throws MessageConversionException {
    final PacketType packetType = PacketType.valueOf(message.getMessageProperties().getType());
    try { return this.objectMapper.readValue(message.getBody(), packetType.getObjectClass()); }
    catch(IOException ioex) {
      log.error("xxxxxxxxxxxxxxxxxxxxxx");
      throw new IllegalStateException(ioex);
    }
  }

}
