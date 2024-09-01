package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.txn.TxnEntry;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<TxnEntry.Payment, Integer> {

}
