package otserver4j.utils;

import otserver4j.structure.GameMap;
import otserver4j.structure.Light;
import otserver4j.structure.PlayerCharacter;

//TODO: Implementar lógica de cálculo de iluminação
public class LightUtils {
  private LightUtils() { }
  private static final LightUtils INSTANCE = new LightUtils();
  public static LightUtils getInstance() { return INSTANCE; }

  public Light fromPlayer(PlayerCharacter player) {
    return new Light();
  }

  public Light fromWorld(GameMap gameMap) {
    return new Light();
  }

}
