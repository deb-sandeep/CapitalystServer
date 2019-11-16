package com.sandy.capitalyst.server.dao.account;

import org.springframework.data.repository.CrudRepository ;

public interface AccountIndexRepo 
    extends CrudRepository<Account, Integer> {
    
    public Account findByAccountNumber( String accountNumber ) ;
}
