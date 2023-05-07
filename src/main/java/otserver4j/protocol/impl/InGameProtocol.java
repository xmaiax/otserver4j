package otserver4j.protocol.impl;

import org.springframework.beans.factory.annotation.Autowired;

import otserver4j.action.impl.LookAction;
import otserver4j.action.impl.MoveAction;
import otserver4j.action.impl.TalkAction;
import otserver4j.action.impl.TurnAction;

@org.springframework.stereotype.Component
public class InGameProtocol implements otserver4j.protocol.Protocol {

  @Autowired private TalkAction talkAction;
  @Autowired private TurnAction turnAction;
  @Autowired private MoveAction moveAction;
  @Autowired private LookAction lookAction;
  
  @Override public otserver4j.packet.Packet execute(java.nio.ByteBuffer buffer,
      java.nio.channels.SelectionKey key, java.nio.channels.SocketChannel channel,
      otserver4j.structure.PlayerCharacter player, otserver4j.packet.PacketType type)
        throws otserver4j.exception.InGameException {
    switch(type) {
      case TALK:
        return this.talkAction.execute(type, buffer, channel, player);
      case TURN_NORTH: case TURN_SOUTH: case TURN_EAST: case TURN_WEST:
        return this.turnAction.execute(type, buffer, channel, player);
      case MOVE_NORTH: case MOVE_SOUTH: case MOVE_EAST: case MOVE_WEST:
        return this.moveAction.execute(type, buffer, channel, player);
      case LOOK: return this.lookAction.execute(type, buffer, channel, player);
      default: return null;
    }
  }

}
