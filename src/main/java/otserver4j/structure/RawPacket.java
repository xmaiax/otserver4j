package otserver4j.structure;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

import java.nio.ByteBuffer;

@lombok.Getter public class RawPacket {

  public static final Integer MAX_SIZE = 0xffff,
    PROCESSING_LOGIN_CODE_OK = 0x0a, PROCESSING_LOGIN_CODE_NOK = 0x14,
    SNAPBACK_CODE = 0xb5;

  public static Integer readByte(final ByteBuffer input) {
    return input.get() & 0xff;
  }
  public static Integer readInt16(final ByteBuffer input) {
    return readByte(input) | (readByte(input) << 8); }
  public static Integer readInt32(final ByteBuffer input) {
    return readByte(input)        | (readByte(input) <<  8)
        | (readByte(input) << 16) | (readByte(input) << 24);
  }

  public static String readString(final ByteBuffer input) {
    return java.util.stream.IntStream.iterate(ZERO.intValue(), i -> readByte(input))
      .limit(readInt16(input) + ONE.intValue()).collect(java.io.ByteArrayOutputStream::new,
        (baos, i) -> baos.write((byte) i), (baos1, baos2) -> baos1.write(baos2.toByteArray(),
          ZERO.intValue(), baos2.size())).toString().substring(ONE.intValue()); }

  public static void skip(final ByteBuffer input, final Integer n) {
    input.position(input.position() + n); }

  public static RawPacket createGenericErrorPacket(final Integer code, final String message) {
    return new RawPacket().writeByte(code).writeString(message); }

  private Integer size = ZERO.intValue();
  private byte[] buffer = new byte[MAX_SIZE - TWO.intValue()];

  public RawPacket writeByte(final byte _byte) {
    this.buffer[this.size++] = _byte;
    return this;
  }

  public RawPacket writeByte(final int _byte) { return this.writeByte(
    Integer.valueOf(_byte & 0xff).byteValue()); }
  public RawPacket writeByte(final char _byte) { return this.writeByte((byte) _byte); }
  public RawPacket writeInt16(final Integer _int) { return this.writeByte(_int & 0x00ff)
    .writeByte((_int & 0xff00) >> 8); }

  public RawPacket writeInt32(final Long _long) {
    return this
      .writeByte( (int)(_long & 0x000000ff))
      .writeByte(((int)(_long & 0x0000ff00)) >>  8)
      .writeByte(((int)(_long & 0x00ff0000)) >> 16)
      .writeByte(((int)(_long & 0xff000000)) >> 24);
  }

  public RawPacket writeString(final String _str) {
    this.writeInt16(_str.length());
    _str.chars().forEachOrdered(c -> this.writeByte(c));
    return this;
  }

  public byte[] bufferWithSize() {
    final byte[] output = new byte[this.size + TWO.intValue()];
    System.arraycopy(new byte[] { ((byte)(this.size & 0x00ff)),
      ((byte)((this.size & 0xff00) >> 8)), }, ZERO.intValue(), output, ZERO.intValue(), TWO.intValue());
    System.arraycopy(this.buffer, ZERO.intValue(), output, 0x02, this.size);
    return output;
  }

  public void send(final java.nio.channels.SocketChannel socketChannel) throws java.io.IOException {
    final ByteBuffer bufferTemp = ByteBuffer.allocate(RawPacket.MAX_SIZE);
    bufferTemp.put(this.bufferWithSize()); bufferTemp.flip();
    while(bufferTemp.hasRemaining()) socketChannel.write(bufferTemp);
  }

}
