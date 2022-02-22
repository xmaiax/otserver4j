package com.github.xmaiax.packet;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.Getter;

@Getter
public class Packet {

  public static final Integer MAX_SIZE = 0xffff,
    CHARACTERS_LIST_START = 0x64, LOGIN_CODE_OK = 0x14, LOGIN_CODE_NOK = 0x0a,
    PROCESSING_LOGIN_CODE_OK = 0x0a, PROCESSING_LOGIN_CODE_NOK = 0x14;

  public static int readByte(ByteBuffer input) {
    return input.get() & 0xff;
  }

  public static int readInt16(ByteBuffer input) {
    return readByte(input) | (readByte(input) << 8);
  }
  
  public static int readInt32(ByteBuffer input) {
    return readByte(input)        | (readByte(input) <<  8)
        | (readByte(input) << 16) | (readByte(input) << 24);
  }
  
  public static String readString(ByteBuffer input) {
    return java.util.stream.IntStream.iterate(ZERO.intValue(), i -> readByte(input))
      .limit(readInt16(input) + ONE.intValue()).collect(ByteArrayOutputStream::new,
        (baos, i) -> baos.write((byte) i), (baos1, baos2) -> baos1.write(baos2.toByteArray(),
          ZERO.intValue(), baos2.size())).toString().substring(ONE.intValue());
  }
  
  public static void skip(ByteBuffer input, int n) {
    input.position(input.position() + n);
  }

  public static Packet createGenericErrorPacket(int code, String message) {
    return new Packet().writeByte(code).writeString(message);
  }

  private int size;
  private byte[] buffer = new byte[MAX_SIZE - 2];

  public Packet writeByte(byte _byte) {
    this.buffer[this.size++] = _byte;
    return this;
  }
  
  public Packet writeByte(int _byte) {
    return this.writeByte(Integer.valueOf(_byte & 0xff).byteValue());
  }
  
  public Packet writeByte(char _byte) {
    return this.writeByte((byte) _byte);
  }

  public Packet writeInt16(int _int) {
    return this.writeByte(_int & 0x00ff).writeByte((_int & 0xff00) >> 8);
  }

  public Packet writeInt32(long _long) {
    return this
      .writeByte( (int)(_long & 0x000000ff))
      .writeByte(((int)(_long & 0x0000ff00)) >>  8)
      .writeByte(((int)(_long & 0x00ff0000)) >> 16)
      .writeByte(((int)(_long & 0xff000000)) >> 24);
  }

  public Packet writeString(String _str) {
    this.writeInt16(_str.length());
    _str.chars().forEachOrdered(c -> this.writeByte(c));
    return this;
  }

  public byte[] bufferWithSize() {
    final byte[] output = new byte[this.size + 0x2];
    System.arraycopy(new byte[] { ((byte)(this.size & 0x00ff)),
      ((byte)((this.size & 0xff00) >> 8)), }, 0, output, 0, 0x2);
    System.arraycopy(this.buffer, 0, output, 0x2, this.size);
    return output;
  }

  public void send(java.nio.channels.SocketChannel sc) throws IOException {
    final ByteBuffer bufferTemp = ByteBuffer.allocate(Packet.MAX_SIZE);
    bufferTemp.put(this.bufferWithSize());
    bufferTemp.flip();
    while(bufferTemp.hasRemaining())
      sc.write(bufferTemp);
  }

}
