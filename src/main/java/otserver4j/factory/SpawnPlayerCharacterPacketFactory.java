package otserver4j.factory;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import otserver4j.entity.AccountEntity;
import otserver4j.entity.PlayerCharacterEntity;
import otserver4j.exception.AccountException;
import otserver4j.packet.PacketType;
import otserver4j.packet.RawPacket;
import otserver4j.service.AbstractPacketFactory;
import otserver4j.service.GameMap;
import otserver4j.service.LoginService;
import otserver4j.structure.Direction;
import otserver4j.structure.Light;
import otserver4j.structure.MessageType;
import otserver4j.structure.PlayerCharacterCondition;
import otserver4j.structure.PlayerCharacterParty;
import otserver4j.structure.PlayerCharacterSlot;
import otserver4j.structure.Position;
import otserver4j.utils.ExperienceUtils;
import otserver4j.utils.LightUtils;

@RequiredArgsConstructor @Slf4j @Component
public class SpawnPlayerCharacterPacketFactory  extends AbstractPacketFactory<
    otserver4j.factory.SpawnPlayerCharacterPacketFactory.SpawnPlayerCharacterRequest, 
    otserver4j.factory.SpawnPlayerCharacterPacketFactory.SpawnPlayerCharacterResponse> {

  public static final Long PLAYER_IDENTIFIER_PREFIX = 0x0fffffffL;

  static final Integer
     PROCESSING_LOGIN_CODE_OK = 0x0a
    ,PROCESSING_LOGIN_CODE_NOK = 0x14
    ,CLIENT_RENDER_CODE = 0x32
    ,ERROR_REPORT_FLAG = 0x00
    ,CODE_MAP_INFO = 0x64
    ,CODE_SPAWN_FX = 0x83
    ,CODE_ICONS = 0xa2
    ,CODE_SEND_MESSAGE = 0xb4
    ,CODE_CHARACTER_LIGHT = 0x8d
    ,CODE_WORLD_LIGHT = 0x82
    ,CODE_SKILLS = 0xa1
    ,CODE_ATTRIBUTES = 0xa0
    ,CODE_INVENTORY_SLOT_FILLED = 0x78
    ,CODE_INVENTORY_SLOT_EMPTY = 0x79
    ;

  private final LoginService loginService;

  @Override public PacketType getPacketType() { return PacketType.LOGIN_SUCCESS; }
  @Override public Boolean thenDisconnect() { return Boolean.FALSE; }
  @Override public Set<String> sessionsToSendFrom(String session) {
    return Collections.singleton(session); }

  @Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true)
  public static class SpawnPlayerCharacterRequest
      extends otserver4j.service.AbstractPacketFactory.PacketRequest {
    private Integer clientVersion;
    private Integer accountNumber;
    private String selectedCharacterName;
    private String password;
  }

  @Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true)
  public static class SpawnPlayerCharacterResponse
      extends otserver4j.service.AbstractPacketFactory.PacketResponse {
    private String errorMessage;
    private PlayerCharacterEntity playerCharacter;
  }

  @Override public SpawnPlayerCharacterRequest newPacketRequest(
      ByteBuffer byteBuffer, Integer packetSize) {
    RawPacket.skip(byteBuffer, TWO.intValue());
    final Integer version = RawPacket.readInt16(byteBuffer);
    RawPacket.skip(byteBuffer, ONE.intValue());
    return new SpawnPlayerCharacterRequest().setClientVersion(version)
      .setAccountNumber(RawPacket.readInt32(byteBuffer))
      .setSelectedCharacterName(RawPacket.readString(byteBuffer))
      .setPassword(RawPacket.readString(byteBuffer));
  }

  @Value("${otserver.version}") private Integer version;

  @Override public SpawnPlayerCharacterResponse generatePacketResponse(
      SpawnPlayerCharacterRequest request) {
    try {
      if(!this.version.equals(request.getClientVersion()))
        throw AccountException.WRONG_VERSION_NUMBER_EXCEPTION;
      final AccountEntity account = this.loginService.findAccountToLogin(
        request.getAccountNumber(), request.getPassword());
      final Optional<PlayerCharacterEntity> selectedPlayerCharacterOpt = account.getCharacterList()
        .stream().filter(pc -> pc.getName().equals(request.getSelectedCharacterName())).findAny();
      if(selectedPlayerCharacterOpt.isEmpty())
        throw AccountException.INVALID_SELECTED_CHARACTER_EXCEPTION;
      return new SpawnPlayerCharacterResponse()
        .setPlayerCharacter(selectedPlayerCharacterOpt.get());
    }
    catch(AccountException accexc) {
      return new SpawnPlayerCharacterResponse().setErrorMessage(accexc.getMessage());
    }
    catch(Exception exc) {
      log.error("Failed to login: {}", exc.getMessage(), exc);
      return new SpawnPlayerCharacterResponse().setErrorMessage(
        String.format("Unexpected error: %s", exc.getMessage()));
    }
  }

  private RawPacket writePosition(final Position position, RawPacket packet) {
    return packet.writeInt16(position.getX()).writeInt16(position.getY()).writeByte(position.getZ());
  }

  private RawPacket writeWorldLight(Position position, RawPacket packet) {
    final Light light = LightUtils.INSTANCE.fromWorld(null, position);
    return packet.writeByte(CODE_WORLD_LIGHT)
      .writeByte(light.getRadius()).writeByte(light.getColor());
  }

  private RawPacket writePlayerLight(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    final Light light = LightUtils.INSTANCE.fromPlayer(playerCharacter);
    return packet.writeByte(CODE_CHARACTER_LIGHT).writeInt32(playerCharacter.getIdentifier())
      .writeByte(light.getRadius()).writeByte(light.getColor()); }

  private RawPacket writeLoginMessages(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    Arrays.stream(new String[] {
      String.format("Welcome, %s.", playerCharacter.getName()),
    }).forEach(msg -> packet.writeByte(CODE_SEND_MESSAGE)
      .writeByte(MessageType.STATUS.getCode()).writeString(msg));
    return packet;
  }

  private RawPacket writeIcons(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    return packet.writeByte(CODE_ICONS).writeByte(
      PlayerCharacterCondition.getIconCodeFromStatuses(playerCharacter.getConditions())); }

  private RawPacket writeSpawnEffect(
      PlayerCharacterEntity playerCharacter, RawPacket packet) {
    return this.writePosition(playerCharacter.getPosition(),
      packet.writeByte(CODE_SPAWN_FX)).writeByte(0x0a);
  }

  private RawPacket writeSkills(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    return packet.writeByte(CODE_SKILLS)
      .writeByte(playerCharacter.getFistSkill().getLevel()).writeByte(playerCharacter.getFistSkill().getPercent())
      .writeByte(playerCharacter.getClubSkill().getLevel()).writeByte(playerCharacter.getClubSkill().getPercent())
      .writeByte(playerCharacter.getSwordSkill().getLevel()).writeByte(playerCharacter.getSwordSkill().getPercent())
      .writeByte(playerCharacter.getAxeSkill().getLevel()).writeByte(playerCharacter.getAxeSkill().getPercent())
      .writeByte(playerCharacter.getDistanceSkill().getLevel()).writeByte(playerCharacter.getDistanceSkill().getPercent())
      .writeByte(playerCharacter.getShieldSkill().getLevel()).writeByte(playerCharacter.getShieldSkill().getPercent())
      .writeByte(playerCharacter.getFishingSkill().getLevel()).writeByte(playerCharacter.getFishingSkill().getPercent());
  }

  private RawPacket writeInventorySlot(PlayerCharacterSlot slot, RawPacket packet) {
    return packet.writeByte(CODE_INVENTORY_SLOT_EMPTY).writeByte(slot.getCode());
  }

  private RawPacket writeInventory(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    Arrays.asList(PlayerCharacterSlot.values()).stream()
      .filter(pcs -> !PlayerCharacterSlot.INVALID.equals(pcs))
      .forEach(pcs -> this.writeInventorySlot(pcs, packet));
    return packet;
  }

  private RawPacket writeAttributes(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    return packet.writeByte(CODE_ATTRIBUTES)
      .writeInt16(playerCharacter.getCurrentLife()).writeInt16(playerCharacter.getMaxLife())
      .writeInt16(/*????*/playerCharacter.getMaxCapacity())
      .writeInt32(playerCharacter.getExperience())
      .writeInt16(ExperienceUtils.INSTANCE.calculateLevelFromExperience(playerCharacter.getExperience()))
      .writeByte(ExperienceUtils.INSTANCE.calculateNextLevelPercentFromExperience(playerCharacter.getExperience()))
      .writeInt16(playerCharacter.getCurrentMana()).writeInt16(playerCharacter.getMaxMana())
      .writeByte(playerCharacter.getMagicSkill().getLevel()).writeByte(playerCharacter.getMagicSkill().getPercent())
      .writeByte(playerCharacter.getCurrentSoul());
  }

  private final GameMap gameMap;

  private RawPacket writeSpawnMapInfo(
      final PlayerCharacterEntity playerCharacter, final RawPacket packet) {
    final Position bounds = new Position().setZ(playerCharacter.getPosition().getZ())
      .setX(playerCharacter.getPosition().getX() + 9).setY(playerCharacter.getPosition().getY() + 7);
    final Integer z = playerCharacter.getPosition().getZ();
    for(Integer x = playerCharacter.getPosition().getX() - 8; x <= bounds.getX(); x++)
      for(Integer y = playerCharacter.getPosition().getY() - 6; y <= bounds.getY(); y++) {
        this.gameMap.writeTileFromPosition(x, y, z, packet);
        if(playerCharacter.getPosition().getX().equals(x) && playerCharacter.getPosition().getY().equals(y)) {
          packet.writeInt16(0x61); // Criatura desconhecida
          packet.writeInt32(0x00L); // Cache de criatura?
          packet.writeInt32(SpawnPlayerCharacterPacketFactory.PLAYER_IDENTIFIER_PREFIX + playerCharacter.getIdentifier());
          packet.writeString(playerCharacter.getName());
          packet.writeByte(playerCharacter.getCurrentLife() * 100 / playerCharacter.getMaxLife());
          packet.writeByte((playerCharacter.getDirection() != null && playerCharacter.getDirection().getSpawnable() ?
              playerCharacter.getDirection() : Direction.SOUTH).getCode());
          
          packet.writeByte(playerCharacter.getOutfit().getType());
          packet.writeByte(playerCharacter.getOutfit().getHead());
          packet.writeByte(playerCharacter.getOutfit().getBody());
          packet.writeByte(playerCharacter.getOutfit().getLegs());
          packet.writeByte(playerCharacter.getOutfit().getFeet());
          packet.writeByte(playerCharacter.getOutfit().getExtra());
          packet.writeInt16(0xff88); //playerCharacter.getSpeed()
          packet.writeByte(0x00); // +Velocidade
          packet.writeByte(playerCharacter.getSkull().getCode());
          packet.writeByte(PlayerCharacterParty.NONE.getCode());
          packet.writeByte(0x00);
        }
        else if(bounds.getX().equals(x) && bounds.getY().equals(y)) {
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
          packet.writeByte(0xff);
        }
        else packet.writeByte(0);
        packet.writeByte(0xff);
      }
    return packet;
  }

  private RawPacket writePlayerMapInfo(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    return this.writeSpawnMapInfo(playerCharacter,
      this.writePosition(playerCharacter.getPosition(), packet.writeByte(CODE_MAP_INFO)));
  }

  @Override public RawPacket generateRawPacketResponse(
      SpawnPlayerCharacterResponse response) {
    if(response.getErrorMessage() != null && !response.getErrorMessage().isBlank())
      return new RawPacket().writeByte(PROCESSING_LOGIN_CODE_NOK)
        .writeString(response.getErrorMessage());
    return
      this.writeIcons(response.getPlayerCharacter(),
      this.writeLoginMessages(response.getPlayerCharacter(),
      this.writePlayerLight(response.getPlayerCharacter(),
      this.writeWorldLight(response.getPlayerCharacter().getPosition(),
      this.writeSpawnEffect(response.getPlayerCharacter(),
      this.writeSkills(response.getPlayerCharacter(),
      this.writeAttributes(response.getPlayerCharacter(),
      this.writeInventory(response.getPlayerCharacter(),
      this.writePlayerMapInfo(response.getPlayerCharacter(), new RawPacket()
        .writeByte(PROCESSING_LOGIN_CODE_OK)
        .writeInt32(PLAYER_IDENTIFIER_PREFIX + response.getPlayerCharacter().getIdentifier())
        .writeInt16(CLIENT_RENDER_CODE).writeByte(ERROR_REPORT_FLAG))))))))))
      ;
  }

}
