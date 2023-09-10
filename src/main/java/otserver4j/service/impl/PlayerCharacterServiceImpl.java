package otserver4j.service.impl;

import java.util.Calendar;

import otserver4j.entity.PlayerCharacter;
import otserver4j.entity.PlayerCharacter.Attribute;
import otserver4j.entity.PlayerCharacter.Outfit;
import otserver4j.entity.PlayerCharacter.Profession;
import otserver4j.entity.PlayerCharacter.Skill;
import otserver4j.entity.PlayerCharacter.Slot;
import otserver4j.exception.AccountException;
import otserver4j.structure.Item;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.Position;
import otserver4j.structure.Status.Condition;
import otserver4j.structure.Status.Skull;

@org.springframework.stereotype.Service public class PlayerCharacterServiceImpl
    implements otserver4j.service.PlayerCharacterService {

  @Override public PlayerCharacter findPlayerCharacter(Integer accountNumber,
      String characterName) throws AccountException {
    final Calendar lastLogin = Calendar.getInstance();
    lastLogin.add(Calendar.DAY_OF_YEAR, -10);
    return new PlayerCharacter()
      .setIdentifier(1L)
      .setName(characterName)
      .setLevel(7)
      .setExperience(4567L)
      .setPercentNextLevel(44)
      .setProfession(Profession.NECROMANCER)
      .setLife(new Attribute().setValue(150).setMaxValue(200))
      .setMana(new Attribute().setValue(80).setMaxValue(100))
      .setCapacity(new Attribute().setValue(30).setMaxValue(180))
      .setSoul(new Attribute().setValue(100).setMaxValue(100))
      .setPosition(new Position().setX(50).setY(50).setZ(7))
      .setDirection(otserver4j.structure.Direction.EAST)
      .setInventory(java.util.Map.of(
        Slot.BACKPACK, new ItemWithQuantity().setItem(Item.BACKPACK),
        Slot.CHEST, new ItemWithQuantity().setItem(Item.MAGIC_PLATE_ARMOR)
      )).setOutfit(new Outfit().setType(0x80)
        .setHead(0x04)
        .setBody(0x03)
        .setLegs(0x02)
        .setFeet(0x01)
        .setExtra(0x00))
      .setMagicSkill(new Skill().setLevel(5).setPercent(33))
      .setFistSkill(new Skill().setLevel(10).setPercent(50))
      .setClubSkill(new Skill().setLevel(12).setPercent(60))
      .setSwordSkill(new Skill().setLevel(14).setPercent(70))
      .setAxeSkill(new Skill().setLevel(16).setPercent(80))
      .setDistanceSkill(new Skill().setLevel(18).setPercent(85))
      .setShieldSkill(new Skill().setLevel(20).setPercent(90))
      .setFishingSkill(new Skill().setLevel(22).setPercent(95))
      .setSpeed(0xff88)
      .setConditions(java.util.Arrays.asList(new Condition[] {
        //...
      }))
      .setSkull(Skull.NONE)
      .setLastLogin(lastLogin);
  }

}
