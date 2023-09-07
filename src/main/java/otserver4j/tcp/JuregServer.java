package otserver4j.tcp;

import static java.math.BigInteger.ZERO;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import otserver4j.action.EventQueue;
import otserver4j.consumer.converter.PacketType;
import otserver4j.consumer.converter.RawPacket;
import otserver4j.protocol.impl.InGameProtocol;
import otserver4j.protocol.impl.SpawnProtocol;
import otserver4j.structure.PlayerCharacter;

@Slf4j @lombok.Getter
@org.springframework.stereotype.Component
public class JuregServer {

  private SpawnProtocol spawnProtocol;
  private InGameProtocol inGameProtocol;

  private Integer port;
  private Integer version;
  private Boolean isRunning;
  private Selector selector;
  private ServerSocketChannel serverSocketChannel;

  private void validateHost(String host) {
    log.info("Validating hostname: {}", host);
    try { java.net.InetAddress.getByName(host).getHostAddress(); }
    catch(java.net.UnknownHostException uhe) {
      log.error("'{}' is not a valid hostname.", host);
      throw new IllegalArgumentException(String.format("'%s' is not a valid hostname.", host));
    }
    log.info("'{}' is a valid hostname.", host);
  }

  @org.springframework.beans.factory.annotation.Autowired public JuregServer(
      @Value("${otserver.host:localhost}") String host,
      @Value("${otserver.port}") Integer port,
      @Value("${otserver.version}") Integer version,
      SpawnProtocol spawnProtocol,
      InGameProtocol inGameProtocol,
      EventQueue eventQueue) {
    this.spawnProtocol = spawnProtocol;
    this.inGameProtocol = inGameProtocol;
    this.port = port + 1;
    this.version = version;
    this.validateHost(host);
    log.info("Starting TCP server...");
    this.isRunning = Boolean.TRUE;
    try {
      this.selector = Selector.open();
      this.serverSocketChannel = ServerSocketChannel.open();
      this.serverSocketChannel.configureBlocking(Boolean.FALSE);
      this.serverSocketChannel.socket().bind(new java.net.InetSocketAddress(this.port));
      this.serverSocketChannel.register(this.selector, this.serverSocketChannel.validOps());
      
      new ConnectionThread(this, eventQueue).start();
      
    }
    catch (IOException ioex) {
      log.error("Unable to start TCP server: {}", ioex.getMessage(), ioex);
      System.exit(-BigInteger.ONE.intValue());
    }
  }

  @javax.annotation.PreDestroy public void shutdown() {
    this.isRunning = Boolean.FALSE;
    log.info("TCP server stopping...");
  }

}

@Slf4j @lombok.experimental.Accessors(chain = true)
class ConnectionThread extends Thread {
  
  private JuregServer server;
  private EventQueue eventQueue;
  
  public ConnectionThread(JuregServer server, EventQueue eventQueue) {
    super("TCPServerThread");
    this.server = server;
    this.eventQueue = eventQueue;
  }

  private void handleReceivedPacket(
      java.util.Iterator<SelectionKey> selectedKeysIterator) throws IOException {
    SocketChannel socketChannel = null;
    while(selectedKeysIterator.hasNext()) {
      final SelectionKey key = selectedKeysIterator.next();
      selectedKeysIterator.remove();
      if(key.isAcceptable()) {
        socketChannel = this.server.getServerSocketChannel().accept();
        socketChannel.configureBlocking(Boolean.FALSE);
        socketChannel.register(this.server.getSelector(),
          SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      }
      else if(key.isReadable() || key.isWritable()) {
        socketChannel = (SocketChannel) key.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(RawPacket.MAX_SIZE);
        try { socketChannel.read(buffer); }
        catch(IOException ioex) {
          log.error("Error on read packet, closing the connection.");
          socketChannel.close();
          return;
        }
        buffer.position(ZERO.intValue());
        final Integer packetSize = RawPacket.readInt16(buffer);
        if(packetSize > ZERO.intValue()) {
          final PacketType packetType = PacketType.fromCode(RawPacket.readByte(buffer));
          PlayerCharacter loggedPlayer = null;
          if(key.attachment() != null) loggedPlayer = (PlayerCharacter) key.attachment();
          log.debug("New received packet [Size={}, Type={}]{}", packetSize,
            packetType.name(), loggedPlayer == null ? "" :
              String.format(" from %s.", loggedPlayer.getName()));
          Boolean thenDisconnect = Boolean.FALSE;
          otserver4j.protocol.Protocol protocol = null;
          if(loggedPlayer == null) {
            switch(packetType) {
              case LOGIN_SUCCESS:
                protocol = this.server.getSpawnProtocol(); break;
              default: break;
            }
          }
          else if(PacketType.LOGOFF.equals(packetType)) {
            //TODO: Lógica para remover personagem do mundo ao desconectar.
            thenDisconnect = Boolean.TRUE;
          }
          else protocol = this.server.getInGameProtocol();
          try {
            if(protocol != null) {
              final RawPacket packet = protocol.execute(buffer, socketChannel, loggedPlayer, packetType);
              (packet == null ? RawPacket.newSnapbackPacket(loggedPlayer) : packet).send(socketChannel);
            }
          }
          catch(otserver4j.exception.LoginException otjex) {
            RawPacket.createGenericErrorPacket(protocol instanceof SpawnProtocol ?
              RawPacket.PROCESSING_LOGIN_CODE_NOK : RawPacket.LOGIN_CODE_NOK,
                otjex.getMessage()).send(socketChannel);
          }
          catch(otserver4j.exception.InGameException ige) {
            //TODO: Tratativa de falhas in-game
            log.error("Deu ruim pra carai: {}", ige.getMessage());
          }
          finally {
            if(thenDisconnect) {
              this.eventQueue.removeConnection(loggedPlayer);
              socketChannel.close();
            }
          }
        }
      }
    }
  }

  @Override
  public void run() {
    while(this.server.getIsRunning()) {
      try {
        this.server.getSelector().select();
        this.handleReceivedPacket(this.server.getSelector().selectedKeys().iterator());
      }
      catch(IOException ioex) {
        log.error("Error on handling connection: {}", ioex.getMessage(), ioex);
      }
    }
  }

}
