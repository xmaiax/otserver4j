package otserver4j.structure;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import otserver4j.packet.Packet;
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

  public Packet writeMapInfo(PlayerCharacter player, Packet packet) {
    for(int i = 0; i < 251; i++) {
      packet.writeByte(106);
      packet.writeByte(0); // Ground objects
      if(i == 118) { // Player position
        packet.writeByte(97);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(16);
        packet.writeString(player.getName());
        packet.writeByte(player.getLife().getValue() * 100 / player.getLife().getMaxValue());
        packet.writeByte(2);
        packet.writeByte(128);
        packet.writeByte(10);
        packet.writeByte(20);
        packet.writeByte(30);
        packet.writeByte(40);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
        packet.writeByte(0);
      }
      else packet.writeByte(0);
      packet.writeByte(0xff);
    }
    packet.writeByte(106);
    packet.writeByte(0);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(0xff);
    packet.writeByte(228);
    packet.writeByte(0xff);
    return packet;
  }

}
