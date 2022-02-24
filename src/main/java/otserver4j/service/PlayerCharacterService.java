package otserver4j.service;

import otserver4j.exception.LoginException;
import otserver4j.structure.PlayerCharacter;

public interface PlayerCharacterService {

  PlayerCharacter findPlayerCharacter(int accountNumber,
    String characterName) throws LoginException;

}
