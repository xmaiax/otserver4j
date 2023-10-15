package otserver4j.service;

import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.Set;

import lombok.Getter;
import otserver4j.structure.PacketType;
import otserver4j.structure.RawPacket;

@lombok.extern.slf4j.Slf4j
@SuppressWarnings({ "unchecked", })
public abstract class AbstractPacketFactory<
    Request extends otserver4j.service.AbstractPacketFactory.PacketRequest,
    Response extends otserver4j.service.AbstractPacketFactory.PacketResponse> {

  public abstract PacketType getPacketType();
  public abstract boolean thenDisconnect();

  public static abstract class PacketRequest {
    @Getter private String session;
    @Getter private PacketType packetType;
  }
  public abstract Request newPacketRequest(java.nio.ByteBuffer byteBuffer, Integer packetSize);
  public final void addSessionAndType(PacketRequest request, String session, PacketType packetType) {
    request.session = session;
    request.packetType = packetType;
  }

  public static abstract class PacketResponse {
    @Getter private String session;
    @Getter private PacketType packetType;
  }
  public abstract Response generatePacketResponse(Request request);
  public final Response convertObjectRequestToCustomPacketResponse(PacketRequest packetRequest) {
    final PacketResponse response = (PacketResponse) this.generatePacketResponse((Request) packetRequest);
    response.session = packetRequest.getSession();
    response.packetType = packetRequest.getPacketType();
    return (Response) response;
  }

  public abstract RawPacket generateRawPacketResponse(Response response);
  public final RawPacket convertPacketResponseToCustomPacketResponse(
    PacketResponse packetResponse) { return this
      .generateRawPacketResponse((Response) packetResponse); }
  public abstract Set<String> sessionsToSendFrom(String session);

  @Getter private final Class<Request> requestClass;
  @Getter private final Class<Response> responseClass;
  protected AbstractPacketFactory() {
    final ParameterizedType genericSuperclass =
      (ParameterizedType) getClass().getGenericSuperclass();
    this.requestClass = (Class<Request>) genericSuperclass.getActualTypeArguments(
      )[BigInteger.ZERO.intValue()];
    this.responseClass = (Class<Response>) genericSuperclass.getActualTypeArguments(
      )[BigInteger.ONE.intValue()];
    log.info("PacketFactory for type '{}' initialized (Request: {}, Response: {}).",
      this.getPacketType(), this.requestClass.getName(), this.responseClass.getName());
  }

}
