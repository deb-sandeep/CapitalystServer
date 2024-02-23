package com.sandy.capitalyst.server.dao.job.repo;

import com.sandy.capitalyst.server.dao.job.JobEntry;
import com.sandy.capitalyst.server.dao.job.JobRunEntry;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class JobRunEntrySpecifications {

    public static Specification<JobRunEntry> buildRootSpecification(
            String[] jobNames,
            String result,
            Date fromDate, Date toDate ) {

        Specification<JobRunEntry> rootSpec = Specification.where( null ) ;
        if( jobNames != null ) {
            rootSpec = rootSpec.and( getJobNameSpecification( jobNames ) ) ;
        }

        if( result != null && !result.equalsIgnoreCase( "any" )) {
            rootSpec = rootSpec.and( getResultSpecification( result ) ) ;
        }

        if( fromDate != null ) {
            rootSpec = rootSpec.and( getFromDateSpecification( fromDate ) ) ;
        }

        if( toDate != null ) {
            rootSpec = rootSpec.and( getToDateSpecification( toDate ) ) ;
        }

        return rootSpec ;
    }

    private static Specification<JobRunEntry> getJobNameSpecification( String[] jobNames ) {
        Specification<JobRunEntry> jobNameSpec = Specification.where( null ) ;
        for( String jobName : jobNames ) {
            if( jobName.startsWith( "~" ) ) {
                jobNameSpec = jobNameSpec.or(JobRunEntrySpecifications.hasJobNameLike( jobName.substring(1) )) ;
            }
            else {
                jobNameSpec = jobNameSpec.or(JobRunEntrySpecifications.hasJobName( jobName ) ) ;
            }
        }
        return jobNameSpec ;
    }

    private static Specification<JobRunEntry> getFromDateSpecification( Date fromDate ) {
        return ((root, query, criteriaBuilder) -> {
            return criteriaBuilder.greaterThanOrEqualTo( root.get( "date" ), fromDate ) ;
        }) ;
    }

    private static Specification<JobRunEntry> getToDateSpecification( Date toDate ) {
        return ((root, query, criteriaBuilder) -> {
            return criteriaBuilder.lessThan( root.get( "date" ), DateUtils.addDays(toDate,1)) ;
        }) ;
    }

    private static Specification<JobRunEntry> getResultSpecification( String result ) {
        return ((root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal( root.get( "result" ), result ) ;
        }) ;
    }

    private static Specification<JobRunEntry> hasJobName( String jobName ) {
        return ((root, query, criteriaBuilder) -> {
            Join<JobRunEntry, JobEntry> jobJoin = root.join( "job", JoinType.INNER ) ;
            return criteriaBuilder.equal( jobJoin.get( "identity" ), jobName ) ;
        }) ;
    }

    private static Specification<JobRunEntry> hasJobNameLike( String jobNameContains ) {
        return ((root, query, criteriaBuilder) -> {
            Join<JobRunEntry, JobEntry> jobJoin = root.join( "job", JoinType.INNER ) ;
            return criteriaBuilder.like( criteriaBuilder.lower(jobJoin.get( "identity" )),
                                         "%" + jobNameContains.toLowerCase() + "%" ) ;
        }) ;
    }
}
