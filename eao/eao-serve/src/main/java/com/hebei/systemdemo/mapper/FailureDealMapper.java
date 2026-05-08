package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.Failure;
import com.hebei.systemdemo.domain.po.FailureDeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FailureDealMapper {
    FailureDeal getByFailureId(@Param("failureId") Long failureId);

    int deleteByFailureId(@Param("failureId") Long failureId);

    int add(FailureDeal failureDeal);

    int updateByFailureId(FailureDeal failureDeal);

    List<Failure> page(@Param("failureCode") String failureCode,
                       @Param("devicePartCode") String devicePartCode,
                       @Param("failureName") String failureName,
                       @Param("failureStartTime") String failureStartTime,
                       @Param("status") String status,
                       @Param("failureType") String failureType,
                       @Param("creatorUsername") String creatorUsername);

    List<Failure> approvePage(@Param("failureCode") String failureCode,
                              @Param("devicePartCode") String devicePartCode,
                              @Param("failureName") String failureName,
                              @Param("failureStartTime") String failureStartTime,
                              @Param("status") String status,
                              @Param("failureType") String failureType,
                              @Param("creatorUsername") String creatorUsername);
}
