package otserver4j.converter;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@lombok.Getter public class RawPacket {

  public static final Integer MAX_SIZE = 0xffff, LOGIN_CODE_NOK = 0x0a,
    PROCESSING_LOGIN_CODE_OK = 0x0a, PROCESSING_LOGIN_CODE_NOK = 0x14,
    CLIENT_RENDER_CODE = 0x32, ERROR_REPORT_FLAG = 0x00, CODE_MAP_INFO = 0x64,
    CODE_INVENTORY_SLOT_FILLED = 0x78, CODE_INVENTORY_SLOT_EMPTY = 0x79,
    CODE_STATS = 0xa0, CODE_SKILLS = 0xa1, CODE_WORLD_LIGHT = 0x82,
    CODE_SPAWN_FX = 0x83, CODE_CHARACTER_LIGHT = 0x8d, CODE_ICONS = 0xa2,
    CODE_SEND_MESSAGE = 0xb4, SNAPBACK_CODE = 0xb5;

  public static Integer readByte(ByteBuffer input) {
    return input.get() & 0xff;
  }
  public static Integer readInt16(ByteBuffer input) {
    return readByte(input) | (readByte(input) << 8); }
  public static Integer readInt32(ByteBuffer input) {
    return readByte(input)        | (readByte(input) <<  8)
        | (readByte(input) << 16) | (readByte(input) << 24);
  }

  public static String readString(ByteBuffer input) {
    return java.util.stream.IntStream.iterate(ZERO.intValue(), i -> readByte(input))
      .limit(readInt16(input) + ONE.intValue()).collect(java.io.ByteArrayOutputStream::new,
        (baos, i) -> baos.write((byte) i), (baos1, baos2) -> baos1.write(baos2.toByteArray(),
          ZERO.intValue(), baos2.size())).toString().substring(ONE.intValue());
  }

  public static void skip(ByteBuffer input, Integer n) { input.position(input.position() + n); }
  public static RawPacket createGenericErrorPacket(Integer code, String message) {
    return new RawPacket().writeByte(code).writeString(message); }

  private Integer size = ZERO.intValue();
  private byte[] buffer = new byte[MAX_SIZE - 2];

  public RawPacket writeByte(byte _byte) {
    this.buffer[this.size++] = _byte;
    return this;
  }

  public RawPacket writeByte(Integer _byte) { return this.writeByte(
    Integer.valueOf(_byte & 0xff).byteValue()); }
  public RawPacket writeByte(char _byte) { return this.writeByte((byte) _byte); }
  public RawPacket writeInt16(Integer _int) { return this.writeByte(_int & 0x00ff)
    .writeByte((_int & 0xff00) >> 8); }

  public RawPacket writeInt32(Long _long) {
    return this
      .writeByte( (int)(_long & 0x000000ff))
      .writeByte(((int)(_long & 0x0000ff00)) >>  8)
      .writeByte(((int)(_long & 0x00ff0000)) >> 16)
      .writeByte(((int)(_long & 0xff000000)) >> 24);
  }

  public RawPacket writeString(String _str) {
    this.writeInt16(_str.length());
    _str.chars().forEachOrdered(c -> this.writeByte(c));
    return this;
  }

  public byte[] bufferWithSize() {
    final byte[] output = new byte[this.size + 0x02];
    System.arraycopy(new byte[] { ((byte)(this.size & 0x00ff)),
      ((byte)((this.size & 0xff00) >> 8)), }, ZERO.intValue(), output, ZERO.intValue(), 0x02);
    System.arraycopy(this.buffer, ZERO.intValue(), output, 0x02, this.size);
    return output;
  }

  public static final RawPacket newSnapbackPacket(otserver4j.structure.PlayerCharacter player) {
    return new RawPacket().writeByte(SNAPBACK_CODE).writeByte(player.getDirection().getCode());
  }

  public void send(SocketChannel socketChannel) throws IOException {
    final ByteBuffer bufferTemp = ByteBuffer.allocate(RawPacket.MAX_SIZE);
    bufferTemp.put(this.bufferWithSize()); bufferTemp.flip();
    while(bufferTemp.hasRemaining()) socketChannel.write(bufferTemp);
  }

  public void sendAndClose(SocketChannel socketChannel) throws IOException {
    this.send(socketChannel);
    socketChannel.close();
  }

}
