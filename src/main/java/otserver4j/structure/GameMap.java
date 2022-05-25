package otserver4j.structure;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import otserver4j.packet.Packet;
import otserver4j.protocol.impl.ProcessingLoginProtocol;
import otserver4j.structure.Status.Party;
import otserver4j.structure.Tile.TileWithItems;

@Component
public class GameMap extends HashMap<String, TileWithItems> {

  private static final long serialVersionUID = -1L;

  public static final Integer
    OFFSET_X = -8, OFFSET_Y = -6, SCREEN_WIDTH = 18, SCREEN_HEIGHT = 14;

  public static final String MAP_TILE_POSITION_SEPARATOR = "#";

  @SuppressWarnings("unused")
  private String getTilePositionKey(Integer x, Integer y, Integer z) {
    return String.format("%d%s%d%s%d",
      x, MAP_TILE_POSITION_SEPARATOR,
      y, MAP_TILE_POSITION_SEPARATOR, z);
  }

  // y > 2 && y < 10 && x > 4 && x < 12
  public Packet writeMapInfo(PlayerCharacter player, Packet packet) {
    for(int i = 0, x = 0; x < 18; x++) {
      for(int y = 0; y < 14; y++, i++) {

        packet.writeInt16((y + 2 == x ? Tile.GROUND : Tile.FANCY_GRASS).getCode());

        if(i == 118) { // 118 é a posição x50 (8) y50 (6) z7
          packet.writeInt16(0x61); // Criatura desconhecida
          packet.writeInt32(0x00); // Cache de criatura?
          packet.writeInt32(ProcessingLoginProtocol.PLAYER_IDENTIFIER_PREFIX + player.getIdentifier());
          packet.writeString(player.getName());
          packet.writeByte(player.getLife().getValue() * 100 / player.getLife().getMaxValue());
          packet.writeByte((player.getDirection() != null && player.getDirection().getSpawnable() ?
            player.getDirection() : Direction.SOUTH).getCode());
          packet.writeByte(player.getOutfit().getType());
          packet.writeByte(player.getOutfit().getHead());
          packet.writeByte(player.getOutfit().getBody());
          packet.writeByte(player.getOutfit().getLegs());
          packet.writeByte(player.getOutfit().getFeet());
          packet.writeByte(player.getOutfit().getExtra());
          packet.writeInt16(player.getSpeed());
          packet.writeByte(0x00); // +Velocidade
          packet.writeByte(player.getSkull().getCode());
          packet.writeByte(Party.NONE.getCode());
          packet.writeByte(0x00);
        }
        else if(i == 251) { // 251 é o último SQM
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
          packet.writeByte(0xff);
        }
        else {
          packet.writeByte(0);
        }
        packet.writeByte(0xff);
      }
    }
    return packet;
  }

}
