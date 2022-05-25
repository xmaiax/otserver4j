package otserver4j.utils;

import otserver4j.structure.GameMap;
import otserver4j.structure.Light;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.Position;

//TODO: Implementar cálculo de iluminação
public class LightUtils {
  private LightUtils() { }
  private static final LightUtils INSTANCE = new LightUtils();
  public static LightUtils getInstance() { return INSTANCE; }

  public Light fromPlayer(PlayerCharacter player) {
    return new Light().setColor((byte) 0xd7).setRadius((byte) 0x07);
  }

  public Light fromWorld(GameMap gameMap, Position position) {
    return new Light().setColor((byte) 0xd7).setRadius((byte) 0xff);
  }

}
