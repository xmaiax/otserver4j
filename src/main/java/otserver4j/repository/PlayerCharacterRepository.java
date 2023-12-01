package otserver4j.repository;

@org.springframework.stereotype.Repository
public interface PlayerCharacterRepository
  extends org.springframework.data.repository.CrudRepository<
    otserver4j.entity.PlayerCharacterEntity, Long> { }
