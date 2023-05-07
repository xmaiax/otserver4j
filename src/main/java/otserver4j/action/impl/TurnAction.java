package otserver4j.action.impl;

import otserver4j.packet.Packet;
import static otserver4j.structure.Direction.*;

@org.springframework.stereotype.Component
public class TurnAction implements otserver4j.action.Action {

  @Override public Packet execute(otserver4j.packet.PacketType type,
      java.nio.ByteBuffer buffer, java.nio.channels.SocketChannel channel,
      otserver4j.structure.PlayerCharacter player) {
    otserver4j.structure.Direction direction = null;
    switch(type) {
      case TURN_NORTH: direction = NORTH; break;
      case TURN_SOUTH: direction = SOUTH; break;
      case TURN_EAST: direction = EAST; break;
      case TURN_WEST: direction = WEST; break;
      default: direction = player.getDirection() != null ? player.getDirection() : SOUTH;
    }
    player.setDirection(direction);
    // TODO Atualizar mundo?
    return Packet.newSnapbackPacket(player);
  }

}
