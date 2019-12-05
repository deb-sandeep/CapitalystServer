package com.sandy.capitalyst.server.dao.fixed_deposit;

import org.springframework.data.repository.CrudRepository ;

public interface FixedDepositRepo 
    extends CrudRepository<FixedDeposit, Integer> {
}
