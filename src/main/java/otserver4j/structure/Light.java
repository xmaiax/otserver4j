package otserver4j.structure;

@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class Light {
  private byte radius;
  private byte color;
}
