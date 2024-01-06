package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Getter public enum PlayerCharacterCondition {
  POISONED(0b00000001), ON_FIRE(0b00000010), STRUCK_BY_ENERGY(0b00000100),
  DRUNK(0b00001000), MAGIC_SHIELD_BUFF(0b00010000), PARALYZED(0b00100000),
  HASTE_BUFF(0b01000000), IN_BATTLE(0b10000000);
  private final Integer code;
  public static Integer getIconCodeFromStatuses(
      final java.util.List<PlayerCharacterCondition> conditions) {
    return conditions == null ? null : conditions.stream()
      .mapToInt(condition -> condition.getCode()).sum(); }
}
