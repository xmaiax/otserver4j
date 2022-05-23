package otserver4j.structure;

@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class Light {
  private byte radius = (byte) 0x7;
  private byte color = (byte) 0xd7;
}
