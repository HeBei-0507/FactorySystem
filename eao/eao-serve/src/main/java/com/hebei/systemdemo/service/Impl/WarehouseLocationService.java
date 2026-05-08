package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.domain.po.WarehouseLocation;
import com.hebei.systemdemo.mapper.WarehouseLocationMapper;
import com.hebei.systemdemo.service.IWarehouseLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarehouseLocationService implements IWarehouseLocationService {
    @Autowired
    private WarehouseLocationMapper warehouseLocationMapper;

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, String areaCode, String locationCode, String keeperUsername, String keeperRealName) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }
        Page<WarehouseLocation> page = PageHelper.startPage(current, size);
        List<WarehouseLocation> records = warehouseLocationMapper.page(
                productionLineId,
                trimToNull(areaCode),
                trimToNull(locationCode),
                trimToNull(keeperUsername),
                trimToNull(keeperRealName),
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
    public Result listByProductionLine(Long productionLineId) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (productionLineId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "生产线ID不能为空");
        }
        return Result.ok(warehouseLocationMapper.list(productionLineId, currentUserId()));
    }

    @Override
    public Result add(WarehouseLocation warehouseLocation, Long userId, String username, String realName) {
        if (warehouseLocation == null || warehouseLocation.getProductionLineId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "生产线ID不能为空");
        }
        normalize(warehouseLocation);
        if (!warehouseLocationMapper.listBySameCode(warehouseLocation.getProductionLineId(), null, warehouseLocation.getAreaCode(), warehouseLocation.getLocationCode(), userId).isEmpty()) {
            return Result.fail("当前生产线下库区与库位组合已存在");
        }
        warehouseLocation.setCreatorId(userId);
        warehouseLocation.setCreatorUsername(trimToNull(username));
        warehouseLocation.setCreatorName(trimToNull(realName) == null ? trimToNull(username) : trimToNull(realName));
        warehouseLocation.setCreatedAt(nowString());
        warehouseLocation.setUpdatedAt(nowString());
        int rows = warehouseLocationMapper.add(warehouseLocation);
        if (rows <= 0 || warehouseLocation.getId() == null) {
            return Result.fail("新增库区库位失败");
        }
        return Result.ok(Collections.singletonMap("id", warehouseLocation.getId()));
    }

    @Override
    public Result update(WarehouseLocation warehouseLocation) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (warehouseLocation == null || warehouseLocation.getId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "库区库位ID不能为空");
        }
        WarehouseLocation exist = warehouseLocationMapper.getById(warehouseLocation.getId(), currentUserId());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "库区库位不存在或无权限修改");
        }
        normalize(warehouseLocation);
        if (!warehouseLocationMapper.listBySameCode(exist.getProductionLineId(), warehouseLocation.getId(), warehouseLocation.getAreaCode(), warehouseLocation.getLocationCode(), currentUserId()).isEmpty()) {
            return Result.fail("当前生产线下库区与库位组合已存在");
        }
        warehouseLocation.setProductionLineId(exist.getProductionLineId());
        warehouseLocation.setUpdatedAt(nowString());
        int rows = warehouseLocationMapper.updateById(warehouseLocation);
        if (rows <= 0) {
            return Result.fail("更新库区库位失败");
        }
        return Result.ok();
    }

    @Override
    public Result batchDelete(List<Long> ids) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        }
        for (Long id : ids) {
            WarehouseLocation exist = warehouseLocationMapper.getById(id, currentUserId());
            if (exist == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的库区库位");
            }
        }
        int rows = warehouseLocationMapper.batchDeleteByIds(ids);
        if (rows <= 0) {
            return Result.fail("删除库区库位失败");
        }
        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private void normalize(WarehouseLocation warehouseLocation) {
        warehouseLocation.setAreaCode(trimToNull(warehouseLocation.getAreaCode()));
        warehouseLocation.setLocationCode(trimToNull(warehouseLocation.getLocationCode()));
        warehouseLocation.setKeeperUsername(trimToNull(warehouseLocation.getKeeperUsername()));
        warehouseLocation.setKeeperRealName(trimToNull(warehouseLocation.getKeeperRealName()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
