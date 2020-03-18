package com.sandy.capitalyst.server.api.refdata;

import static com.sandy.capitalyst.server.CapitalystServer.getAppContext ;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.dao.individual.Individual ;
import com.sandy.capitalyst.server.dao.individual.repo.IndividualRepo ;

public class RefData {

    private List<String> bankNames = null ;
    private List<String> accountTypes = null ;
    private List<Individual> individuals = null ;

    private static RefData instance = null ;
    
    private RefData() {
        this.bankNames = createBankNamesList() ;
        this.accountTypes = createAccountTypesList() ;
        this.individuals = createIndividualsList() ;
    }
    
    private List<String> createBankNamesList() {
        List<String> values = new ArrayList<>() ;
        values.add( "ICICI" ) ;
        values.add( "SBI" ) ;
        values.add( "HOME" ) ;
        return values ;
    }
    
    private List<String> createAccountTypesList() {
        List<String> values = new ArrayList<>() ;
        values.add( "SAVING" ) ;
        values.add( "CURRENT" ) ;
        values.add( "FIXED_DEPOSIT" ) ;
        values.add( "RECURRING_DEPOSIT" ) ;
        values.add( "LINKED_FD" ) ;
        values.add( "CREDIT" ) ;
        values.add( "PPF" ) ;
        return values ;
    }
    
    private List<Individual> createIndividualsList() {
        IndividualRepo iRepo = null ;
        iRepo = getAppContext().getBean( IndividualRepo.class ) ;
        
        List<Individual> values = new ArrayList<>() ;
        for( Individual person : iRepo.findAll() ) {
            values.add( person ) ;
        }
        return values ;
    }
    
    public static RefData instance() {
        if( instance == null ) {
            instance = new RefData() ;
        }
        return instance ;
    }

    public void setBankNames( List<String> val ) {
        this.bankNames = val ;
    }
        
    public List<String> getBankNames() {
        return this.bankNames ;
    }

    public void setAccountTypes( List<String> val ) {
        this.accountTypes = val ;
    }
        
    public List<String> getAccountTypes() {
        return this.accountTypes ;
    }

    public void setIndividuals( List<Individual> val ) {
        this.individuals = val ;
    }
        
    public List<Individual> getIndividuals() {
        return this.individuals ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "RefData [\n" ) ; 

        builder.append( "   bankNames = " + this.bankNames + "\n" ) ;
        builder.append( "   accountTypes = " + this.accountTypes + "\n" ) ;
        builder.append( "   individuals = " + this.individuals + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
