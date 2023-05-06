package otserver4j.protocol.impl;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import otserver4j.exception.LoginException;
import otserver4j.packet.Packet;
import otserver4j.packet.PacketType;
import otserver4j.protocol.Protocol;
import otserver4j.service.AccountService;
import otserver4j.service.PlayerCharacterService;
import otserver4j.structure.Account;
import otserver4j.structure.Chat.MessageType;
import otserver4j.structure.FX;
import otserver4j.structure.GameMap;
import otserver4j.structure.Item.ItemWithQuantity;
import otserver4j.structure.Light;
import otserver4j.structure.PlayerCharacter;
import otserver4j.structure.PlayerCharacter.Attribute;
import otserver4j.structure.PlayerCharacter.Skill;
import otserver4j.structure.PlayerCharacter.Slot;
import otserver4j.structure.Position;
import otserver4j.structure.Status.Condition;
import otserver4j.utils.DateFormatUtils;
import otserver4j.utils.ExperienceUtils;
import otserver4j.utils.LightUtils;

@Component @Slf4j
public class SpawnProtocol implements Protocol {

  public static final Long PLAYER_IDENTIFIER_PREFIX = 0x0fffffffL;
  public static final FX SPAWN_EFFECT = FX.SPAWN;

  @Autowired private AccountService accService;
  @Autowired private PlayerCharacterService pcService;
  @Autowired private GameMap gameMap;

  @Value("${otserver.version}") private Integer version;

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

  private Packet writeInventory(Map<Slot, ItemWithQuantity> inventory, Packet packet) {
    Arrays.stream(Slot.values()).filter(s -> !Slot.INVALID.equals(s)).forEach(slot -> {
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

  private Packet writeStats(Attribute life, Attribute mana, Attribute capacity,
      Long experience, Skill magicSkill, Attribute soul, Packet packet) {
    return packet.writeByte(Packet.CODE_STATS)
      .writeInt16(life.getValue()).writeInt16(life.getMaxValue()).writeInt16(capacity.getValue())
      .writeInt32(experience).writeInt16(ExperienceUtils.getInstance().levelFromExp(experience))
      .writeByte(ExperienceUtils.getInstance().nextLevelPercent(experience))
      .writeInt16(mana.getValue()).writeInt16(mana.getMaxValue())
      .writeByte(magicSkill.getLevel()).writeByte(magicSkill.getPercent())
      .writeByte(soul.getValue());
  }

  private Packet writeSkills(Skill fist, Skill club, Skill sword, Skill axe,
      Skill distance, Skill shield, Skill fishing, Packet packet) {
    packet.writeByte(Packet.CODE_SKILLS); Arrays.stream(new Skill[] {
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
      String.format("Last login: %s", DateFormatUtils.getInstance()
        .formatFullDate(player.getLastLogin())),
    }).forEach(msg -> packet.writeByte(Packet.CODE_SEND_MESSAGE)
      .writeByte(MessageType.STATUS.getCode()).writeString(msg));
    return packet;
  }

  private Packet writeIcons(PlayerCharacter player, Packet packet) {
    return packet.writeByte(Packet.CODE_ICONS).writeByte(
      Condition.getIconCodeFromStatuses(player.getConditions()));
  }

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key,
      SocketChannel channel, PacketType type) throws LoginException {
    Packet.skip(buffer, TWO.intValue());
    final Integer version = Packet.readInt16(buffer);
    if(!this.version.equals(version))
      throw new LoginException(LoginException.CommonError.WRONG_VERSION_NUMBER);
    Packet.skip(buffer, ONE.intValue());
    final Integer accountNumber = Packet.readInt32(buffer);
    final String selectedCharacterName = Packet.readString(buffer);
    if(selectedCharacterName == null || selectedCharacterName.isBlank())
      throw new LoginException(LoginException.CommonError.INVALID_SELECTED_CHARACTER);
    final String password = Packet.readString(buffer);
    final Account account = this.accService.findAccount(accountNumber, password);
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