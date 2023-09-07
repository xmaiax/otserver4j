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
public class SessionManager extends DefaultTcpNioConnectionSupport implements TcpSender {

  private final String portsRegexCapturingGroup;
  private final Map<String, SocketChannel> activeSessions;

  public SessionManager(@Value(":\\d+:${otserver.port}:") String portsRegexCapturingGroup) {
    this.portsRegexCapturingGroup = portsRegexCapturingGroup;
    this.activeSessions = new HashMap<>();
  }

  public SocketChannel getSocketChannelFromPacketWrapper(PacketWrapper packetWrapper) {
    if(packetWrapper == null || packetWrapper.getSession() == null ||
       packetWrapper.getSession().isBlank()) throw new IllegalArgumentException("???");
    final SocketChannel socketChannel = this.activeSessions.get(packetWrapper.getSession());
    if(socketChannel == null) throw new IllegalArgumentException("???");
    return socketChannel;
  }

  public String getSession(String connectionId) {
    if(connectionId == null || connectionId.isBlank()) throw new IllegalArgumentException("???");
    final String[] split = connectionId.split(this.portsRegexCapturingGroup);
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
    final String session = this.getSession(tcpConnection.getConnectionId());
    this.activeSessions.put(session, ((TcpNioConnectionWithSocketChannel) tcpConnection).getSocketChannel());
    log.info("New session: {}", session);
  }

  public void removeConnection(String connectionIdentifier) {
    this.activeSessions.remove(connectionIdentifier);
    log.info("Session removed: {}", connectionIdentifier);
  }

  @Override
  public void removeDeadConnection(TcpConnection tcpConnection) {
    if(tcpConnection == null || tcpConnection.getConnectionId() == null ||
        tcpConnection.getConnectionId().isBlank()) {
      throw new IllegalArgumentException("???");
    }
    final String session = this.getSession(tcpConnection.getConnectionId());
    this.removeConnection(session);
  }

  @Override
  public TcpNioConnection createNewConnection(SocketChannel socketChannel, boolean isServer,
      boolean lookupHost, ApplicationEventPublisher applicationEventPublisher, String connectionFactoryName) {
    return new TcpNioConnectionWithSocketChannel(socketChannel, isServer,
      lookupHost, applicationEventPublisher, connectionFactoryName);
  }

}
