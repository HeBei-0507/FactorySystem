package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.IDAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IDAddressMapper {
    List<IDAddress> list(IDAddress idAddress);

    int addIDAddress(IDAddress idAddress);

    IDAddress getById(@Param("id") Long id,
                      @Param("creatorUsername") String creatorUsername);

    int updateIDAddress(IDAddress idAddress);

    List<IDAddress> page(@Param("idLocationCode") String idLocationCode,
                         @Param("idLocationName") String idLocationName,
                         @Param("idLocationInnerCode") String idLocationInnerCode,
                         @Param("locationType") String locationType,
                         @Param("idLocationCategory") String idLocationCategory,
                         @Param("creatorUsername") String creatorUsername);

    int deleteById(@Param("id") Long id);

    int batchDeleteByIds(@Param("ids") List<Long> ids);

    IDAddress getByCodeAndCreatorUsername(@Param("idLocationCode") String idLocationCode,
                                          @Param("creatorUsername") String creatorUsername);
}
