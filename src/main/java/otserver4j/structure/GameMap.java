package otserver4j.structure;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import otserver4j.packet.Packet;
import otserver4j.protocol.impl.ProcessingLoginProtocol;
import otserver4j.structure.Tile.TileWithItems;

//FIXME FULL DOR NA BUNDA

@Component
public class GameMap extends HashMap<Position, TileWithItems> {
  private static final long serialVersionUID = -1L;
  public Packet writeMapInfo(Long indetifier, Position position, Packet packet) {
    return ProcessingLoginProtocol.writePosition(position, packet);
  }
}
