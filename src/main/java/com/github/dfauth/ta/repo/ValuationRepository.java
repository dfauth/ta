package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Valuation;
import com.github.dfauth.ta.model.ValuationCompositeKey;
import org.springframework.data.repository.CrudRepository;

public interface ValuationRepository extends CrudRepository<Valuation, ValuationCompositeKey> {

}
