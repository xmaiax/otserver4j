package otserver4j.structure;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter public enum Direction {
  NORTH(0x00, Boolean.TRUE), EAST(0x01, Boolean.TRUE),
  SOUTH(0x02, Boolean.TRUE), WEST(0x03, Boolean.TRUE),
  NORTHEAST(0x04, Boolean.FALSE), NORTHWEST(0x05, Boolean.FALSE),
  SOUTHEAST(0x06, Boolean.FALSE), SOUTHWEST(0x07, Boolean.FALSE);
  private final Integer code;
  private final Boolean spawnable;
  public static Direction fromCode(Integer code) {
    return Arrays.asList(Direction.values()).stream().filter(dr -> dr.getCode()
      .equals(code)).findFirst().get();
  }
}
