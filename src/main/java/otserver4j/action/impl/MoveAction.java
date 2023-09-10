package otserver4j.action.impl;

import otserver4j.converter.PacketType;

@org.springframework.stereotype.Component
public class MoveAction implements otserver4j.action.Action {

  @org.springframework.beans.factory.annotation.Autowired private TurnAction turnAction;
  
  @Override public otserver4j.converter.RawPacket execute(PacketType type, java.nio.ByteBuffer buffer,
      java.nio.channels.SocketChannel channel, otserver4j.entity.PlayerCharacter player) {
    switch(type) {
      case MOVE_NORTH: return this.turnAction.execute(PacketType.TURN_NORTH, buffer, channel, player);
      case MOVE_SOUTH: return this.turnAction.execute(PacketType.TURN_SOUTH, buffer, channel, player);
      case MOVE_EAST:  return this.turnAction.execute(PacketType.TURN_EAST,  buffer, channel, player);
      case MOVE_WEST:  return this.turnAction.execute(PacketType.TURN_WEST,  buffer, channel, player);
      default: return null;
    }
  }

}
