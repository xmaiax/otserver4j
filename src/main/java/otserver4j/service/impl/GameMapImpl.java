package otserver4j.service.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.packet.RawPacket;
import otserver4j.structure.Position;

@Slf4j @Getter @org.springframework.stereotype.Service
public class GameMapImpl extends HashMap<Long,
    otserver4j.service.impl.GameMapImpl.TileWithItems> implements otserver4j.service.GameMap {

  private static final long serialVersionUID = -1L;

  @Data @Accessors(chain = true) public static class TileWithItems {
    private Position position;
    private Integer tile;
    private List<Integer> items;
  }

  private Long getPositionNode(final JsonNode positionNode) {
    return positionNode == null || positionNode.isNull() ||
      !positionNode.canConvertToLong() ? null : positionNode.asLong();
  }

  private Integer getTileFromJsonNode(final JsonNode tileNode) {
    return tileNode == null || tileNode.isNull() ||
      !tileNode.canConvertToInt() ? null : tileNode.asInt();
  }

  private List<Integer> getItemsFromJsonNode(final JsonNode itemsNode) {
    if(itemsNode == null || itemsNode.isNull() || !itemsNode.isArray() || itemsNode.isEmpty()) return null;
    final List<Integer> output = new ArrayList<>(itemsNode.size());
    itemsNode.iterator().forEachRemaining(itemNode -> output.add(itemNode.asInt()));
    return output;
  }

  public GameMapImpl(@Value("classpath:map/rookgaard.json") final Resource resourceFile,
      final ObjectMapper objectMapper) throws IOException {
    log.info("Loading game map...");
    final long time = System.currentTimeMillis();
    final AtomicInteger failedTilesCounter = new AtomicInteger();
    objectMapper.readTree(resourceFile.getURL()).iterator().forEachRemaining(jsonNode -> {
      try {
        final Long position = this.getPositionNode(jsonNode.get("position"));
        if(position != null) this.put(position, new TileWithItems()
          .setPosition(Position.fromLong(position))
          .setTile(this.getTileFromJsonNode(jsonNode.get("tile")))
          .setItems(this.getItemsFromJsonNode(jsonNode.get("items"))));
      }
      catch(Exception _ignore) { failedTilesCounter.getAndIncrement(); }
    });
    if(failedTilesCounter.get() < BigInteger.ONE.intValue())
      log.debug("No failed tiles loaded from map!");
    else log.warn("Failed tiles loaded from map: {} (Total: {})",
      failedTilesCounter.get(), failedTilesCounter.get() + this.size());
    log.info("Game map load complete in {}ms.", System.currentTimeMillis() - time);
  }

  @Override public RawPacket writeTileFromPosition(int x, int y, int z, RawPacket packet) {
    final TileWithItems tileWithItems = this.get(Position.toLong(x, y, z));
    return tileWithItems != null && tileWithItems.getTile() != null ?
      packet.writeInt16(tileWithItems.getTile()) : packet;
  }

  @Override public RawPacket writeTileFromPosition(Position position, RawPacket packet) {
    return this.writeTileFromPosition(position.getX(), position.getY(), position.getZ(), packet);
  }

}
