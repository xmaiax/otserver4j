package otserver4j.structure;

@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class Position {
  private Integer x;
  private Integer y;
  private Integer z;
  public Position move(Direction direction) {
    switch(direction) {
      case EAST: return this.setX(this.getX() + 1);
      case NORTH: return this.setY(this.getY() - 1);
      case SOUTH: return this.setY(this.getY() + 1);
      case WEST: return this.setX(this.getX() - 1);
      case NORTHWEST: return this.move(Direction.NORTH).move(Direction.WEST);
      case NORTHEAST: return this.move(Direction.NORTH).move(Direction.EAST);
      case SOUTHEAST: return this.move(Direction.SOUTH).move(Direction.EAST);
      case SOUTHWEST: return this.move(Direction.SOUTH).move(Direction.WEST);
    }
    return this;
  }
  public Position copy() {
    return new Position().setX(this.getX()).setY(this.getY()).setZ(this.getZ());
  }
}
