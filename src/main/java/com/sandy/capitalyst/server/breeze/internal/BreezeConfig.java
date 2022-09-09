package com.sandy.capitalyst.server.breeze.internal;

import java.io.File ;
import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.breeze.BreezeCred ;

import lombok.Data ;

@Data
public class BreezeConfig {

    private File serializationDir = null ;
    private List<BreezeCred> credentials = new ArrayList<>() ;
}
