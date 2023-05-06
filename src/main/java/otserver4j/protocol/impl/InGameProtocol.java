package otserver4j.protocol.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import otserver4j.exception.InGameException;
import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;
import otserver4j.protocol.Protocol;
import otserver4j.structure.Chat;
import otserver4j.structure.Chat.ChatType;
import otserver4j.structure.Direction;
import otserver4j.structure.GameMap;
import otserver4j.structure.Item;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.Position;
import otserver4j.structure.Tile;
import otserver4j.structure.Tile.TileWithItems;
import otserver4j.utils.DateFormatUtils;

@Component @Slf4j
public class InGameProtocol implements Protocol {

  private static final Integer SNAPBACK_CODE = 0xb5;

  @Autowired private GameMap gameMap;

  private Packet snapback(SocketChannel channel, PlayerCharacter player) {
    final Packet packet = new Packet();
    packet.writeByte(SNAPBACK_CODE);
    packet.writeByte(player.getDirection().getCode());
    try { packet.send(channel); }
    catch (IOException ioex) {
      log.error("Tururuuuuu: {}", ioex.getMessage(), ioex);
    }
    return new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.DISCREET_STATUS.getCode())
      .writeString(" ");
  }

  private Packet addMessageToClientLog(PlayerCharacter player,
      ChatType chatType, String message) {
    return new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(Chat.MessageType.YELLOW.getCode())
      .writeString(String.format("[%s] %s %s: %s",
        DateFormatUtils.getInstance().timeNow(), player.getName(),
          ChatType.SAY.equals(chatType) ? "says" :
            ChatType.YELL.equals(chatType) ? "yells" : "whispers", message));
  }

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key,
      SocketChannel channel, PacketType type) throws InGameException {
    final PlayerCharacter player = (PlayerCharacter) key.attachment();
    switch(type) {
      case TALK: return this.addMessageToClientLog(player,
        ChatType.fromCode(Packet.readByte(buffer)), Packet.readString(buffer));

      case TURN_NORTH: return this.snapback(channel, player.setDirection(Direction.NORTH));
      case TURN_SOUTH: return this.snapback(channel, player.setDirection(Direction.SOUTH));
      case TURN_EAST: return this.snapback(channel, player.setDirection(Direction.EAST));
      case TURN_WEST: return this.snapback(channel, player.setDirection(Direction.WEST));

      case MOVE_NORTH:
        //...
        return this.snapback(channel, player.setDirection(Direction.NORTH));
      case MOVE_SOUTH:
        //...
        return this.snapback(channel, player.setDirection(Direction.SOUTH));
      case MOVE_EAST:
        //...
        return this.snapback(channel, player.setDirection(Direction.EAST));
      case MOVE_WEST:
        //...
        return this.snapback(channel, player.setDirection(Direction.WEST));

      case LOOK:
        final Position position = new Position().setX(Packet.readInt16(buffer))
          .setY(Packet.readInt16(buffer)).setZ(Packet.readByte(buffer));
        final Item item = Item.fromCode(Packet.readInt16(buffer));
        final TileWithItems tileWithItems = this.gameMap.getTileWithItemsFromPosition(position);
        final String description = tileWithItems.getItems() == null || tileWithItems.getItems().isEmpty() ?
          tileWithItems.getTile().toString() : (Item.NO_ITEM.equals(item) || tileWithItems.getItems()
            .stream().filter(i -> item.equals(i.getItem())).findAny().isEmpty() ?
              Tile.NOTHING.toString() : tileWithItems.getLookedItemInfo(item));
        return new Packet().writeByte(Packet.CODE_SEND_MESSAGE)
          .writeByte(Chat.MessageType.INFO.getCode())
          .writeString(String.format("You see %s.", description));
      default:
        return this.snapback(channel, player);
    }
  }

}
