package otserver4j.service.impl;

import java.util.Calendar;
import java.util.Collections;

import org.springframework.stereotype.Service;

import otserver4j.exception.LoginException;
import otserver4j.service.PlayerCharacterService;
import otserver4j.structure.Direction;
import otserver4j.structure.Item;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.PlayerCharacter.Attribute;
import otserver4j.structure.PlayerCharacter.Outfit;
import otserver4j.structure.PlayerCharacter.Profession;
import otserver4j.structure.PlayerCharacter.Skill;
import otserver4j.structure.PlayerCharacter.Slot;
import otserver4j.structure.Position;

@Service
public class PlayerCharacterServiceImpl implements PlayerCharacterService {

  @Override
  public PlayerCharacter findPlayerCharacter(int accountNumber,
      String characterName) throws LoginException {
    final Calendar lastLogin = Calendar.getInstance();
    lastLogin.add(Calendar.DAY_OF_YEAR, -10);
    return new PlayerCharacter()
      .setIdentifier(0x04030201L)
      .setName(characterName)
      .setLevel(3)
      .setExperience(4567L)
      .setPercentNextLevel((byte) 66)
      .setProfession(Profession.NECROMANCER)
      .setLife(new Attribute().setValue(150).setMaxValue(200))
      .setMana(new Attribute().setValue(80).setMaxValue(100))
      .setCapacity(new Attribute().setValue(30).setMaxValue(180))
      .setPosition(new Position().setX(32000).setY(32000).setZ((byte) 0x07))
      .setDirection(Direction.SOUTH)
      .setInventory(Collections.singletonMap(Slot.BACKPACK,
        new ItemWithQuantity().setItem(Item.BACKPACK)))
      .setOutfit(new Outfit()
        .setHead((byte) 0x50)
        .setBody((byte) 0x50)
        .setLegs((byte) 0x50)
        .setFeet((byte) 0x50)
        .setExtra(null)
        .setType(0x80))
      .setMagicSkill(new Skill().setLevel((byte) 5).setPercent((byte) 33))
      .setFistSkill(new Skill().setLevel((byte) 10).setPercent((byte) 50))
      .setClubSkill(new Skill().setLevel((byte) 12).setPercent((byte) 60))
      .setSwordSkill(new Skill().setLevel((byte) 14).setPercent((byte) 70))
      .setAxeSkill(new Skill().setLevel((byte) 16).setPercent((byte) 80))
      .setDistanceSkill(new Skill().setLevel((byte) 18).setPercent((byte) 85))
      .setShieldSkill(new Skill().setLevel((byte) 20).setPercent((byte) 90))
      .setFishingSkill(new Skill().setLevel((byte) 22).setPercent((byte) 95))
      .setIcons(0)
      .setLastLogin(lastLogin);
  }

}
