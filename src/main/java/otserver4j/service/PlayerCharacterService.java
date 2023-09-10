package otserver4j.service;

import otserver4j.exception.AccountException;
import otserver4j.structure.PlayerCharacter;

public interface PlayerCharacterService {
  PlayerCharacter findPlayerCharacter(Integer accountNumber, String characterName) throws AccountException;
}
