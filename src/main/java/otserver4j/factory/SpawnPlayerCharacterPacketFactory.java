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
import otserver4j.service.AbstractPacketFactory;
import otserver4j.service.LoginService;
import otserver4j.structure.Light;
import otserver4j.structure.MessageType;
import otserver4j.structure.PacketType;
import otserver4j.structure.PlayerCharacterCondition;
import otserver4j.structure.Position;
import otserver4j.structure.RawPacket;
import otserver4j.utils.LightUtils;

@SuppressWarnings("unused")

@RequiredArgsConstructor @Slf4j @Component
public class SpawnPlayerCharacterPacketFactory  extends AbstractPacketFactory<
    otserver4j.factory.SpawnPlayerCharacterPacketFactory.SpawnPlayerCharacterRequest, 
    otserver4j.factory.SpawnPlayerCharacterPacketFactory.SpawnPlayerCharacterResponse> {

  static final Long PLAYER_IDENTIFIER_PREFIX = 0x0fffffffL;

  static final Integer
     PROCESSING_LOGIN_CODE_OK = 0x0a
    ,PROCESSING_LOGIN_CODE_NOK = 0x14
    ,CLIENT_RENDER_CODE = 0x32
    ,ERROR_REPORT_FLAG = 0x00
    ,CODE_ICONS = 0xa2
    ,CODE_SEND_MESSAGE = 0xb4
    ,CODE_CHARACTER_LIGHT = 0x8d
    ,CODE_WORLD_LIGHT = 0x82
    ,CODE_SKILLS = 0xa1
    ;

  private final LoginService loginService;

  @Override public PacketType getPacketType() { return PacketType.LOGIN_SUCCESS; }
  @Override public boolean thenDisconnect() { return Boolean.FALSE; }
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
      //String.format("Last login: %s", "today?"),
    }).forEach(msg -> packet.writeByte(CODE_SEND_MESSAGE)
      .writeByte(MessageType.STATUS.getCode()).writeString(msg));
    return packet;
  }

  private RawPacket writeIcons(PlayerCharacterEntity playerCharacter, RawPacket packet) {
    return packet.writeByte(CODE_ICONS).writeByte(
      PlayerCharacterCondition.getIconCodeFromStatuses(playerCharacter.getConditions())); }

  @Override public RawPacket generateRawPacketResponse(
      SpawnPlayerCharacterResponse response) {
    if(response.getErrorMessage() != null && !response.getErrorMessage().isBlank())
      return new RawPacket().writeByte(PROCESSING_LOGIN_CODE_NOK)
        .writeString(response.getErrorMessage());
    return new RawPacket().writeByte(PROCESSING_LOGIN_CODE_OK)
      .writeInt32(PLAYER_IDENTIFIER_PREFIX + response.getPlayerCharacter().getIdentifier())
      .writeInt16(CLIENT_RENDER_CODE).writeByte(ERROR_REPORT_FLAG);
  }

}
