package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.Abnormality;
import com.hebei.systemdemo.domain.po.AbnormalityDeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AbnormalityDealMapper {
    AbnormalityDeal getByAbnormalityId(@Param("abnormalityId") Long abnormalityId,
                                       @Param("creatorUsername") String creatorUsername);

    int deleteByAbnormalityId(@Param("abnormalityId") Long abnormalityId,
                              @Param("creatorUsername") String creatorUsername);

    int add(AbnormalityDeal abnormalityDeal);

    int updateByAbnormalityId(AbnormalityDeal abnormalityDeal);

    List<Abnormality> page(@Param("abnormalCode") String abnormalCode,
                           @Param("deviceUnitCode") String deviceUnitCode,
                           @Param("reporter") String reporter,
                           @Param("reportDate") String reportDate,
                           @Param("status") String status,
                           @Param("abnormalType") String abnormalType,
                           @Param("creatorUsername") String creatorUsername);

    List<Abnormality> approvePage(@Param("abnormalCode") String abnormalCode,
                                  @Param("deviceUnitCode") String deviceUnitCode,
                                  @Param("reporter") String reporter,
                                  @Param("reportDate") String reportDate,
                                  @Param("status") String status,
                                  @Param("abnormalType") String abnormalType,
                                  @Param("creatorUsername") String creatorUsername);
}
