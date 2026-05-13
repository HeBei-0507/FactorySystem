package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaterialCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaterialCodeMapper {
    List<MaterialCode> list(MaterialCode materialCode);

    int addMaterialCode(MaterialCode materialCode);

    MaterialCode getById(@Param("id") Long id,
                         @Param("creatorId") Long creatorId);

    int updateMaterialCode(MaterialCode materialCode);

    List<MaterialCode> page(@Param("materialCode") String materialCode,
                            @Param("materialName") String materialName,
                            @Param("materialMajorCategory") String materialMajorCategory,
                            @Param("materialSubCategory") String materialSubCategory,
                            @Param("materialProperty") String materialProperty,
                            @Param("status") String status,
                            @Param("creatorId") Long creatorId);

    int deleteById(@Param("id") Long id,
                   @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids,
                         @Param("creatorId") Long creatorId);
}
