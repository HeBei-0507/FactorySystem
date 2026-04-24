package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.pojo.Equipment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EquipmentMapper {
    @Select({
            "<script>",
            "SELECT id, unit_code, unit_name, equipment_code, equipment_name, maintainer_name, equipment_category, equipment_importance, repair_strategy, overhaul_team, created_at, updated_at",
            "FROM equipment",
            "<where>",
            "  <if test='unitCode != null and unitCode != \"\"'> AND unit_code LIKE CONCAT('%', #{unitCode}, '%') </if>",
            "  <if test='equipmentCode != null and equipmentCode != \"\"'> AND equipment_code LIKE CONCAT('%', #{equipmentCode}, '%') </if>",
            "  <if test='equipmentName != null and equipmentName != \"\"'> AND equipment_name LIKE CONCAT('%', #{equipmentName}, '%') </if>",
            "</where>",
            "ORDER BY id DESC",
            "</script>"
    })
    List<Equipment> page(@Param("unitCode") String unitCode,
                         @Param("equipmentCode") String equipmentCode,
                         @Param("equipmentName") String equipmentName);

    @Select("SELECT id, unit_code, unit_name, equipment_code, equipment_name, maintainer_name, equipment_category, equipment_importance, repair_strategy, overhaul_team, created_at, updated_at FROM equipment WHERE id = #{id}")
    Equipment getById(Long id);

    @Insert({
            "<script>",
            "INSERT INTO equipment",
            "<trim prefix='(' suffix=')' suffixOverrides=','>",
            "unit_code,",
            "unit_name,",
            "equipment_code,",
            "equipment_name,",
            "<if test='maintainerName != null and maintainerName != \"\"'>maintainer_name,</if>",
            "<if test='equipmentCategory != null and equipmentCategory != \"\"'>equipment_category,</if>",
            "<if test='equipmentImportance != null and equipmentImportance != \"\"'>equipment_importance,</if>",
            "<if test='repairStrategy != null and repairStrategy != \"\"'>repair_strategy,</if>",
            "<if test='overhaulTeam != null and overhaulTeam != \"\"'>overhaul_team,</if>",
            "<if test='createdAt != null and createdAt != \"\"'>created_at,</if>",
            "<if test='updatedAt != null and updatedAt != \"\"'>updated_at,</if>",
            "</trim>",
            "<trim prefix='VALUES (' suffix=')' suffixOverrides=','>",
            "#{unitCode},",
            "#{unitName},",
            "#{equipmentCode},",
            "#{equipmentName},",
            "<if test='maintainerName != null and maintainerName != \"\"'>#{maintainerName},</if>",
            "<if test='equipmentCategory != null and equipmentCategory != \"\"'>#{equipmentCategory},</if>",
            "<if test='equipmentImportance != null and equipmentImportance != \"\"'>#{equipmentImportance},</if>",
            "<if test='repairStrategy != null and repairStrategy != \"\"'>#{repairStrategy},</if>",
            "<if test='overhaulTeam != null and overhaulTeam != \"\"'>#{overhaulTeam},</if>",
            "<if test='createdAt != null and createdAt != \"\"'>#{createdAt},</if>",
            "<if test='updatedAt != null and updatedAt != \"\"'>#{updatedAt},</if>",
            "</trim>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int add(Equipment equipment);

    @Update({
            "<script>",
            "UPDATE equipment",
            "<set>",
            "unit_code = #{unitCode},",
            "unit_name = #{unitName},",
            "equipment_code = #{equipmentCode},",
            "equipment_name = #{equipmentName},",
            "maintainer_name = #{maintainerName},",
            "equipment_category = #{equipmentCategory},",
            "equipment_importance = #{equipmentImportance},",
            "repair_strategy = #{repairStrategy},",
            "overhaul_team = #{overhaulTeam},",
            "<if test='createdAt != null and createdAt != \"\"'>created_at = #{createdAt},</if>",
            "<if test='updatedAt != null and updatedAt != \"\"'>updated_at = #{updatedAt},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateById(Equipment equipment);

    @Delete("DELETE FROM equipment WHERE id = #{id}")
    int deleteById(Long id);
}
