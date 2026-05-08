package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.EquipmentPart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipmentPartMapper {

    List<EquipmentPart> page(@Param("equipmentId") Long equipmentId,
                             @Param("partCode") String partCode,
                             @Param("partName") String partName,
                             @Param("maintainer") String maintainer,
                             @Param("partType") String partType,
                             @Param("repairStrategy") String repairStrategy,
                             @Param("importanceLevel") String importanceLevel,
                             @Param("repairTeam") String repairTeam,
                             @Param("operateTeam") String operateTeam,
                             @Param("creatorId") Long creatorId);

    List<EquipmentPart> list(EquipmentPart equipmentPart);

    EquipmentPart getById(@Param("id") Long id,
                          @Param("creatorId") Long creatorId);

    int add(EquipmentPart equipmentPart);

    int updateById(EquipmentPart equipmentPart);

    int deleteById(Long id);

    EquipmentPart getByEquipmentIdAndPartCodeAndCreatorId(@Param("equipmentId") Long equipmentId,
                                                          @Param("partCode") String partCode,
                                                          @Param("creatorId") Long creatorId);
}
