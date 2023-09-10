package otserver4j.repository;

import otserver4j.structure.Account;

@org.springframework.stereotype.Repository
public interface AccountRepository
  extends org.springframework.data.repository.CrudRepository<Account, Integer> { }
