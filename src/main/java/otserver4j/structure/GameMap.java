package otserver4j.structure;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import otserver4j.structure.TileType.TileWithItems;

@Component
public class GameMap extends HashMap<Position, TileWithItems> {
  private static final long serialVersionUID = -1L;
}
