package otserver4j.structure;

import java.util.Arrays;
import java.util.Collections;

import otserver4j.consumer.converter.RawPacket;
import otserver4j.protocol.impl.SpawnProtocol;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.Status.Party;
import otserver4j.structure.Tile.TileWithItems;

@org.springframework.stereotype.Component
public class GameMap extends java.util.HashMap<String, TileWithItems> {

  private static final long serialVersionUID = -1L;

  public GameMap() {
    final Integer z = 7;
    for(Integer x = 46; x < 55; x++)
      for(Integer y = 46; y < 55; y++) {
        final TileWithItems tile = new TileWithItems()
          .setTile(y == x ? Tile.GROUND : Tile.GRASS);
        if(x == 53 && y == 47) {
          tile.setItems(
            Arrays.asList(new ItemWithQuantity[] {
              new ItemWithQuantity().setItem(Item.CARLIN_SWORD),
              new ItemWithQuantity().setItem(Item.WARLORD_SWORD),
            })
          );
        }
        if(x == 52 && y == 46) {
          tile.setItems(Collections.singletonList(
            new ItemWithQuantity().setItem(Item.MAGIC_PLATE_ARMOR)
          ));
        }
        if(x == 53 && y == 46) {
          tile.setItems(
            Arrays.asList(new ItemWithQuantity[] {
              new ItemWithQuantity().setItem(Item.ARROW).setQuantity(15),
              new ItemWithQuantity().setItem(Item.CROSSBOW),
            })
          );
        }
        this.put(this.getTilePositionKey(x, y, z), tile);
      }
  }

  private String getTilePositionKey(Integer x, Integer y, Integer z) {
    return String.format("%d#%d#%d", x, y, z);
  }
  
  public TileWithItems getTileWithItemsFromPosition(Position position) {
    final String positionKey = this.getTilePositionKey(position.getX(), position.getY(), position.getZ());
    return this.containsKey(positionKey) ? this.get(positionKey) : new TileWithItems()
      .setTile(Tile.NOTHING).setItems(Collections.emptyList());
  }

  public RawPacket writeMapInfo(PlayerCharacter player, RawPacket packet) {
    final Position bounds = new Position().setZ(player.getPosition().getZ())
      .setX(player.getPosition().getX() + 9).setY(player.getPosition().getY() + 7);
    for(Integer x = player.getPosition().getX() - 8; x <= bounds.getX(); x++)
      for(Integer y = player.getPosition().getY() - 6; y <= bounds.getY(); y++) {
        final TileWithItems tileWithItems = this.get(this.getTilePositionKey(x, y, 7));
        if(tileWithItems != null) {
          packet.writeInt16(tileWithItems.getTile().getCode());
          if(tileWithItems.getItems() != null && !tileWithItems.getItems().isEmpty())
            tileWithItems.getItems().forEach(item -> {
              packet.writeInt16(item.getItem().getCode());
              if(item.getItem().isStackable()) packet.writeByte(item.getQuantity());
            });
        }
        else {
          //...
        }
        if(player.getPosition().getX().equals(x) && player.getPosition().getY().equals(y)) {
          packet.writeInt16(0x61); // Criatura desconhecida
          packet.writeInt32(0x00L); // Cache de criatura?
          packet.writeInt32(SpawnProtocol.PLAYER_IDENTIFIER_PREFIX + player.getIdentifier());
          packet.writeString(player.getName());
          packet.writeByte(player.getLife().getValue() * 100 / player.getLife().getMaxValue());
          packet.writeByte((player.getDirection() != null && player.getDirection().getSpawnable() ?
            player.getDirection() : Direction.SOUTH).getCode());
          packet.writeByte(player.getOutfit().getType());
          packet.writeByte(player.getOutfit().getHead());
          packet.writeByte(player.getOutfit().getBody());
          packet.writeByte(player.getOutfit().getLegs());
          packet.writeByte(player.getOutfit().getFeet());
          packet.writeByte(player.getOutfit().getExtra());
          packet.writeInt16(player.getSpeed());
          packet.writeByte(0x00); // +Velocidade
          packet.writeByte(player.getSkull().getCode());
          packet.writeByte(Party.NONE.getCode());
          packet.writeByte(0x00);
        }
        else if(bounds.getX().equals(x) && bounds.getY().equals(y)) {
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
        }
        else packet.writeByte(0);
        packet.writeByte(0xff);
      }
    return packet;
  }

}
