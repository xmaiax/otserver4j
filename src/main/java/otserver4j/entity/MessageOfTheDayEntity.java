package otserver4j.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.PrePersist;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Data @ToString @Accessors(chain = true)
public class MessageOfTheDayEntity {
  @Column(name = "motd_id") @javax.persistence.Id
  @GeneratedValue(generator = "motd_sequence")
  @GenericGenerator(strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      name = "motd_sequence", parameters = {
    @Parameter(name = "sequence_name", value = "motd_sequence"),
    @Parameter(name = "initial_value", value = "1"),
    @Parameter(name = "increment_size", value = "1"),
  }) private Integer identifier;
  @Column(nullable = false, updatable = false, length = 138) private String message;
  @Column(nullable = false, updatable = false) private LocalDateTime creationTime;
  @PrePersist public void prePersist() { this.creationTime = LocalDateTime.now(); }
}
