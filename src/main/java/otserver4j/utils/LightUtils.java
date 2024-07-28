package otserver4j.utils;

import otserver4j.entity.PlayerCharacterEntity;
import otserver4j.structure.Light;
import otserver4j.structure.Position;

public class LightUtils {
  private LightUtils() { }
  public static final LightUtils INSTANCE = new LightUtils();
  public Light fromPlayer(PlayerCharacterEntity player) {
    return new Light().setColor((byte) 0xd7).setRadius((byte) 0x07);
  }
  public Light fromWorld(Object gameMap, Position position) {
    return new Light().setColor((byte) 0xd7).setRadius((byte) 0xff);
  }
}
