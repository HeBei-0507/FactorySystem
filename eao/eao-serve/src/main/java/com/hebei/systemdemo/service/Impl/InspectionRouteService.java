package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.InspectionRoute;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.InspectionRouteMapper;
import com.hebei.systemdemo.service.IInspectionRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InspectionRouteService implements IInspectionRouteService {
    @Autowired
    private InspectionRouteMapper inspectionRouteMapper;

    @Override
    public Result getInspectionRouteListByProductionLineId(Long productionLineId) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        List<InspectionRoute> routeList = inspectionRouteMapper.getInspectionRouteListByProductionLineId(productionLineId, currentUserId());
        return Result.ok(routeList == null ? Collections.emptyList() : routeList);
    }

    @Override
    public Result addInspectionRoute(InspectionRoute inspectionRoute) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        inspectionRoute.setCreatorId(currentUserId());
        inspectionRoute.setCreatedAt(normalizeDateTime(inspectionRoute.getCreatedAt()));
        inspectionRoute.setUpdatedAt(normalizeDateTime(inspectionRoute.getUpdatedAt()));

        List<InspectionRoute> existed = inspectionRouteMapper.list(
                new InspectionRoute().setRouteCode(trimToNull(inspectionRoute.getRouteCode())).setCreatorId(currentUserId())
        );
        if (!existed.isEmpty()) return Result.fail("路线编码已存在");

        int rows = inspectionRouteMapper.addInspectionRoute(inspectionRoute);
        if (rows <= 0 || inspectionRoute.getId() == null) return Result.fail("新增点检路线失败");
        return Result.ok(Collections.singletonMap("id", inspectionRoute.getId()));
    }

    @Override
    public Result updateInspectionRoute(InspectionRoute inspectionRoute) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (inspectionRoute.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "路线ID不能为空");

        InspectionRoute existRoute = inspectionRouteMapper.getById(inspectionRoute.getId(), currentUserId());
        if (existRoute == null) return Result.fail(ResultCode.NOT_FOUND, "点检路线不存在或无权限访问");

        if (StringUtils.hasText(inspectionRoute.getRouteCode())) {
            List<InspectionRoute> duplicateRoutes = inspectionRouteMapper.list(
                    new InspectionRoute().setRouteCode(inspectionRoute.getRouteCode().trim()).setCreatorId(currentUserId())
            );
            boolean duplicated = duplicateRoutes.stream().anyMatch(item -> item != null && !item.getId().equals(existRoute.getId()));
            if (duplicated) return Result.fail("路线编码已存在");
        }

        inspectionRoute.setUpdatedAt(normalizeDateTime(inspectionRoute.getUpdatedAt()));
        int rows = inspectionRouteMapper.updateInspectionRoute(inspectionRoute);
        if (rows <= 0) return Result.fail("更新点检路线失败");

        return Result.ok("更新成功", null);
    }

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, String routeCode, String routeName) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");

        String normalizedRouteCode = trimToNull(routeCode);
        String normalizedRouteName = trimToNull(routeName);

        Page<InspectionRoute> page = PageHelper.startPage(current, size);
        List<InspectionRoute> records = inspectionRouteMapper.page(productionLineId, normalizedRouteCode, normalizedRouteName, currentUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result batchDeleteInspectionRoute(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");

        List<Long> deletableIds = new ArrayList<>();
        for (Long id : ids) {
            InspectionRoute existRoute = inspectionRouteMapper.getById(id, currentUserId());
            if (existRoute != null) deletableIds.add(id);
        }
        if (deletableIds.isEmpty()) return Result.fail("未找到可删除的点检路线或无权限删除");

        int rows = inspectionRouteMapper.batchDeleteByIds(deletableIds);
        if (rows <= 0) return Result.fail("删除点检路线失败");

        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        return trimmed.length() == 10 ? trimmed + " 00:00:00" : trimmed;
    }
}
