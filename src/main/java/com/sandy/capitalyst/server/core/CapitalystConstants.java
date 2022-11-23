package com.sandy.capitalyst.server.core;

// NOTE: If a new entry is made here, add the corresponding value in RefData
public class CapitalystConstants {

    public static enum Bank {
        ICICI,
        SBI,
        HOME,
        BAFINS
    } ;
    
    public static enum AccountType {
        SAVING,
        CURRENT,
        FIXED_DEPOSIT,
        RECURRING_DEPOSIT,
        LINKED_FD,
        CREDIT,
        PPF,
        NBFC_FD
    }
}
