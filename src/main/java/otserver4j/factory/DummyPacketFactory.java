package otserver4j.factory;

import java.util.Collections;
import java.util.Set;

import otserver4j.packet.AbstractPacketFactory;
import otserver4j.structure.PacketType;
import otserver4j.structure.RawPacket;

@org.springframework.stereotype.Component
public class DummyPacketFactory extends AbstractPacketFactory<
    otserver4j.factory.DummyPacketFactory.DummyPacketRequest,
    otserver4j.factory.DummyPacketFactory.DummyPacketResponse> {

  @Override public PacketType getPacketType() { return PacketType.INVALID; }
  @Override public boolean thenDisconnect() { return false; }

  public static class DummyPacketRequest
    extends otserver4j.packet.AbstractPacketFactory.PacketRequest { }

  public static class DummyPacketResponse
    extends otserver4j.packet.AbstractPacketFactory.PacketResponse { }

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
