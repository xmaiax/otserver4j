package otserver4j.factory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import otserver4j.packet.PacketType;
import otserver4j.packet.RawPacket;
import otserver4j.service.AbstractPacketFactory;
import otserver4j.structure.ChatType;

@Component public class TalkPacketFactory extends AbstractPacketFactory<
    otserver4j.factory.TalkPacketFactory.TalkPacketRequest,
    otserver4j.factory.TalkPacketFactory.TalkPacketResponse> {

  public static final Integer TALK_SERVER_CODE = 0xaa;

  @Override public PacketType getPacketType() { return PacketType.TALK; }
  @Override public Boolean thenDisconnect() { return Boolean.FALSE; }

  @Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true)
  public static class TalkPacketRequest extends otserver4j.service.AbstractPacketFactory.PacketRequest {
    private final ChatType chatType;
    private final String message;
  }

  @Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true)
  public static class TalkPacketResponse extends otserver4j.service.AbstractPacketFactory.PacketResponse {
    //
  }

  @Override
  public TalkPacketRequest newPacketRequest(ByteBuffer byteBuffer, Integer packetSize) {
    return new TalkPacketRequest(
      ChatType.fromCode(RawPacket.readByte(byteBuffer)),
      RawPacket.readString(byteBuffer));
  }

  @Override
  public TalkPacketResponse generatePacketResponse(TalkPacketRequest request) {
    return new TalkPacketResponse();
  }

  @Override
  public RawPacket generateRawPacketResponse(TalkPacketResponse response) {
    return new RawPacket();
  }

  @Override public Set<String> sessionsToSendFrom(String session) {
    return Collections.singleton(session); }

}
