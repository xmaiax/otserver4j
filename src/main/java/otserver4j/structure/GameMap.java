package otserver4j.structure;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.stereotype.Component;

import otserver4j.packet.Packet;
import otserver4j.protocol.impl.ProcessingLoginProtocol;
import otserver4j.structure.Tile.TileWithItems;

@Component
public class GameMap extends HashMap<String, TileWithItems> {

  private static final long serialVersionUID = -1L;

  public static final Integer
    OFFSET_X = -8, OFFSET_Y = -6, SCREEN_WIDTH = 18, SCREEN_HEIGHT = 14;

  public static final String MAP_TILE_POSITION_SEPARATOR = "#";

  private String getTilePositionKey(Integer x, Integer y, Integer z) {
    return String.format("%d%s%d%s%d",
      x, MAP_TILE_POSITION_SEPARATOR,
      y, MAP_TILE_POSITION_SEPARATOR, z);
  }

  public GameMap() {
    for(Integer x = 31950, z = 7; x < 32050; x++)
      for(Integer y = 31950; y < 32050; y++)
        this.put(this.getTilePositionKey(x, y, z),
          new TileWithItems().setTile(Tile.GRASS));
  }

  public Packet writeMapInfo(Long identifier, Position position, Packet packet) {
    ProcessingLoginProtocol.writePosition(position, packet);
    Integer skip = 0;
    for(Integer offsetZ = 7; offsetZ > -1; offsetZ--)
      for(Integer offsetX = 0; offsetX < SCREEN_WIDTH; offsetX++)
        for(Integer offsetY = 0; offsetY < SCREEN_HEIGHT; offsetY++) {
          final String tileKey = this.getTilePositionKey(
            offsetX + position.getX() + OFFSET_X,
            offsetY + position.getY() + OFFSET_Y,
            offsetZ + position.getZ());
          final Optional<TileWithItems> tile = Optional.ofNullable(this.get(tileKey));
          if(tile.isPresent()) {
            if(skip > 0) packet.writeByte(skip - 1).writeByte(0xff);
            skip = 1;
            packet.writeInt16(tile.get().getTile().getCode());
            //TODO: Logica dos itens e criaturas
          }
          else if(skip == 0xfe) {
            packet.writeByte(skip).writeByte(0xff);
            skip = 0;
          }
          else skip++;
        }
    return skip > 0 ? packet.writeByte(skip - 1).writeByte(0xff) : packet;
  }

}
