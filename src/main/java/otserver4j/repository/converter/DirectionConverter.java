package otserver4j.repository.converter;

import otserver4j.structure.Direction;

@javax.persistence.Converter(autoApply = true) public class DirectionConverter
    implements javax.persistence.AttributeConverter<Direction, String> {
  @Override public String convertToDatabaseColumn(final Direction direction) {
    return direction == null ? null : direction.getDbCode(); }
  @Override public Direction convertToEntityAttribute(final String directionDatabaseCode) {
    return directionDatabaseCode == null || directionDatabaseCode.isBlank() ? null :
      Direction.fromDatabaseCode(directionDatabaseCode); }
}
