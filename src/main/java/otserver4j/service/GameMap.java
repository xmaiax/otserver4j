package otserver4j.service;

public interface GameMap {
  otserver4j.packet.RawPacket writeTileFromPosition(
    final int x, final int y, final int z,
    final otserver4j.packet.RawPacket packet);
  otserver4j.packet.RawPacket writeTileFromPosition(
    final otserver4j.structure.Position position,
    final otserver4j.packet.RawPacket packet);
}
