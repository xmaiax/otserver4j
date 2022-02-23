package com.github.xmaiax.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.xmaiax.exception.GenericException;
import com.github.xmaiax.exception.LoginException;
import com.github.xmaiax.packet.Packet;
import com.github.xmaiax.protocol.Protocol;
import com.github.xmaiax.protocol.Protocol.LoginRequestType;
import com.github.xmaiax.protocol.impl.InGameProtocol;
import com.github.xmaiax.protocol.impl.LoadCharactersProtocol;
import com.github.xmaiax.protocol.impl.LoginSuccessProtocol;
import com.github.xmaiax.structure.Player;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Component @Getter @Slf4j
public class Server {

  private LoadCharactersProtocol loadCharactersProtocol;
  private LoginSuccessProtocol loginSuccessProtocol;
  private InGameProtocol inGameProtocol;

  private Integer port;
  private Integer version;
  private Boolean isRunning;
  private Selector selector;
  private ServerSocketChannel serverSocketChannel;

  @PostConstruct
  public void initOk() {
    log.info("TCP server started! [port={}, protocol={}]", this.port,
      this.loadCharactersProtocol.formatClientVersion(this.version));
  }

  private void validateHost(String host) {
    log.info("Validating hostname: {}", host);
    try { java.net.InetAddress.getByName(host).getHostAddress(); }
    catch(UnknownHostException uhe) {
      log.error("'{}' is not a valid hostname.", host);
      throw new IllegalArgumentException(String.format("'%s' is not a valid hostname.", host));
    }
    log.info("'{}' is a valid hostname.", host);
  }

  @Autowired
  public Server(@Value("${otserver.host}") String host,
      @Value("${otserver.port}") Integer port,
      @Value("${otserver.version}") Integer version,
      LoadCharactersProtocol loadCharactersProtocol,
      LoginSuccessProtocol loginSuccessProtocol,
      InGameProtocol inGameProtocol) {
    this.loadCharactersProtocol = loadCharactersProtocol;
    this.loginSuccessProtocol = loginSuccessProtocol;
    this.inGameProtocol = inGameProtocol;
    this.port = port;
    this.version = version;
    this.validateHost(host);
    log.info("Starting TCP server...");
    this.isRunning = Boolean.TRUE;
    try {
      this.selector = Selector.open();
      this.serverSocketChannel = ServerSocketChannel.open();
      this.serverSocketChannel.configureBlocking(false);
      this.serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
      this.serverSocketChannel.register(this.selector, serverSocketChannel.validOps());
      new ConnectionThread().setServer(this).start();
    }
    catch (IOException ioex) {
      log.error("Unable to start TCP server: {}", ioex.getMessage(), ioex);
      System.exit(-1);
    }
  }

  @PreDestroy
  public void shutdown() {
    this.isRunning = Boolean.FALSE;
    log.info("TCP server stopping...");
  }

}

@Setter @Slf4j
@Accessors(chain = true)
class ConnectionThread extends Thread {

  private Server server;

  private void handleReceivedPacket(Iterator<SelectionKey> selectedKeysIterator) throws IOException {
    SocketChannel socketChannel = null;
    while(selectedKeysIterator.hasNext()) {
      final SelectionKey key = selectedKeysIterator.next();
      if(key.isAcceptable()) {
        socketChannel = this.server.getServerSocketChannel().accept();
        socketChannel.configureBlocking(Boolean.FALSE);
        socketChannel.register(this.server.getSelector(),
          SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      }
      else if(key.isReadable() || key.isWritable()) {
        socketChannel = (SocketChannel) key.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_SIZE);
        try { socketChannel.read(buffer); }
        catch(IOException ioex) {
          log.error("Error on read packet, closing the connection.", ioex);
          socketChannel.close();
          return;
        }
        buffer.position(BigInteger.ZERO.intValue());
        final Integer packetSize = Packet.readInt16(buffer);
        if(packetSize > BigInteger.ZERO.intValue()) {
          final Integer rawType = Packet.readByte(buffer);
          Player loggedPlayer = null;
          if(key.attachment() != null) {
            loggedPlayer = (Player) key.attachment();
          }
          log.info("New received packet [Size={}, Type=0x{}]{}", packetSize,
            String.format("%2s", Integer.toHexString(rawType)).replace(' ', '0'),
              loggedPlayer == null ? "" : String.format("from %s.", loggedPlayer.getName()));
          Protocol protocol = null;
          if(loggedPlayer == null) switch(LoginRequestType.fromCode(rawType)) {
            case LOAD_CHARACTER_LIST: protocol = this.server.getLoadCharactersProtocol(); break;
            case LOGIN_SUCCESS: protocol = this.server.getLoginSuccessProtocol(); break;
            default: break;
          }
          else protocol = this.server.getInGameProtocol();
          Packet packet = null;
          try { packet = protocol.execute(buffer, key); }
          catch(LoginException otjex) {
            packet = Packet.createGenericErrorPacket(otjex.getCode(), otjex.getMessage());
          }
          catch(GenericException ge) {
            //TODO: Tratativa de falhas in-game
          }
          if(protocol != null && packet != null) packet.send(socketChannel);
        }
      }
      selectedKeysIterator.remove();
    }
  }

  @Override
  public void run() {
    while(this.server.getIsRunning()) {
      try {
        this.server.getSelector().select();
        this.handleReceivedPacket(
          this.server.getSelector().selectedKeys().iterator());
      }
      catch(IOException ioex) {
        log.error("Error on handling connection: {}", ioex.getMessage(), ioex);
      }
    }
  }

}
