package otserver4j.configuration;

import static java.math.BigInteger.ZERO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import otserver4j.repository.SessionManager;
import otserver4j.structure.PacketType;
import otserver4j.structure.RawPacket;

@Slf4j @Component
public class TcpServerConfiguration extends TcpNioServerConnectionFactory {

  private final AmqpTemplate amqpTemplate;
  private final SessionManager sessionManager;

  public TcpServerConfiguration(@Value("${otserver.port}") Integer port,
      SessionManager sessionManager, AmqpTemplate amqpTemplate) {
    super(port);
    this.amqpTemplate = amqpTemplate;
    this.sessionManager = sessionManager;
    log.info("Starting TCP Server...");
    final ByteArrayRawSerializer byteArrayRawSerializer = new ByteArrayRawSerializer();
    super.setSerializer(byteArrayRawSerializer);
    super.setDeserializer(byteArrayRawSerializer);
    super.registerListener(new TcpReceivingChannelAdapter());
    super.registerSender(sessionManager);
    super.setTcpNioConnectionSupport(sessionManager);
  }

  @PostConstruct
  public void initialize() {
    super.start();
    log.info("TCP Server started on port {}", super.getPort());
  }

  @Override
  public void doAccept(Selector selector, ServerSocketChannel serverSocketChannel, long now) {
    super.doAccept(selector, serverSocketChannel, now);
    try { selector.select(); }
    catch(IOException ioex) {
      log.error("Failed to select the keys for channels that are ready for I/O operations: {}",
        ioex.getMessage(), ioex);
      return;
    }
    final Iterator<SelectionKey> selectedKeysIterator = selector.selectedKeys().iterator();
    SocketChannel socketChannel = null;
    while(selectedKeysIterator.hasNext()) {
      final SelectionKey key = selectedKeysIterator.next(); 
      selectedKeysIterator.remove();
      if(key.isAcceptable()) {
        try {
          socketChannel = serverSocketChannel.accept();
          socketChannel.configureBlocking(Boolean.FALSE);
          socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
        catch(IOException ioex) {
          log.error("Failed to accept / configure socket channel blocking / register the socket channel operations: {}",
            ioex.getMessage(), ioex);
          return;
        }
      } else if(key.isReadable() || key.isWritable()) {
        socketChannel = (SocketChannel) key.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(RawPacket.MAX_SIZE);
        try { socketChannel.read(buffer); }
        catch (IOException ioex) {
          log.error("Packet read error: {}", ioex.getMessage(), ioex);
          try { socketChannel.close(); }
          catch(IOException ioex2) {
            log.error("Socket channel failed to close: {}", ioex2.getMessage(), ioex2);
          }
          return;
        }
        buffer.position(ZERO.intValue());
        final Integer packetSize = RawPacket.readInt16(buffer); 
        if(packetSize > ZERO.intValue()) {
          final PacketType packetType = PacketType.fromCode(RawPacket.readByte(buffer));
          if(PacketType.INVALID.equals(packetType)) {
            log.warn("Invalid packet!"); return;
          }
          this.amqpTemplate.convertAndSend(AmqpConfiguration.PACKET_INPUT_QUEUE,
            new otserver4j.service.impl.PacketMessageConverter.RawPacketAmqpMessage()
              .setPacketSize(packetSize).setPacketType(packetType).setBuffer(buffer)
              .setSession(this.sessionManager.getSession(key.attachment().toString())));
        }
      }
    }
  }

  @RabbitListener(queues = { AmqpConfiguration.PACKET_INPUT_QUEUE, })
  public void inputListener(otserver4j.service.AbstractPacketFactory.PacketRequest packetRequest) {
    this.amqpTemplate.convertAndSend(AmqpConfiguration.PACKET_OUTPUT_QUEUE, packetRequest); }

}
