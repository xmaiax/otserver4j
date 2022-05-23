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

  public Packet writeMapInfo(Long identifier, Position position, Packet packet) {
    ProcessingLoginProtocol.writePosition(position, packet);
    Integer skip = 0;
    for(Integer offsetZ = 7; offsetZ > -1; offsetZ--)
      for(Integer offsetX = 0; offsetX < SCREEN_WIDTH; offsetX++)
        for(Integer offsetY = 0; offsetY < SCREEN_HEIGHT; offsetY++) {
          final String tileKey = String.format("%d%s%d%s%d", offsetX, MAP_TILE_POSITION_SEPARATOR,
            offsetY, MAP_TILE_POSITION_SEPARATOR, offsetZ);
          final Optional<TileWithItems> tile = Optional.ofNullable(this.get(tileKey));
          if(tile.isPresent()) {
            if(skip > 0) packet.writeByte(skip - 1).writeByte(0xff);
            skip = 1;
            // AddTile
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
