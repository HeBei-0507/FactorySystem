package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.Equipment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    List<Equipment> page(@Param("deviceUnitId") Long deviceUnitId,
                         @Param("equipmentCode") String equipmentCode,
                         @Param("equipmentName") String equipmentName,
                         @Param("equipmentCategory") String equipmentCategory,
                         @Param("equipmentImportance") String equipmentImportance,
                         @Param("repairStrategy") String repairStrategy,
                         @Param("overhaulTeam") String overhaulTeam,
                         @Param("actTeam") String actTeam,
                         @Param("creatorId") Long creatorId);

    List<Equipment> list(Equipment equipment);

    Equipment getById(@Param("id") Long id,
                      @Param("creatorId") Long creatorId);

    int add(Equipment equipment);

    int updateById(Equipment equipment);

    int deleteById(Long id);

    @Delete({
            "<script>",
            "DELETE FROM equipment WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int deleteBatch(@Param("ids") List<Long> ids);

    List<Equipment> listByLineCode(@Param("lineCode") String lineCode,
                                   @Param("equipmentCode") String equipmentCode,
                                   @Param("equipmentName") String equipmentName,
                                   @Param("equipmentCategory") String equipmentCategory,
                                   @Param("equipmentImportance") String equipmentImportance,
                                   @Param("repairStrategy") String repairStrategy,
                                   @Param("overhaulTeam") String overhaulTeam,
                                   @Param("actTeam") String actTeam,
                                   @Param("creatorId") Long creatorId);

    Equipment getByEquipmentCodeAndCreatorId(@Param("equipmentCode") String equipmentCode,
                                             @Param("creatorId") Long creatorId);
}
