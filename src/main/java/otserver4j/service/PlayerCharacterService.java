package otserver4j.service;

public interface PlayerCharacterService {
  otserver4j.structure.PlayerCharacter findPlayerCharacter(Integer accountNumber,
    String characterName) throws otserver4j.exception.LoginException;
}
