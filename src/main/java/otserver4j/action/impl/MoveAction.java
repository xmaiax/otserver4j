package otserver4j.action.impl;

import org.springframework.beans.factory.annotation.Autowired;

import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;
import otserver4j.structure.Direction;
import otserver4j.structure.Position;
import otserver4j.structure.Tile.TileWithItems;

@org.springframework.stereotype.Component
public class MoveAction implements otserver4j.action.Action {

  @Autowired private otserver4j.structure.GameMap gameMap;

  public TileWithItems getTileWithItemsFromPosition(Integer x, Integer y, Integer z) {
    final String positionKey = String.format("%d#%d#%d", x, y, z);
    return this.gameMap.containsKey(positionKey) ? this.gameMap.get(positionKey) : null;
  }

  private Packet getMapDescription(final Position position,
      final Integer width, final Integer height, final Packet packet) {
    int skip = -1, startz = 7, endz = 0, zstep = -1,
      x = position.getX(), y = position.getY();
    for(int nz = startz; nz != endz + zstep; nz += zstep) {
      int offset = position.getZ() - nz;
      for(int nx = 0; nx < width; nx++) {
        for(int ny = 0; ny < height; ny++) {
          final TileWithItems tile = this.getTileWithItemsFromPosition(x + nx + offset, y + ny + offset, nz);
          if(tile != null) {
            if(skip >= 0) {
              packet.writeByte(skip);
              packet.writeByte(0xff);
            }
            skip = 0;
            packet.writeInt16(tile.getTile().getCode());
          }
          else if(skip == 0xfe) {
            packet.writeByte(0xff).writeByte(0xff);
            skip = -1;
          }
          else {
            skip++;
          }
        }
      }
    }
    if(skip >= 0) {
      packet.writeByte(skip);
      packet.writeByte(0xff);
    }
    return packet;
  }

  @Override public Packet execute(PacketType type, java.nio.ByteBuffer buffer,
      java.nio.channels.SocketChannel channel, otserver4j.structure.PlayerCharacter player) {

    Direction direction = null;
    switch(type) {
      case MOVE_NORTH:
        //GetMapDescription(oldTile.Position.X - 8, newTile.Position.Y - 6, newTile.Position.Z, 18, 1, msg);
        direction = Direction.NORTH; break;
      case MOVE_SOUTH:
        //GetMapDescription(oldTile.Position.X - 8, newTile.Position.Y + 7, newTile.Position.Z, 18, 1, msg);
        direction = Direction.SOUTH; break;
      case MOVE_EAST:
        //GetMapDescription(newTile.Position.X + 9, newTile.Position.Y - 6, newTile.Position.Z, 1, 14, msg);
        direction = Direction.EAST; break;
      case MOVE_WEST:
        //GetMapDescription(newTile.Position.X - 8, newTile.Position.Y - 6, newTile.Position.Z, 1, 14, msg);
        direction = Direction.WEST; break;
      default: return Packet.newSnapbackPacket(player);
    }

    final Packet packet = new Packet();
    packet.writeByte(0x6d);
    packet.writeInt16(player.getPosition().getX());
    packet.writeInt16(player.getPosition().getY());
    packet.writeByte(player.getPosition().getZ());
    packet.writeByte(0x01); //Stack position??
    player.setPosition(player.getPosition().move(direction));
    packet.writeInt16(player.getPosition().getX());
    packet.writeInt16(player.getPosition().getY());
    packet.writeByte(player.getPosition().getZ());
    packet.writeByte(0x65 + direction.getCode());

    final Position position = new Position()
      .setX((Direction.EAST.equals(direction) ? 9 : -8) + player.getPosition().getX())
      .setY((Direction.SOUTH.equals(direction) ? 7 : -6) + player.getPosition().getX())
      .setZ(player.getPosition().getZ());

    final Integer width = Direction.NORTH.equals(direction) || Direction.SOUTH.equals(direction) ? 18 : 1;
    final Integer height = Direction.NORTH.equals(direction) || Direction.SOUTH.equals(direction) ? 1 : 14;

    return this.getMapDescription(position, width, height, packet);
  }

}
