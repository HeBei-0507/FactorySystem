package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.maintenanceStandard.MaintenanceStandardAddDTO;
import com.hebei.systemdemo.domain.po.MaintenanceStandard;

import java.util.List;

public interface IMaintenanceStandardService {
    Result page(Integer current, Integer size, String standardCode, String partCode, String partName,
                String itemName, String profession, String maintenanceCategory,
                String maintenanceStartTime, String maintenanceEndTime, String workCategory,
                String workContent, List<String> partCodes);

    Result getById(Long id);

    Result add(MaintenanceStandard maintenanceStandard, MaintenanceStandardAddDTO dto);

    Result update(MaintenanceStandard maintenanceStandard, MaintenanceStandardAddDTO dto);

    Result deleteById(Long id);

    Result getMaterialCandidates(Integer current, Integer size, String materialCode, String materialName,
                                 String materialSubCategory, String inboundStatus);
}
