package com.sandy.capitalyst.server.api.account.stmtparser;

import java.io.File ;
import java.util.List ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public abstract class AccountStmtParser {

    public abstract List<LedgerEntry> parseLedgerEntries( Account account, 
                                                          File file )
            throws Exception ;

}
