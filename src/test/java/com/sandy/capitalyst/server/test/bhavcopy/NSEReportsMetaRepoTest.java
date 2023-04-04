package com.sandy.capitalyst.server.test.bhavcopy;

import com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class NSEReportsMetaRepoTest {

    @Test
    public void metaLoad() throws Exception {

        NSEReportsMetaRepo repo = NSEReportsMetaRepo.instance() ;
        assertNotNull( repo.getCurrentReportMeta( NSEReportsMetaRepo.BHAVCOPY_META_KEY ) ) ;
        assertNotNull( repo.getCurrentReportMeta( NSEReportsMetaRepo.INDEX_META_KEY ) ) ;
    }
}
