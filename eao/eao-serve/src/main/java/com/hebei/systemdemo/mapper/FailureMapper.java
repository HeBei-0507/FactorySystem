package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.Failure;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FailureMapper {
    int add(Failure failure);

    int updateById(Failure failure);

    Failure getById(@Param("id") Long id, @Param("creatorUsername") String creatorUsername);

    List<Failure> page(@Param("failureCode") String failureCode,
                       @Param("devicePartCode") String devicePartCode,
                       @Param("failureName") String failureName,
                       @Param("failureStartTime") String failureStartTime,
                       @Param("status") String status,
                       @Param("failureType") String failureType,
                       @Param("creatorUsername") String creatorUsername);

    int deleteById(@Param("id") Long id);

    int batchDeleteByIds(@Param("ids") List<Long> ids);

    int updateStatusByIds(@Param("ids") List<Long> ids,
                          @Param("fromStatus") String fromStatus,
                          @Param("toStatus") String toStatus,
                          @Param("updatedAt") String updatedAt);
}
