package otserver4j.service.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.entity.PlayerCharacterEntity;
import otserver4j.factory.SpawnPlayerCharacterPacketFactory;
import otserver4j.structure.Direction;
import otserver4j.structure.PlayerCharacterParty;
import otserver4j.structure.Position;
import otserver4j.structure.RawPacket;
import otserver4j.structure.TileType;

@Slf4j @Getter @org.springframework.stereotype.Service
public class GameMapImpl implements otserver4j.service.GameMap {

  private static final String FULL_PATH = "C:\\Users\\felip\\workspace\\otserver4j\\src\\main\\resources\\map\\just-rookgaard.otbm";

  private static final int
    OTBM_FILE_HEADER_ZERO = BigInteger.ZERO.intValue()
   ,OTBM_FILE_HEADER_OTBM = ((byte) 'O') | (((byte) 'T') << 8) | (( (byte) 'B') << 16) | (((byte) 'M') << 24)
   ,NODE_ESC = 0xfd
   ,NODE_START = 0xfe
   ,NODE_END = 0xff
   ,OTBM_MAP_HEADER = 0x00
   ,OTBM_MAP_INFO = 0x02
   ,OTBM_TILE_AREA = 0x04
   ,OTBM_TILE = 0x05
   ,OTBM_ITEM = 0x06
   ,OTBM_TOWNS = 0x0c
   ,OTBM_TOWN = 0x0d
   ,OTBM_HOUSETILE = 0x0e
   ,OTBM_WAYPOINTS = 0x0f
   ,OTBM_WAYPOINT = 0x10
   ,OTBM_ATTR_DESCRIPTION = 0x01
   ,OTBM_ATTR_EXT_FILE = 0x02
   ,OTBM_ATTR_TILE_FLAGS = 0x03
   ,OTBM_ATTR_ACTION_ID = 0x04
   ,OTBM_ATTR_UNIQUE_ID = 0x05
   ,OTBM_ATTR_TEXT = 0x06
   ,OTBM_ATTR_DESC = 0x07
   ,OTBM_ATTR_TELE_DEST = 0x08
   ,OTBM_ATTR_ITEM = 0x09
   ,OTBM_ATTR_DEPOT_ID = 0x0a
   ,OTBM_ATTR_EXT_SPAWN_FILE = 0x0b
   ,OTBM_ATTR_EXT_HOUSE_FILE = 0x0d
   ,OTBM_ATTR_HOUSEDOORID = 0x0e
   ,OTBM_ATTR_COUNT = 0x0f
   ,OTBM_ATTR_RUNE_CHARGES = 0x16
   ,CODE_MAP_INFO = 0x64
   ;

  private Integer version;
  private Integer width;
  private Integer height;
  private Integer itemsMajorVersion;
  private Integer itemsMinorVersion;

  private void parseMapHeader(final ByteBuffer buffer) {
    this.version = RawPacket.readInt32(buffer);
    this.width = RawPacket.readInt16(buffer);
    this.height = RawPacket.readInt16(buffer);
    this.itemsMajorVersion = RawPacket.readInt32(buffer);
    this.itemsMinorVersion = RawPacket.readInt32(buffer);
  }

  private String description;
  private String spawnFile;
  private String houseFile;

  private void rewindToPreviousByte(final ByteBuffer input) {
    final int currentPosition = input.position() - BigInteger.ONE.intValue();
    input.rewind();
    input.position(currentPosition);
  }

  private void parseMapInfo(final ByteBuffer input) {
    switch(RawPacket.readByte(input)) {
      case OTBM_ATTR_DESCRIPTION:
        this.description = (Objects.isNull(this.description) ? "" : this.description)
          .concat(System.lineSeparator()).concat(RawPacket.readString(input)).trim();
        this.parseMapInfo(input);
        break;
      case OTBM_ATTR_EXT_SPAWN_FILE:
        this.spawnFile = RawPacket.readString(input);
        this.parseMapInfo(input);
        break;
      case OTBM_ATTR_EXT_HOUSE_FILE:
        this.houseFile = RawPacket.readString(input);
        this.parseMapInfo(input);
        break;
      default:
        this.rewindToPreviousByte(input);
    }
  }

  @Data @Accessors(chain = true)
  public static final class Tile {
    private Integer x;
    private Integer y;
    private Integer code;
    private List<Integer> items;
  }

  @Data @Accessors(chain = true)
  public static final class TileArea {
    private Integer x;
    private Integer y;
    private Integer z;
    private List<Tile> tiles;
  }

  @Data @Accessors(chain = true)
  public static final class TileOutput {
    private Integer x;
    private Integer y;
    private Integer z;
    private Integer code;
    private List<Integer> items;
  }

  private List<TileArea> tileAreas = new ArrayList<>();

  private void parseTileArea(final ByteBuffer input) {
    this.tileAreas.add(new TileArea()
      .setX(RawPacket.readInt16(input))
      .setY(RawPacket.readInt16(input))
      .setZ(RawPacket.readByte(input))
      .setTiles(new ArrayList<>())
    );
  }

  private void parseNodeFromTile(final ByteBuffer input, final Tile tile) {
    while(true) {
      final int temp = RawPacket.readByte(input);
      switch(temp) {
        case OTBM_ITEM:
          if(Objects.isNull(tile.getItems())) {
            tile.setItems(new ArrayList<>());
          }
          tile.getItems().add(RawPacket.readInt16(input));
          break;
        case NODE_END: break;
        default:
          System.err.println(temp);
      }
      if(temp == NODE_END) break;
    }
  }

  private Integer convertServerItemIdToClientItemId(final Integer serverItemId) {
    switch(serverItemId) {
      case 430: return 435;
      case 724: return 870;
      case 1368: return 1930;
      case 1369: return 1931;
      default: return serverItemId;
    }
  }

  private void parseTile(final ByteBuffer input) {
    final TileArea lastTileArea = this.tileAreas.get(this.tileAreas.size() - BigInteger.ONE.intValue());
    final Tile tile = new Tile().setX(RawPacket.readByte(input)).setY(RawPacket.readByte(input));
    while(true) {
      final int temp = RawPacket.readByte(input);
      switch(temp) {
        case OTBM_ATTR_ITEM: tile.setCode(this.convertServerItemIdToClientItemId(RawPacket.readInt16(input))); break;
        case NODE_START: this.parseNodeFromTile(input, tile); break;
        case NODE_END: break;
        default:
          System.err.println(temp);
      }
      if(temp == NODE_END) break;
    }
    lastTileArea.getTiles().add(tile);
  }

  //https://otland.net/threads/a-comphrensive-description-of-the-otbm-format.258583/
  private void parseNode(final ByteBuffer buffer) {
    final int _byte = RawPacket.readByte(buffer);
    if(_byte == NODE_START) {
      final int _byte2 = RawPacket.readByte(buffer);
      switch(_byte2) {
        case OTBM_MAP_HEADER: this.parseMapHeader(buffer); return;
        case OTBM_MAP_INFO: this.parseMapInfo(buffer); return;
        case OTBM_TILE_AREA: this.parseTileArea(buffer); return;
        case OTBM_TILE: this.parseTile(buffer); return;
      }
    }
    else {
      //System.err.println(_byte);
    }
  }

  public GameMapImpl() throws IOException {
    log.info("Loading game map...");
    try(final RandomAccessFile raFile = new RandomAccessFile(FULL_PATH, "r");
        final FileChannel fileChannel = raFile.getChannel();) {
      final MappedByteBuffer buffer = fileChannel.map(
        FileChannel.MapMode.READ_ONLY, BigInteger.ZERO.intValue(), fileChannel.size());
      buffer.load();
      final Integer mapIdentifier = RawPacket.readInt32(buffer);
      if(!mapIdentifier.equals(OTBM_FILE_HEADER_ZERO) || mapIdentifier.equals(OTBM_FILE_HEADER_OTBM))
        throw new RuntimeException("Invalid map identifier: ".concat(mapIdentifier.toString()));
      while(buffer.hasRemaining()) this.parseNode(buffer);
      this.tiles = this.tileAreas.stream().flatMap(ta -> {
        return ta.getTiles().stream().map(t -> {
          TileOutput to = new TileOutput();
          to.setX(ta.getX() + t.getX());
          to.setY(ta.getY() + t.getY());
          to.setZ(ta.getZ());
          to.setCode(t.getCode());
          return to;
        });
      }).collect(Collectors.toList());
      buffer.clear();
    }
    catch(IOException ioExcp) {
      log.error("AAAAAAAAAAAAAAAAAAAAA: {}", ioExcp.getMessage(), ioExcp);
    }
    
    log.info("Game map load complete!");
  }

  private List<TileOutput> tiles;
  public TileOutput getTile(Integer x, Integer y, Integer z) {
    List<TileOutput> temp = this.tiles.stream().filter(t -> t.getX().equals(x) && t.getY().equals(y) && t.getZ().equals(z)).collect(Collectors.toList());
    return temp.isEmpty() ? new TileOutput().setCode(TileType.DIRT.getCode()) : temp.get(0);
  }

  private void writeTileFromCoordinates(final Integer x, final Integer y, final Integer z,
      final RawPacket packet) {
    final TileOutput tile = this.getTile(x, y, z);
    packet.writeInt16(tile.getCode());
    if(tile.getItems() != null && !tile.getItems().isEmpty()) {
      packet.writeInt16(tile.getItems().get(0));
    }
  }

  @Override public RawPacket writeSpawnMapInfo(
      final PlayerCharacterEntity playerCharacter, final RawPacket packet) {
    final Position bounds = new Position().setZ(playerCharacter.getPosition().getZ())
      .setX(playerCharacter.getPosition().getX() + 9).setY(playerCharacter.getPosition().getY() + 7);
    for(Integer x = playerCharacter.getPosition().getX() - 8; x <= bounds.getX(); x++)
      for(Integer y = playerCharacter.getPosition().getY() - 6; y <= bounds.getY(); y++) {
        this.writeTileFromCoordinates(x, y, playerCharacter.getPosition().getZ(), packet);
        if(playerCharacter.getPosition().getX().equals(x) && playerCharacter.getPosition().getY().equals(y)) {
          packet.writeInt16(0x61); // Criatura desconhecida
          packet.writeInt32(0x00L); // Cache de criatura?
          packet.writeInt32(SpawnPlayerCharacterPacketFactory.PLAYER_IDENTIFIER_PREFIX + playerCharacter.getIdentifier());
          packet.writeString(playerCharacter.getName());
          packet.writeByte(playerCharacter.getCurrentLife() * 100 / playerCharacter.getMaxLife());
          packet.writeByte((playerCharacter.getDirection() != null && playerCharacter.getDirection().getSpawnable() ?
              playerCharacter.getDirection() : Direction.SOUTH).getCode());
          
          packet.writeByte(playerCharacter.getOutfit().getType());
          packet.writeByte(playerCharacter.getOutfit().getHead());
          packet.writeByte(playerCharacter.getOutfit().getBody());
          packet.writeByte(playerCharacter.getOutfit().getLegs());
          packet.writeByte(playerCharacter.getOutfit().getFeet());
          packet.writeByte(playerCharacter.getOutfit().getExtra());
          packet.writeInt16(0xff88); //playerCharacter.getSpeed()
          packet.writeByte(0x00); // +Velocidade
          packet.writeByte(playerCharacter.getSkull().getCode());
          packet.writeByte(PlayerCharacterParty.NONE.getCode());
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
