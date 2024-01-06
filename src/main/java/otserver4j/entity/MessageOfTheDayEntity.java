package otserver4j.entity;

import javax.persistence.Column;

import org.hibernate.annotations.Parameter;

@javax.persistence.Entity @javax.persistence.Table(name = MessageOfTheDayEntity.TABLE_NAME)
@lombok.Data @lombok.experimental.Accessors(chain = true) public class MessageOfTheDayEntity {
  static final String TABLE_NAME = "motd";
  @Column(name = MessageOfTheDayEntity.TABLE_NAME + "_id") @javax.persistence.Id
  @javax.persistence.GeneratedValue(generator = MessageOfTheDayEntity.TABLE_NAME + "_sequence")
  @org.hibernate.annotations.GenericGenerator(strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      name = "motd_sequence", parameters = {
    @Parameter(name = "sequence_name", value = MessageOfTheDayEntity.TABLE_NAME + "_sequence"),
    @Parameter(name = "initial_value", value = "1"),
    @Parameter(name = "increment_size", value = "1"),
  }) private Integer identifier;
  @Column(nullable = false, updatable = false, length = 138) private String message;
  @Column(nullable = false, updatable = false) private java.time.LocalDateTime creationTime;
  @javax.persistence.PrePersist public void prePersist() {
    this.creationTime = java.time.LocalDateTime.now(); }
}
