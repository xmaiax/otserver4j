package otserver4j.action.impl;

import static java.math.BigInteger.ZERO;

import java.util.Collections;

import otserver4j.consumer.converter.RawPacket;
import otserver4j.structure.Item;
import otserver4j.structure.Position;
import otserver4j.structure.Tile;
import otserver4j.structure.PlayerCharacter.Slot;

@org.springframework.stereotype.Component
public class LookAction implements otserver4j.action.Action {

  @org.springframework.beans.factory.annotation.Autowired
  private otserver4j.structure.GameMap gameMap;

  @Override public RawPacket execute(otserver4j.consumer.converter.PacketType type,
      java.nio.ByteBuffer buffer, java.nio.channels.SocketChannel channel,
      otserver4j.structure.PlayerCharacter player) {
    final Position position = new Position().setX(RawPacket.readInt16(buffer))
      .setY(RawPacket.readInt16(buffer)).setZ(RawPacket.readByte(buffer));
    final Item item = Item.fromCode(RawPacket.readInt16(buffer));
    final Boolean isOnCharacter = RawPacket.readByte(buffer).equals(ZERO.intValue());
    final Tile.TileWithItems tileWithItems = isOnCharacter ?
      new Tile.TileWithItems().setTile(Tile.NOTHING).setItems(Collections.singletonList(
        player.getInventory().get(Slot.fromCode(position.getY())))) :
          this.gameMap.getTileWithItemsFromPosition(position);
    final String description = tileWithItems.getItems() == null || tileWithItems.getItems().isEmpty() ?
      tileWithItems.getTile().toString() : (Item.NO_ITEM.equals(item) || tileWithItems.getItems()
        .stream().filter(i -> item.equals(i.getItem())).findAny().isEmpty() ?
          Tile.NOTHING.toString() : tileWithItems.getLookedItemInfo(item));
    return new RawPacket().writeByte(RawPacket.CODE_SEND_MESSAGE)
      .writeByte(otserver4j.structure.Chat.MessageType.INFO.getCode())
      .writeString(String.format("You see %s.", description));
  }

}
