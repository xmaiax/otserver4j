package otserver4j.service;

import otserver4j.entity.PlayerCharacter;
import otserver4j.exception.AccountException;

public interface PlayerCharacterService {
  PlayerCharacter findPlayerCharacter(Integer accountNumber, String characterName) throws AccountException;
}
