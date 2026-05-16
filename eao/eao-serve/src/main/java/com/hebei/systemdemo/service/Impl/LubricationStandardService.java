package com.hebei.systemdemo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.lubricationStandard.LubricationStandardImportDTO;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.domain.po.LubricationStandard;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.EquipmentPartMapper;
import com.hebei.systemdemo.mapper.LubricationStandardMapper;
import com.hebei.systemdemo.service.ILubricationStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LubricationStandardService implements ILubricationStandardService {
    @Autowired
    private LubricationStandardMapper lubricationStandardMapper;
    @Autowired
    private EquipmentPartMapper equipmentPartMapper;

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, Long deviceUnitId, Long equipmentId,
                       String standardCode, String partCode, String partName,
                       String feedPoint, String oilModels, String profession, String oilFeedType) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<LubricationStandard> page = PageHelper.startPage(current, size);
        List<LubricationStandard> records = lubricationStandardMapper.page(
                productionLineId,
                deviceUnitId,
                equipmentId,
                trimToNull(standardCode),
                trimToNull(partCode),
                trimToNull(partName),
                trimToNull(feedPoint),
                trimToNull(oilModels),
                trimToNull(profession),
                trimToNull(oilFeedType),
                currentUserId()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result getById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        LubricationStandard standard = lubricationStandardMapper.getById(id, currentUserId());
        if (standard == null) return Result.fail("未找到润滑标准信息或无权限访问");
        return Result.ok(standard);
    }

    @Override
    public Result add(LubricationStandard lubricationStandard) {
        if (!StringUtils.hasText(lubricationStandard.getPartCode())) return Result.fail("设备部位编码不能为空");
        EquipmentPart matchedPart = findEquipmentPartByCode(lubricationStandard.getPartCode(), currentUserId());
        if (matchedPart == null) return Result.fail("设备部位编码不存在或无权限使用");
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) return Result.fail("无法获取当前用户信息");
        lubricationStandard.setPartCode(trimToNull(matchedPart.getPartCode()));
        lubricationStandard.setPartName(resolvePartName(lubricationStandard.getPartName(), matchedPart));
        lubricationStandard.setStandardCode(generateStandardCode(username));
        lubricationStandard.setCreatorId(UserContext.getUserId());
        lubricationStandard.setCreatedAt(normalizeDateTime(lubricationStandard.getCreatedAt()));
        lubricationStandard.setUpdatedAt(normalizeDateTime(lubricationStandard.getUpdatedAt()));
        int rows = lubricationStandardMapper.add(lubricationStandard);
        if (rows <= 0 || lubricationStandard.getId() == null) return Result.fail("新增润滑标准失败");
        return Result.ok(Collections.singletonMap("id", lubricationStandard.getId()));
    }

    @Override
    public Result batchAdd(LubricationStandardImportDTO importDTO, Long userId) {
        if (importDTO == null || importDTO.getLubricationStandardList() == null || importDTO.getLubricationStandardList().isEmpty()) {
            return Result.fail("导入数据不能为空");
        }
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) return Result.fail("无法获取当前用户信息");
        List<String> failedRecords = new ArrayList<>();
        int successCount = 0;
        for (var standardAddDTO : importDTO.getLubricationStandardList()) {
            try {
                LubricationStandard lubricationStandard = BeanUtil.copyProperties(standardAddDTO, LubricationStandard.class);
                if (!StringUtils.hasText(lubricationStandard.getPartCode())) {
                    failedRecords.add("设备部位编码[为空]：设备部位编码不能为空");
                    continue;
                }
                EquipmentPart matchedPart = findEquipmentPartByCode(lubricationStandard.getPartCode(), userId);
                if (matchedPart == null) {
                    failedRecords.add("设备部位编码[" + lubricationStandard.getPartCode() + "]不存在或无权限，已跳过");
                    continue;
                }
                lubricationStandard.setPartCode(trimToNull(matchedPart.getPartCode()));
                lubricationStandard.setPartName(resolvePartName(lubricationStandard.getPartName(), matchedPart));
                lubricationStandard.setStandardCode(generateStandardCode(username));
                lubricationStandard.setCreatorId(userId);
                lubricationStandard.setCreatedAt(normalizeDateTime(lubricationStandard.getCreatedAt()));
                lubricationStandard.setUpdatedAt(normalizeDateTime(lubricationStandard.getUpdatedAt()));
                int rows = lubricationStandardMapper.add(lubricationStandard);
                if (rows > 0 && lubricationStandard.getId() != null) successCount++;
                else failedRecords.add("润滑标准[加油点:" + lubricationStandard.getFeedPoint() + "]：新增失败");
            } catch (Exception e) {
                failedRecords.add("润滑标准[加油点:" + standardAddDTO.getFeedPoint() + "]：异常 - " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", failedRecords.size());
        result.put("failedRecords", failedRecords);
        return failedRecords.isEmpty()
                ? Result.ok("批量新增成功，共" + successCount + "条", result)
                : Result.ok("部分成功，成功" + successCount + "条，失败" + failedRecords.size() + "条", result);
    }

    @Override
    public Result update(LubricationStandard lubricationStandard) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (lubricationStandard.getId() == null) return Result.fail("润滑标准ID不能为空");
        LubricationStandard existStandard = lubricationStandardMapper.getById(lubricationStandard.getId(), currentUserId());
        if (existStandard == null) return Result.fail("润滑标准不存在或无权限修改");
        if (StringUtils.hasText(lubricationStandard.getPartCode())) {
            EquipmentPart matchedPart = findEquipmentPartByCode(lubricationStandard.getPartCode(), currentUserId());
            if (matchedPart == null) return Result.fail("设备部位编码不存在或无权限使用");
            lubricationStandard.setPartCode(trimToNull(matchedPart.getPartCode()));
            lubricationStandard.setPartName(resolvePartName(lubricationStandard.getPartName(), matchedPart));
        }
        lubricationStandard.setUpdatedAt(normalizeDateTime(lubricationStandard.getUpdatedAt()));
        int rows = lubricationStandardMapper.updateById(lubricationStandard, currentUserId());
        if (rows <= 0) return Result.fail("更新润滑标准失败");
        return Result.ok();
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        LubricationStandard existStandard = lubricationStandardMapper.getById(id, currentUserId());
        if (existStandard == null) return Result.fail("润滑标准不存在或无权限删除");
        int rows = lubricationStandardMapper.deleteById(id, currentUserId());
        if (rows <= 0) return Result.fail("删除润滑标准失败");
        return Result.ok();
    }

    @Override
    public Result getStandardListByPartCode(String partCode) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        EquipmentPart matchedPart = findEquipmentPartByCode(partCode, currentUserId());
        if (matchedPart == null) return Result.ok(Collections.emptyList());
        List<Map<String, Object>> list = lubricationStandardMapper
                .list(new LubricationStandard().setPartCode(trimToNull(partCode)).setCreatorId(currentUserId()))
                .stream()
                .map(standard -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", standard.getId());
                    result.put("standardName", standard.getStandardName());
                    result.put("standardCode", standard.getStandardCode());
                    return result;
                }).collect(Collectors.toList());
        return Result.ok(list);
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    private EquipmentPart findEquipmentPartByCode(String partCode, Long userId) {
        if (!StringUtils.hasText(partCode) || userId == null) return null;
        List<EquipmentPart> matchedParts = equipmentPartMapper.list(new EquipmentPart().setPartCode(partCode.trim()).setCreatorId(userId));
        return matchedParts.isEmpty() ? null : matchedParts.get(0);
    }

    private String resolvePartName(String partName, EquipmentPart matchedPart) {
        String normalizedName = trimToNull(partName);
        String actualName = matchedPart == null ? null : trimToNull(matchedPart.getPartName());
        return StringUtils.hasText(normalizedName) ? normalizedName : actualName;
    }

    private String generateStandardCode(String username) {
        List<LubricationStandard> allStandards = lubricationStandardMapper.list(new LubricationStandard().setCreatorId(currentUserId()));
        long count = allStandards.stream().filter(s -> s.getStandardCode() != null && s.getStandardCode().startsWith(username + "-")).count();
        return username + "-" + String.format("%06d", count + 1);
    }

    private String normalizeDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        return trimmed.length() == 10 ? trimmed + " 00:00:00" : trimmed;
    }
}
