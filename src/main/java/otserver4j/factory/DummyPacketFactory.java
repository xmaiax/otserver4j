package otserver4j.factory;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;

import otserver4j.packet.PacketType;
import otserver4j.packet.RawPacket;
import otserver4j.service.AbstractPacketFactory;

@Component public class DummyPacketFactory extends AbstractPacketFactory<
    otserver4j.factory.DummyPacketFactory.DummyPacketRequest,
    otserver4j.factory.DummyPacketFactory.DummyPacketResponse> {

  @Override public PacketType getPacketType() { return PacketType.INVALID; }
  @Override public Boolean thenDisconnect() { return Boolean.FALSE; }

  public static class DummyPacketRequest
    extends otserver4j.service.AbstractPacketFactory.PacketRequest { }

  public static class DummyPacketResponse
    extends otserver4j.service.AbstractPacketFactory.PacketResponse { }

  @Override
  public DummyPacketRequest newPacketRequest(java.nio.ByteBuffer byteBuffer, Integer packetSize) {
    return new DummyPacketRequest();
  }

  @Override
  public DummyPacketResponse generatePacketResponse(DummyPacketRequest request) {
    return new DummyPacketResponse();
  }

  @Override
  public RawPacket generateRawPacketResponse(DummyPacketResponse response) {
    return new RawPacket();
  }

  @Override
  public Set<String> sessionsToSendFrom(String session) {
    return Collections.emptySet();
  }

}
