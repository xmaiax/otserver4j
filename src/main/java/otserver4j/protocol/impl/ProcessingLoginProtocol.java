package otserver4j.protocol.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import otserver4j.exception.LoginException;
import otserver4j.packet.Packet;
import otserver4j.protocol.Protocol;
import otserver4j.structure.FX;
import otserver4j.structure.GameMap;
import otserver4j.structure.Item;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.Light;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.PlayerCharacter.Attribute;
import otserver4j.structure.PlayerCharacter.Skill;
import otserver4j.structure.PlayerCharacter.Slot;
import otserver4j.structure.Position;
import otserver4j.structure.Chat.MessageType;
import otserver4j.utils.ExperienceUtils;
import otserver4j.utils.LightUtils;

@Component @Slf4j
public class ProcessingLoginProtocol implements Protocol {

  public static final FX SPAWN_EFFECT = FX.SPAWN;
  public static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

  @Autowired private GameMap gameMap;

  @Value("${otserver.version}") private Integer version;

  private Packet writePosition(Position position, Packet packet) {
    return packet.writeInt16(position.getX())
      .writeInt16(position.getY()).writeByte(position.getZ());
  }

  //TODO: Implementar essa dor na bunda aqui
  private Packet writeMapInfo(Long indetifier, Position position, Packet packet) {
    return this.writePosition(position, packet);
  }

  private Packet writeSpawnEffect(Position position, Packet packet) {
    return this.writePosition(position, packet.writeByte(Packet.CODE_SPAWN_FX))
      .writeByte(SPAWN_EFFECT.getCode());
  }

  private Packet writeInventory(Map<Slot, ItemWithQuantity> inventory, Packet packet) {
    Arrays.stream(Slot.values()).filter(s -> !Slot.INVALID.equals(s) ||
        !Slot.LAST.equals(s)).forEach(slot -> {
      final boolean haveItem = inventory.containsKey(slot);
      packet.writeByte(haveItem ?
        Packet.CODE_INVENTORY_SLOT_FILLED : Packet.CODE_INVENTORY_SLOT_EMPTY);
      packet.writeByte(slot.getCode());
      if(haveItem) {
        final ItemWithQuantity itemWithQt = inventory.get(slot);
        packet.writeInt16(itemWithQt.getItem().getCode());
        if(itemWithQt.getItem().getStackable())
          packet.writeByte(itemWithQt.getQuantity());
      }
    }); return packet;
  }

  private Packet writeStats(Attribute life, Attribute mana, Attribute capacity,
      Long experience, Skill magicSkill, Packet packet) {
    return packet.writeByte(Packet.CODE_STATS)
      .writeInt16(life.getValue()).writeInt16(life.getMaxValue())
      .writeInt16(capacity.getValue()).writeInt32(experience)
      .writeByte(ExperienceUtils.getInstance().levelFromExp(experience))
      .writeByte(ExperienceUtils.getInstance().nextLevelPercent(experience))
      .writeInt16(mana.getValue()).writeInt16(mana.getMaxValue())
      .writeByte(magicSkill.getLevel()).writeByte(magicSkill.getPercent());
  }

  private Packet writeSkills(Skill fist, Skill club, Skill sword, Skill axe,
      Skill distance, Skill shield, Skill fishing, Packet packet) {
    packet.writeByte(Packet.CODE_SKILLS); Arrays.stream(new Skill[] {
      fist, club, sword, axe, distance, shield, fishing, }).forEach(skill -> packet
        .writeByte(skill.getLevel()).writeByte(skill.getPercent())); return packet;
  }

  private Packet writeWorldLight(Packet packet) {
    final Light light = LightUtils.getInstance().fromWorld(this.gameMap);
    return packet.writeByte(Packet.CODE_WORLD_LIGHT)
      .writeByte(light.getRadius()).writeByte(light.getColor());
  }

  private Packet writePlayerLight(PlayerCharacter player, Packet packet) {
    final Light light = LightUtils.getInstance().fromPlayer(player);
    return packet.writeByte(Packet.CODE_CHARACTER_LIGHT).writeInt32(player.getIdentifier())
      .writeByte(light.getRadius()).writeByte(light.getColor());
  }

  private Packet writeLoginMessages(PlayerCharacter player, Packet packet) {
    Arrays.stream(new String[] {
      String.format("Welcome, %s.", player.getName()),
      String.format("Last login: %s", SDF.format(player.getLastLogin().getTime())),
    }).forEach(msg -> {
      packet.writeByte(Packet.CODE_SUCCESSFUL_LOGIN_MESSAGE)
        .writeByte(MessageType.STATUS.getCode()).writeString(msg);
    });
    return packet;
  }

  private Packet writeIcons(PlayerCharacter player, Packet packet) {
    return packet.writeByte(Packet.CODE_ICONS).writeByte(player.getIcons());
  }

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key) throws LoginException {
    Packet.skip(buffer, 2);
    if(!this.version.equals(Packet.readInt16(buffer)))
      throw new LoginException("Wrong version number.");
    Packet.skip(buffer, 1);
    final Integer accountNumber = Packet.readInt32(buffer);
    final String selectedCharacterName = Packet.readString(buffer);
    final String password = Packet.readString(buffer);
    if(password == null || password.isBlank())
      throw new LoginException("Nice try, a**hole!");
    log.info("Successful login attemp from account number '{}': {}",
      accountNumber, selectedCharacterName);

    final PlayerCharacter player = new PlayerCharacter()
       .setName(selectedCharacterName)
       .setIdentifier((long) accountNumber)
       .setPosition(new Position().setX(1).setY(2).setZ((byte) 0x03))
       .setInventory(Collections.singletonMap(Slot.BACKPACK,
         new ItemWithQuantity().setItem(Item.BACKPACK)));

    key.attach(player);

    return this.writeIcons(player,
           this.writePlayerLight(player,
           this.writeLoginMessages(player,
           this.writePlayerLight(player,
           this.writeWorldLight(
           this.writeSkills(player.getFistSkill(), player.getClubSkill(),
             player.getSwordSkill(), player.getAxeSkill(), player.getDistanceSkill(),
             player.getShieldSkill(), player.getFishingSkill(),
           this.writeStats(player.getLife(), player.getMana(), player.getCapacity(),
             player.getExperience(), player.getMagicSkill(),
           this.writeInventory(player.getInventory(),
           this.writeSpawnEffect(player.getPosition(), 
           this.writeMapInfo(player.getIdentifier(), player.getPosition(),
      new Packet().writeByte(Packet.PROCESSING_LOGIN_CODE_OK).writeInt32(player.getIdentifier())
        .writeInt16(Packet.CLIENT_RENDER_CODE).writeByte(Packet.ERROR_REPORT_FLAG)))))))))));
  }

}
