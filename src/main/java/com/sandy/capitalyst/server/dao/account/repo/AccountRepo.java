package com.sandy.capitalyst.server.dao.account.repo;

import java.util.List ;
import java.util.Set ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.account.Account ;

public interface AccountRepo 
    extends CrudRepository<Account, Integer> {
    
    public Account findByAccountNumber( String accountNumber ) ;
    
    public List<Account> findByAccountType( String accountType ) ;
    
    public List<Account> findByAccountTypeIn( Set<String> accountTypes ) ;
}
