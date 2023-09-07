package otserver4j.consumer.converter;

@lombok.Getter
public enum PacketType {
  LOAD_CHARACTER_LIST(0x01, otserver4j.consumer.converter.wrapper.LoadCharacterListPacketWrapper.class),
  LOGIN_SUCCESS(0x0a, null),
  LOGOFF(0x14, null),
  KEEP_CONECTED(0x1e, null),
  AUTOWALK_ON(0x64, null),
  MOVE_NORTH(0x65, null),
  MOVE_EAST(0x66, null),
  MOVE_SOUTH(0x67, null),
  MOVE_WEST(0x68, null),
  AUTOWALK_OFF(0x69, null),
  MOVE_NORTHEAST(0x6a, null),
  MOVE_SOUTHEAST(0x6b, null),
  MOVE_SOUTHWEST(0x6c, null),
  MOVE_NORTHWEST(0x6d, null),
  TURN_NORTH(0x6f, null),
  TURN_EAST(0x70, null),
  TURN_SOUTH(0x71, null),
  TURN_WEST(0x72, null),
  MOVE_ITEM(0x78, null),
  TRADE_REQUEST(0x7d, null),
  LOOK_TRADE_ITEM(0x7e, null),
  ACCEPT_TRADE(0x7f, null),
  CANCEL_TRADE(0x80, null),
  USE_ITEM(0x82, null),
  USE_ITEM_2(0x83, null),
  BATTLE_WINDOW(0x84, null),
  TURN_ITEM(0x85, null),
  CLOSE_CONTAINER(0x87, null),
  BACK_CONTAINER_BROWSER(0x88, null),
  TEXT_WINDOW(0x89, null),
  HOUSE_WINDOW(0x8a, null),
  LOOK(0x8c, null),
  TALK(0x96, null),
  REQUEST_CHANNEL(0x97, null),
  OPEN_CHANNEL(0x98, null),
  CLOSE_CHANNEL(0x99, null),
  OPEN_PRIVATE_CHANNEL(0x9a, null),
  BATTLE_MODE(0xa0, null),
  ATTACK(0xa1, null),
  FOLLOW(0xa2, null),
  GROUP_INVITE(0xa3, null),
  GROUP_INVITE_ACCEPT(0xa4, null),
  REMOVE_INVITE_GROUP(0xa5, null),
  PASS_GROUP_LEADERSHIP(0xa6, null),
  LEAVE_GROUP(0xa7, null),
  CREATE_PRIVATE_CHANNEL(0xaa, null),
  CHANNEL_INVITE(0xab, null),
  DELETE_CHANNEL(0xac, null),
  MOVEMENT_CANCEL(0xbe, null),
  CLIENT_REQUEST_TILES_RESEND(0xc9, null),
  CLIENT_REQUEST_CONTAINER_RESEND(0xca, null),
  OUTFIT_SCREEN(0xd2, null),
  APPLY_OUTFIT(0xd3, null),
  ADD_FRIEND(0xdc, null),
  REMOVE_FRIEND(0xdd, null),
  INVALID(-1, null);

  private final Integer code;
  private final Class<?> objectClass;

  PacketType(Integer code, Class<?> objectClass) {
    this.code = code;
    this.objectClass = objectClass;
  }

  public static PacketType fromCode(Integer code) {
    return java.util.Arrays.asList(PacketType.values()).stream()
      .filter(pt -> pt.getCode().equals(code) || INVALID.equals(pt)).findFirst().get(); }

}
