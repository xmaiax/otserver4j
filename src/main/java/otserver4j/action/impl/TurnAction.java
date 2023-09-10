package otserver4j.action.impl;

import static otserver4j.structure.Direction.*;

import otserver4j.converter.RawPacket;

@org.springframework.stereotype.Component
public class TurnAction implements otserver4j.action.Action {

  @Override public RawPacket execute(otserver4j.converter.PacketType type,
      java.nio.ByteBuffer buffer, java.nio.channels.SocketChannel channel,
      otserver4j.entity.PlayerCharacter player) {
    otserver4j.structure.Direction direction = null;
    switch(type) {
      case TURN_NORTH: direction = NORTH; break;
      case TURN_SOUTH: direction = SOUTH; break;
      case TURN_EAST:  direction = EAST;  break;
      case TURN_WEST:  direction = WEST;  break;
      default: direction = player.getDirection() != null ? player.getDirection() : SOUTH;
    }
    player.setDirection(direction);
    // TODO Atualizar mundo?
    return RawPacket.newSnapbackPacket(player);
  }

}
