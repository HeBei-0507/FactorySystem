package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.Abnormality;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AbnormalityMapper {
    int add(Abnormality abnormality);

    int updateById(Abnormality abnormality);

    Abnormality getById(@Param("id") Long id,
                        @Param("creatorUsername") String creatorUsername);

    List<Abnormality> page(@Param("abnormalCode") String abnormalCode,
                           @Param("deviceUnitCode") String deviceUnitCode,
                           @Param("reporter") String reporter,
                           @Param("reportDate") String reportDate,
                           @Param("status") String status,
                           @Param("abnormalType") String abnormalType,
                           @Param("creatorUsername") String creatorUsername);

    int deleteById(@Param("id") Long id);

    int batchDeleteByIds(@Param("ids") List<Long> ids);

    int updateStatusByIds(@Param("ids") List<Long> ids,
                          @Param("fromStatus") String fromStatus,
                          @Param("toStatus") String toStatus,
                          @Param("updatedAt") String updatedAt);
}
