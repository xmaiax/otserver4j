package otserver4j.repository;

import otserver4j.entity.MessageOfTheDayEntity;

@org.springframework.stereotype.Repository public interface MessageOfTheDayRepository
    extends org.springframework.data.repository.CrudRepository<MessageOfTheDayEntity, Integer> {
  MessageOfTheDayEntity findTopByOrderByCreationTimeDesc();
}
