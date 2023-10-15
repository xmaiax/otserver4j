package otserver4j.repository;

import otserver4j.entity.AccountEntity;

@org.springframework.stereotype.Repository
public interface AccountRepository
  extends org.springframework.data.repository.CrudRepository<AccountEntity, Integer> { }
