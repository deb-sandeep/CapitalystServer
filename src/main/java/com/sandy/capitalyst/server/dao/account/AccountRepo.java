package com.sandy.capitalyst.server.dao.account;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

public interface AccountRepo 
    extends CrudRepository<Account, Integer> {
    
    public Account findByAccountNumber( String accountNumber ) ;
    
    public List<Account> findByAccountType( String accountType ) ;
}
