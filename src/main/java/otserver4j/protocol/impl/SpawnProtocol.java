package otserver4j.protocol.impl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import otserver4j.exception.LoginException;
import otserver4j.packet.Packet;
import otserver4j.structure.FX;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.Light;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.Position;
import otserver4j.utils.ExperienceUtils;
import otserver4j.utils.LightUtils;

@lombok.extern.slf4j.Slf4j @org.springframework.stereotype.Component
public class SpawnProtocol implements otserver4j.protocol.Protocol {

  public static final Long PLAYER_IDENTIFIER_PREFIX = 0x0fffffffL;
  public static final FX SPAWN_EFFECT = FX.SPAWN;

  @Autowired private otserver4j.service.AccountService accService;
  @Autowired private otserver4j.service.PlayerCharacterService pcService;
  @Autowired private otserver4j.structure.GameMap gameMap;

  @org.springframework.beans.factory.annotation.Value("${otserver.version}")
  private Integer version;

  private Packet writePosition(Position position, Packet packet) {
    return packet.writeInt16(position.getX())
      .writeInt16(position.getY()).writeByte(position.getZ());
  }

  private Packet writePlayerMapInfo(PlayerCharacter player, Packet packet) {
    return this.gameMap.writeMapInfo(player, this.writePosition(player.getPosition(),
      packet.writeByte(Packet.CODE_MAP_INFO)));
  }

  private Packet writeSpawnEffect(Position position, Packet packet) {
    packet.writeByte(Packet.CODE_SPAWN_FX);
    writePosition(position, packet);
    return packet.writeByte(SPAWN_EFFECT.getCode());
  }

  private Packet writeInventory(java.util.Map<PlayerCharacter.Slot, ItemWithQuantity> inventory,
      Packet packet) {
    Arrays.stream(PlayerCharacter.Slot.values()).filter(s -> !PlayerCharacter.Slot.INVALID.equals(s))
        .forEach(slot -> {
      final boolean haveItem = inventory.containsKey(slot);
      packet.writeByte(haveItem ?
        Packet.CODE_INVENTORY_SLOT_FILLED : Packet.CODE_INVENTORY_SLOT_EMPTY);
      packet.writeByte(slot.getCode());
      if(haveItem) {
        final ItemWithQuantity itemWithQt = inventory.get(slot);
        packet.writeInt16(itemWithQt.getItem().getCode());
        if(itemWithQt.getItem().isStackable())
          packet.writeByte(itemWithQt.getQuantity());
      }
    });
    return packet;
  }

  private Packet writeStats(PlayerCharacter.Attribute life, PlayerCharacter.Attribute mana,
      PlayerCharacter.Attribute capacity, Long experience, PlayerCharacter.Skill magicSkill,
      PlayerCharacter.Attribute soul, Packet packet) {
    return packet.writeByte(Packet.CODE_STATS)
      .writeInt16(life.getValue()).writeInt16(life.getMaxValue()).writeInt16(capacity.getValue())
      .writeInt32(experience).writeInt16(ExperienceUtils.getInstance().levelFromExp(experience))
      .writeByte(ExperienceUtils.getInstance().nextLevelPercent(experience))
      .writeInt16(mana.getValue()).writeInt16(mana.getMaxValue())
      .writeByte(magicSkill.getLevel()).writeByte(magicSkill.getPercent())
      .writeByte(soul.getValue());
  }

  private Packet writeSkills(PlayerCharacter.Skill fist, PlayerCharacter.Skill club,
      PlayerCharacter.Skill sword, PlayerCharacter.Skill axe, PlayerCharacter.Skill distance,
      PlayerCharacter.Skill shield, PlayerCharacter.Skill fishing, Packet packet) {
    packet.writeByte(Packet.CODE_SKILLS); Arrays.stream(new PlayerCharacter.Skill[] {
      fist, club, sword, axe, distance, shield, fishing, }).forEach(skill -> packet
        .writeByte(skill.getLevel()).writeByte(skill.getPercent())); return packet;
  }

  private Packet writeWorldLight(Position position, Packet packet) {
    final Light light = LightUtils.getInstance().fromWorld(this.gameMap, position);
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
      String.format("Last login: %s", otserver4j.utils.DateFormatUtils.getInstance()
        .formatFullDate(player.getLastLogin())),
    }).forEach(msg -> packet.writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(otserver4j.structure.Chat.MessageType.STATUS.getCode()).writeString(msg));
    return packet;
  }

  private Packet writeIcons(PlayerCharacter player, Packet packet) {
    return packet.writeByte(Packet.CODE_ICONS).writeByte(
      otserver4j.structure.Status.Condition.getIconCodeFromStatuses(player.getConditions()));
  }

  @Override
  public Packet execute(java.nio.ByteBuffer buffer, java.nio.channels.SelectionKey key,
      java.nio.channels.SocketChannel channel, PlayerCharacter _null,
      otserver4j.packet.PacketType type) throws LoginException {
    Packet.skip(buffer, java.math.BigInteger.TWO.intValue());
    final Integer version = Packet.readInt16(buffer);
    if(!this.version.equals(version))
      throw new LoginException(LoginException.CommonError.WRONG_VERSION_NUMBER);
    Packet.skip(buffer, java.math.BigInteger.ONE.intValue());
    final Integer accountNumber = Packet.readInt32(buffer);
    final String selectedCharacterName = Packet.readString(buffer);
    if(selectedCharacterName == null || selectedCharacterName.isBlank())
      throw new LoginException(LoginException.CommonError.INVALID_SELECTED_CHARACTER);
    final String password = Packet.readString(buffer);
    final otserver4j.structure.Account account = this.accService.findAccount(accountNumber, password);
    if(account.getCharacters().stream().noneMatch(c -> c.getName().equals(selectedCharacterName)))
      throw new LoginException(String.format("No character found with name "
        + "'%s' in the given account.", selectedCharacterName));
    final PlayerCharacter player = this.pcService.findPlayerCharacter(
      accountNumber, selectedCharacterName);
    log.info("Successful login attemp from account number '{}': {}",
      accountNumber, selectedCharacterName);
    key.attach(player);
    return this.writeIcons(player,
           this.writeLoginMessages(player,
           this.writePlayerLight(player,
           this.writeWorldLight(player.getPosition(),
           this.writeSpawnEffect(player.getPosition(),
           this.writeSkills(
             player.getFistSkill(), player.getClubSkill(),
             player.getSwordSkill(), player.getAxeSkill(),
             player.getDistanceSkill(), player.getShieldSkill(),
             player.getFishingSkill(),
           this.writeStats(
             player.getLife(), player.getMana(),
             player.getCapacity(), player.getExperience(),
             player.getMagicSkill(), player.getSoul(),
           this.writeInventory(player.getInventory(),
           this.writePlayerMapInfo(player, new Packet()
             .writeByte(Packet.PROCESSING_LOGIN_CODE_OK)
             .writeInt32(PLAYER_IDENTIFIER_PREFIX + player.getIdentifier())
             .writeInt16(Packet.CLIENT_RENDER_CODE)
             .writeByte(Packet.ERROR_REPORT_FLAG))))))))));
  }

}
