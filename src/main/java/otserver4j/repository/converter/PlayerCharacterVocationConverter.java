package otserver4j.repository.converter;

import otserver4j.structure.PlayerCharacterVocation;

@javax.persistence.Converter(autoApply = true) public class PlayerCharacterVocationConverter
    implements javax.persistence.AttributeConverter<PlayerCharacterVocation, Integer> {
  @Override public Integer convertToDatabaseColumn(final PlayerCharacterVocation vocation) {
    return vocation == null ? null : vocation.getCode(); }
  @Override public PlayerCharacterVocation convertToEntityAttribute(final Integer vocationCode) {
    return vocationCode == null ? null : PlayerCharacterVocation.fromCode(vocationCode); }
}
