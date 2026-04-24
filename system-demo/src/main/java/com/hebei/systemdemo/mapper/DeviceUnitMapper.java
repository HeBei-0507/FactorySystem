package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.pojo.DeviceUnit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceUnitMapper {
    @Select("SELECT du.id, du.production_line_id, du.unit_code, du.unit_name, du.creator_id, du.created_at AS create_at, du.updated_at AS update_at FROM device_unit du WHERE du.production_line_id = #{productionLineId}")
    List<DeviceUnit> getDeviceUnitListByProductionLineId(Long productionLineId);

    @Insert({
            "<script>",
            "INSERT INTO device_unit",
            "<trim prefix='(' suffix=')' suffixOverrides=','>",
            "production_line_id,",
            "unit_code,",
            "unit_name,",
            "<if test='creatorId != null'>creator_id,</if>",
            "<if test='createAt != null and createAt != \"\"'>created_at,</if>",
            "<if test='updateAt != null and updateAt != \"\"'>updated_at,</if>",
            "</trim>",
            "<trim prefix='VALUES (' suffix=')' suffixOverrides=','>",
            "#{productionLineId},",
            "#{unitCode},",
            "#{unitName},",
            "<if test='creatorId != null'>#{creatorId},</if>",
            "<if test='createAt != null and createAt != \"\"'>#{createAt},</if>",
            "<if test='updateAt != null and updateAt != \"\"'>#{updateAt},</if>",
            "</trim>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addDeviceUnit(DeviceUnit deviceUnit);

    List<DeviceUnit> page(@Param("productionLineId") Long productionLineId,
                          @Param("unitCode") String unitCode,
                          @Param("unitName") String unitName);
}
