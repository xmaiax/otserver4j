package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum PlayerCharacterParty {
  NONE(0x00), REQUESTED(0x02), ACCEPTED(0x03), LEADER(0x04);
  private final Integer code;
  public static PlayerCharacterParty fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(PlayerCharacterParty.values()).stream()
      .filter(pt -> pt.getCode().equals(code)).findFirst().orElse(
          PlayerCharacterParty.values()[java.math.BigInteger.ZERO.intValue()]); }
}
