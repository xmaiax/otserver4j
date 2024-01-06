package otserver4j.structure;

@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE) @lombok.Getter
public enum PacketType {
  LOAD_CHARACTER_LIST(0x01),
  LOGIN_SUCCESS(0x0a),
  LOGOFF(0x14),
  KEEP_CONECTED(0x1e),
  AUTOWALK_ON(0x64),
  MOVE_NORTH(0x65),
  MOVE_EAST(0x66),
  MOVE_SOUTH(0x67),
  MOVE_WEST(0x68),
  AUTOWALK_OFF(0x69),
  MOVE_NORTHEAST(0x6a),
  MOVE_SOUTHEAST(0x6b),
  MOVE_SOUTHWEST(0x6c),
  MOVE_NORTHWEST(0x6d),
  TURN_NORTH(0x6f),
  TURN_EAST(0x70),
  TURN_SOUTH(0x71),
  TURN_WEST(0x72),
  MOVE_ITEM(0x78),
  TRADE_REQUEST(0x7d),
  LOOK_TRADE_ITEM(0x7e),
  ACCEPT_TRADE(0x7f),
  CANCEL_TRADE(0x80),
  USE_ITEM(0x82),
  USE_ITEM_2(0x83),
  BATTLE_WINDOW(0x84),
  TURN_ITEM(0x85),
  CLOSE_CONTAINER(0x87),
  BACK_CONTAINER_BROWSER(0x88),
  TEXT_WINDOW(0x89),
  HOUSE_WINDOW(0x8a),
  LOOK(0x8c),
  TALK(0x96),
  REQUEST_CHANNEL(0x97),
  OPEN_CHANNEL(0x98),
  CLOSE_CHANNEL(0x99),
  OPEN_PRIVATE_CHANNEL(0x9a),
  BATTLE_MODE(0xa0),
  ATTACK(0xa1),
  FOLLOW(0xa2),
  GROUP_INVITE(0xa3),
  GROUP_INVITE_ACCEPT(0xa4),
  REMOVE_INVITE_GROUP(0xa5),
  PASS_GROUP_LEADERSHIP(0xa6),
  LEAVE_GROUP(0xa7),
  CREATE_PRIVATE_CHANNEL(0xaa),
  CHANNEL_INVITE(0xab),
  DELETE_CHANNEL(0xac),
  MOVEMENT_CANCEL(0xbe),
  CLIENT_REQUEST_TILES_RESEND(0xc9),
  CLIENT_REQUEST_CONTAINER_RESEND(0xca),
  OUTFIT_SCREEN(0xd2),
  APPLY_OUTFIT(0xd3),
  ADD_FRIEND(0xdc),
  REMOVE_FRIEND(0xdd),
  INVALID(-1);

  private final Integer code;
  public static PacketType fromCode(final Integer code) {
    return code == null ? null : java.util.Arrays.asList(PacketType.values())
      .stream().filter(pt -> pt.getCode().equals(code) || INVALID.equals(pt))
        .findFirst().get(); }
  @Override public String toString() { return String.format(
    "%s (0x%02X)", this.name(), this.getCode()); }
}
