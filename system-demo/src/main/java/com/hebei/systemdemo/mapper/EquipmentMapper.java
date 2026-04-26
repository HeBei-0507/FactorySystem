package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.pojo.Equipment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipmentMapper {
    List<Equipment> page(@Param("unitCode") String unitCode,
                         @Param("equipmentCode") String equipmentCode,
                         @Param("equipmentName") String equipmentName);

    Equipment getById(Long id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    int add(Equipment equipment);

    int updateById(Equipment equipment);

    @Delete("DELETE FROM equipment WHERE id = #{id}")
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

    List<Equipment> listByLineCode(@Param("lineCode") String lineCode);
}
