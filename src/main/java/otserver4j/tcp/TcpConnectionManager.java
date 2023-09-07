package otserver4j.tcp;

import static java.math.BigInteger.ONE;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.ip.tcp.connection.DefaultTcpNioConnectionSupport;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpNioConnection;
import org.springframework.integration.ip.tcp.connection.TcpSender;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import otserver4j.consumer.converter.PacketMessageConverter.PacketWrapper;

//TODO Melhorar as exception aqui
@Slf4j @Component
public class TcpConnectionManager extends DefaultTcpNioConnectionSupport implements TcpSender {

  private final String portsRegexCapturingGroup;
  private final Map<String, SocketChannel> connections;

  public SocketChannel getSocketChannelFromPacketWrapper(PacketWrapper packetWrapper) {
    if(packetWrapper == null || packetWrapper.getConnectionIdentifier() == null ||
       packetWrapper.getConnectionIdentifier().isBlank()) throw new IllegalArgumentException("???");
    final SocketChannel socketChannel = this.connections.get(packetWrapper.getConnectionIdentifier());
    if(socketChannel == null) throw new IllegalArgumentException("???");
    return socketChannel;
  }

  public TcpConnectionManager(@Value(":\\d+:${otserver.port}:") String portsRegexCapturingGroup) {
    this.connections = new HashMap<>();
    this.portsRegexCapturingGroup = portsRegexCapturingGroup;
  }
  
  public String getConnectionIdentifier(String connectionId) {
    if(connectionId == null || connectionId.isBlank()) throw new IllegalArgumentException("???");
    final String[] split = connectionId.split(portsRegexCapturingGroup);
    if(split.length > ONE.intValue()) return split[ONE.intValue()];
    throw new IllegalArgumentException("???");
  }

  private static class TcpNioConnectionWithSocketChannel extends TcpNioConnection {
    private SocketChannel _socketChannel;
    public SocketChannel getSocketChannel() { return this._socketChannel; }
    public TcpNioConnectionWithSocketChannel(SocketChannel socketChannel, boolean server, boolean lookupHost,
        ApplicationEventPublisher applicationEventPublisher, String connectionFactoryName) {
      super(socketChannel, server, lookupHost, applicationEventPublisher, connectionFactoryName);
      this._socketChannel = socketChannel;
    }
  }

  @Override
  public void addNewConnection(TcpConnection tcpConnection) {
    if(tcpConnection == null || tcpConnection.getConnectionId() == null ||
       tcpConnection.getConnectionId().isBlank()) {
      throw new IllegalArgumentException("???");
    }
    final String connectionIdentifier = this.getConnectionIdentifier(tcpConnection.getConnectionId());
    this.connections.put(connectionIdentifier, ((TcpNioConnectionWithSocketChannel) tcpConnection).getSocketChannel());
    log.info("New connection: {}", connectionIdentifier);
  }

  public void removeConnection(String connectionIdentifier) {
    this.connections.remove(connectionIdentifier);
    log.info("Connection removed: {}", connectionIdentifier);
  }

  @Override
  public void removeDeadConnection(TcpConnection tcpConnection) {
    if(tcpConnection == null || tcpConnection.getConnectionId() == null ||
        tcpConnection.getConnectionId().isBlank()) {
       throw new IllegalArgumentException("???");
     }
     final String connectionIdentifier = this.getConnectionIdentifier(tcpConnection.getConnectionId());
    this.removeConnection(connectionIdentifier);
  }

  @Override
  public TcpNioConnection createNewConnection(SocketChannel socketChannel, boolean isServer,
      boolean lookupHost, ApplicationEventPublisher applicationEventPublisher, String connectionFactoryName) {
    return new TcpNioConnectionWithSocketChannel(socketChannel, isServer,
      lookupHost, applicationEventPublisher, connectionFactoryName);
  }

}
