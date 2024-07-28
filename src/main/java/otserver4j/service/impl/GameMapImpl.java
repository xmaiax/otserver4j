package otserver4j.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.packet.RawPacket;
import otserver4j.structure.Position;
import otserver4j.structure.TileType;

@Slf4j @Getter @org.springframework.stereotype.Service
public class GameMapImpl extends HashMap<Long, otserver4j.service.impl.GameMapImpl.OTBMTile> implements otserver4j.service.GameMap {

  private static final long serialVersionUID = -1L;

  @Data @Accessors(chain = true) public static class OTBMTile {
    private Integer tileCode;
    private List<Integer> groundItems;
  }

  private static final int
    OTBM_FILE_HEADER_ZERO = BigInteger.ZERO.intValue()
   ,OTBM_FILE_HEADER_OTBM = (((byte) 'M') << 24) | (((byte) 'B') << 16) | (((byte) 'T') << 8) | ((byte) 'O')
   ,NODE_ESC = 0xfd
   ,NODE_START = 0xfe
   ,NODE_END = 0xff
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

  private void rewind1byte(final ByteBuffer input) {
    final int currentPosition = input.position() - BigInteger.ONE.intValue();
    input.rewind(); input.position(currentPosition);
  }

  private void parseMapInfo(final ByteBuffer input) {
    switch(RawPacket.readByte(input)) {
      case 0x01:
        this.description = (this.description == null ? "" : this.description)
          .concat(System.lineSeparator()).concat(RawPacket.readString(input)).trim();
        this.parseMapInfo(input); break;
      case 0x0b:
        this.spawnFile = RawPacket.readString(input);
        this.parseMapInfo(input); break;
      case 0x0d:
        this.houseFile = RawPacket.readString(input);
        this.parseMapInfo(input); break;
      default: this.rewind1byte(input);
    }
  }

  private Integer currentTileAreaX, currentTileAreaY, currentTileAreaZ;
  private Long currentTileX, currentTileY, currentTileZ;
  private Integer lastTileCode;

  private void parseTileArea(final ByteBuffer input) {
    this.currentTileAreaX = RawPacket.readInt16(input);
    this.currentTileAreaY = RawPacket.readInt16(input);
    this.currentTileAreaZ = RawPacket.readByte(input);
    this.currentTileX = null;
    this.currentTileY = null;
    this.currentTileZ = null;
  }

  public static final int PORTAL = 1387;

  private void parseNodeFromTile(final ByteBuffer input, final OTBMTile tile) {
    while(true) {
      final int tileStuff = RawPacket.readByte(input);
      switch(tileStuff) {
        case 0x06:
          final int groundItem = RawPacket.readInt16(input);
          int nextByte = RawPacket.readByte(input);
          if(nextByte == NODE_END) this.rewind1byte(input);
          else if(groundItem == PORTAL) {
            final List<Integer> portalData = new ArrayList<>();
            while(nextByte != NODE_END) {
              portalData.add(nextByte);
              nextByte = RawPacket.readByte(input);
            }
            this.rewind1byte(input);
            log.debug("PORTAL (x={}, y={}, z={}): {}",
              this.currentTileX, this.currentTileY, this.currentTileZ, portalData);
          }
          else {
            if(nextByte < BigInteger.ONE.intValue()) {
              //FIXME Replace value on load?
              log.debug("ITEM ZERO QUANTITY (x={}, y={}, z={}): {}",
                this.currentTileX, this.currentTileY, this.currentTileZ, groundItem);
            }
          }
          break;
        case NODE_ESC: break;
        case NODE_END: break;
        default:
          if(!TileType.VOID.getCode().equals(this.lastTileCode))
           log.debug("Unknown tile stuff (x={}, y={}, z={}): {}",
             this.currentTileX, this.currentTileY, this.currentTileZ, tileStuff);
      }
      if(tileStuff == NODE_END) break;
    }
  }

  private Integer parseOTBMTileCodeToClientCode(final Integer code) {
    return code;
  }

  private void parseTile(final ByteBuffer input) {
    final long x = this.currentTileAreaX + RawPacket.readByte(input),
               y = this.currentTileAreaY + RawPacket.readByte(input),
               z = this.currentTileAreaZ;
    this.currentTileX = x; this.currentTileY = y; this.currentTileZ = z;
    final OTBMTile tile = new OTBMTile().setTileCode(TileType.VOID.getCode());
    while(true) {
      final int tileAttributes = RawPacket.readByte(input);
      switch(tileAttributes) {
        case 0x09: tile.setTileCode(this
          .parseOTBMTileCodeToClientCode(RawPacket.readInt16(input)));
          this.lastTileCode = tile.getTileCode();
        break;
        case NODE_START: this.parseNodeFromTile(input, tile); break;
        case NODE_ESC: break;
        case NODE_END: break;
        default: log.debug("Unknown tile attribute (x={}, y={}, z={}): {}",
          this.currentTileX, this.currentTileY, this.currentTileZ, tileAttributes);
      }
      if(tileAttributes == NODE_END) break;
    }
    this.put(Position.toLong(x, y, z), tile);
  }

  private void parseNode(final ByteBuffer buffer) {
    final int currentByte = RawPacket.readByte(buffer);
    if(currentByte == NODE_START) {
      final int nodeType = RawPacket.readByte(buffer);
      switch(nodeType) {
        case 0x00: this.parseMapHeader(buffer); return;
        case 0x02: this.parseMapInfo(buffer); return;
        case 0x04: this.parseTileArea(buffer); return;
        case 0x05: this.parseTile(buffer); return;
        default: log.warn("Unknown node type: {}", nodeType);
      }
    }
    else if(currentByte != NODE_END) {
      log.warn("Unknown byte from OTBM file: {}", currentByte);
    }
  }

  @SuppressWarnings("unused")
  private void loadOtbmContentFromFile(final File file) {
    try(final RandomAccessFile raFile = new RandomAccessFile(file.getAbsolutePath(), "r");
        final FileChannel fileChannel = raFile.getChannel();) {
      final MappedByteBuffer buffer = fileChannel.map(
        FileChannel.MapMode.READ_ONLY, BigInteger.ZERO.intValue(), fileChannel.size());
      buffer.load();
      final Integer mapIdentifier = RawPacket.readInt32(buffer);
      if(!mapIdentifier.equals(OTBM_FILE_HEADER_ZERO) && !mapIdentifier.equals(OTBM_FILE_HEADER_OTBM))
        throw new RuntimeException("Invalid map identifier: ".concat(mapIdentifier.toString()));
      while(buffer.hasRemaining()) this.parseNode(buffer);
      buffer.clear();
    }
    catch(IOException ioExcp) {
      log.error("Failed to load OTBM map: {}", ioExcp.getMessage(), ioExcp);
      throw new RuntimeException("Failed to load OTBM map.", ioExcp);
    }
  }

  private URL getUrlFromResource(String resource) throws FileNotFoundException {
    if(resource == null || resource.isBlank()) throw new IllegalArgumentException("The OTBM file path is blank or null.");
    final URL url = Thread.currentThread().getContextClassLoader().getResource(resource.trim());
    if(url == null) throw new FileNotFoundException("Resource not found: ".concat(resource));
    return url;
  }

  public GameMapImpl(@Value("map/rookgaard.otbm") final String otbmFile) throws IOException {
    log.info("Loading game map...");
    final URL url = this.getUrlFromResource(otbmFile);
    System.err.println("URL: " + url.toString());
    final File file = new File(url.getFile());
    System.err.println("Absolute path: " + file.getAbsolutePath());
    System.err.println("is file: " + file.isFile());
    //this.loadOtbmContentFromFile(file);
    log.info("Game map load complete!");
  }

  public void writeTileFromPosition(final Position position, final RawPacket packet) {
    this.writeTileFromPosition(position.getX(), position.getY(), position.getZ(), packet);
  }

  @Override public void writeTileFromPosition(
      final int x, final int y, final int z, RawPacket packet) {
    if(x > 0 && x < 100 && y > 0 && y < 100 && z == 7) {
      packet.writeInt16(TileType.GRASS1.getCode());
    }
    else {
      //?
    }
    /*
    final TileOutput tile = this.getTile(x, y, z);
    packet.writeInt16(tile.getCode());
    if(tile.getItems() != null && !tile.getItems().isEmpty()) {
      packet.writeInt16(tile.getItems().get(0));
    }
    */
  }

}
