package com.sandy.capitalyst.server.dao.account;

import java.util.Date ;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import lombok.Data ;

@Data
@Entity
@Table( name = "corpus_snapshot" )
public class CorpusSnapshot {

    @Id
    @TableGenerator(
            name            = "corpusSnapshotPkGen", 
            table           = "id_gen", 
            pkColumnName    = "gen_key", 
            valueColumnName = "gen_value", 
            pkColumnValue   = "corpus_snapshot_itd_id",
            initialValue    = 1,
            allocationSize  = 1 )    
    @GeneratedValue( 
            strategy        = GenerationType.TABLE, 
            generator       = "corpusSnapshotPkGen" )
    private Integer id ;
    
    private Date  date ;

    @Column( precision=16 )
    private float savingAccount = 0 ;

    @Column( precision=16 )
    private float fixedDeposit = 0 ;
    
    @Column( precision=16 )
    private float equityInvested = 0 ;

    @Column( precision=16 )
    private float equityMktValue = 0 ;

    @Column( precision=16 )
    private float equityDailyGain = 0 ;
    
    @Column( precision=16 )
    private float equityUnrealizedPat = 0 ;

    @Column( precision=16 )
    private float equityRealizedPat = 0 ;

    @Column( precision=16 )
    private float taxOnRealizedProfit = 0 ;
    
    public float getTotalCorpus() {
        return this.savingAccount + 
               this.fixedDeposit + 
               this.equityMktValue ;
    }
}
