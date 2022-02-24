package otserver4j.service.impl;

import java.util.Collections;

import org.springframework.stereotype.Service;

import otserver4j.exception.LoginException;
import otserver4j.service.PlayerCharacterService;
import otserver4j.structure.Item;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.Position;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.PlayerCharacter.Slot;

@Service
public class PlayerCharacterServiceImpl implements PlayerCharacterService {

  @Override
  public PlayerCharacter findPlayerCharacter(int accountNumber,
      String characterName) throws LoginException {
    return new PlayerCharacter()
      .setName(characterName)
      .setIdentifier((long) accountNumber)
      .setPosition(new Position().setX(1).setY(2).setZ((byte) 0x03))
      .setInventory(Collections.singletonMap(Slot.BACKPACK,
        new ItemWithQuantity().setItem(Item.BACKPACK)));
  }

}
