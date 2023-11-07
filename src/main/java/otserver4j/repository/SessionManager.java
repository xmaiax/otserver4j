package otserver4j.repository;

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

@Slf4j @Component public class SessionManager extends DefaultTcpNioConnectionSupport implements TcpSender {

  private final String portsRegexCapturingGroup;
  private final Map<String, SocketChannel> activeSessions;

  public SessionManager(@Value(":\\d+:${otserver.port}:") String portsRegexCapturingGroup) {
    this.portsRegexCapturingGroup = portsRegexCapturingGroup;
    this.activeSessions = new HashMap<>();
  }

  private static final IllegalArgumentException
    INVALID_SESSION_EXCEPTION = new IllegalArgumentException("Invalid session.");

  public SocketChannel getSocketChannelFromSession(String session) {
    if(session == null || session.isBlank()) {
      log.error("Invalid session.");
      throw INVALID_SESSION_EXCEPTION;
    }
    final SocketChannel socketChannel = this.activeSessions.get(session);
    if(socketChannel == null) {
      final String message = String.format("No socket channel found for session '%s'.", session);
      log.error(message); throw new IllegalArgumentException(message);
    }
    return socketChannel;
  }

  public String getSession(String connectionId) {
    if(connectionId == null || connectionId.isBlank()) throw new IllegalArgumentException("???");
    final String[] split = connectionId.split(this.portsRegexCapturingGroup);
    if(split.length > ONE.intValue()) return split[ONE.intValue()];
    final String message = String.format("Invalid connectionId format: %s", connectionId);
    log.error(message); throw new IllegalArgumentException(message);
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
      log.error("Invalid TCP connection.");
      throw new IllegalArgumentException("Invalid TCP connection.");
    }
    final String session = this.getSession(tcpConnection.getConnectionId());
    //...
    this.activeSessions.put(session,
      ((TcpNioConnectionWithSocketChannel) tcpConnection).getSocketChannel());
  }

  @Override
  public void removeDeadConnection(TcpConnection tcpConnection) {
    if(tcpConnection == null || tcpConnection.getConnectionId() == null ||
        tcpConnection.getConnectionId().isBlank()) {
      log.error("Invalid TCP connection.");
      throw new IllegalArgumentException("Invalid TCP connection.");
    }
    final String session = this.getSession(tcpConnection.getConnectionId());
    //...
    this.activeSessions.remove(session);
  }

  @Override
  public TcpNioConnection createNewConnection(SocketChannel socketChannel, boolean isServer,
      boolean lookupHost, ApplicationEventPublisher applicationEventPublisher, String connectionFactoryName) {
    return new TcpNioConnectionWithSocketChannel(socketChannel, isServer,
      lookupHost, applicationEventPublisher, connectionFactoryName);
  }

}
