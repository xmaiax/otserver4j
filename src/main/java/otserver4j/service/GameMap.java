package otserver4j.service;

import otserver4j.structure.RawPacket;

public interface GameMap {
  RawPacket writeSpawnMapInfo(
    final otserver4j.entity.PlayerCharacterEntity playerCharacter, final RawPacket packet);
}
