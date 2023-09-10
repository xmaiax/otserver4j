package otserver4j.action;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Data;
import lombok.experimental.Accessors;
import otserver4j.converter.RawPacket;
import otserver4j.entity.PlayerCharacter;
import otserver4j.structure.Position;

//@lombok.extern.slf4j.Slf4j
@org.springframework.stereotype.Component
public class EventQueue {

  private java.util.Map<PlayerCharacter, SocketChannel> loggedPlayers;

  @Data @Accessors(chain = true)
  private static class Event {
    private RawPacket packet;
    private Set<PlayerCharacter> players;
  }
  
  @Autowired
  public EventQueue() {
    this.loggedPlayers = new java.util.HashMap<>(1000);
  }

  public void addConnection(PlayerCharacter playerCharacter, SelectionKey selectionKey) {
    this.loggedPlayers.put(playerCharacter, (SocketChannel) selectionKey.channel());
  }
  
  public void removeConnection(PlayerCharacter playerCharacter) {
    if(playerCharacter != null)
      this.loggedPlayers.remove(playerCharacter);
  }

  public void addNewEvent(RawPacket packet, Position position) {
    final Event event = new Event().setPacket(packet).setPlayers(
      this.loggedPlayers.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toSet())
    );
    event.getPlayers().forEach(player -> {
      final SocketChannel socketChannel = this.loggedPlayers.get(player);
      if(socketChannel != null) {
        try { event.getPacket().send(socketChannel); }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

}
