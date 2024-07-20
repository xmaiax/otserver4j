package otserver4j.action.impl;

import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;
import otserver4j.protocol.impl.SpawnProtocol;
import otserver4j.structure.Direction;

@org.springframework.stereotype.Component
public class MoveAction implements otserver4j.action.Action {

  @org.springframework.beans.factory.annotation.Autowired private TurnAction turnAction;
  @org.springframework.beans.factory.annotation.Autowired private SpawnProtocol spawnProtocol;
  
  @Override public otserver4j.packet.Packet execute(PacketType type, java.nio.ByteBuffer buffer,
      java.nio.channels.SocketChannel channel, otserver4j.structure.PlayerCharacter player) {
    switch(type) {
      case MOVE_NORTH:
        //return this.turnAction.execute(PacketType.TURN_NORTH, buffer, channel, player);
        final Long id = SpawnProtocol.PLAYER_IDENTIFIER_PREFIX + player.getIdentifier();
        return this.spawnProtocol.writePlayerMapInfo(player, new Packet()
          .writeByte(0x65));
      case MOVE_SOUTH: return this.turnAction.execute(PacketType.TURN_SOUTH, buffer, channel, player);
      case MOVE_EAST:  return this.turnAction.execute(PacketType.TURN_EAST,  buffer, channel, player);
      case MOVE_WEST:  return this.turnAction.execute(PacketType.TURN_WEST,  buffer, channel, player);
      default: return null;
    }
  }

}
