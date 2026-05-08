package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.InboundRequest;
import com.hebei.systemdemo.domain.po.MaterialCode;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.domain.po.WarehouseLocation;
import com.hebei.systemdemo.mapper.InboundRequestMapper;
import com.hebei.systemdemo.mapper.MaterialCodeMapper;
import com.hebei.systemdemo.mapper.WarehouseLocationMapper;
import com.hebei.systemdemo.service.IInboundRequestService;
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
public class InboundRequestService implements IInboundRequestService {
    @Autowired
    private InboundRequestMapper inboundRequestMapper;
    @Autowired
    private MaterialCodeMapper materialCodeMapper;
    @Autowired
    private WarehouseLocationMapper warehouseLocationMapper;

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, String inboundNo, String materialCode, String materialName, String areaCode, String locationCode, String inboundStatus) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<InboundRequest> page = PageHelper.startPage(current, size);
        List<InboundRequest> records = inboundRequestMapper.page(productionLineId, trimToNull(inboundNo), trimToNull(materialCode), trimToNull(materialName), trimToNull(areaCode), trimToNull(locationCode), trimToNull(inboundStatus), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result add(InboundRequest inboundRequest, Long userId, String username, String realName) {
        if (inboundRequest == null || inboundRequest.getProductionLineId() == null) return Result.fail(ResultCode.BAD_REQUEST, "生产线ID不能为空");
        Result check = validateReference(inboundRequest);
        if (check != null) return check;
        normalize(inboundRequest);
        inboundRequest.setInboundNo(generateInboundNo());
        inboundRequest.setInboundAmount(calcAmount(inboundRequest.getInboundQty(), inboundRequest.getUnitPrice()));
        inboundRequest.setInboundStatus("00-待确认入库");
        inboundRequest.setCreatorId(userId);
        inboundRequest.setCreatorUsername(trimToNull(username));
        inboundRequest.setCreatorName(trimToNull(realName) == null ? trimToNull(username) : trimToNull(realName));
        inboundRequest.setCreatedAt(nowString());
        inboundRequest.setUpdatedAt(nowString());
        int rows = inboundRequestMapper.add(inboundRequest);
        if (rows <= 0 || inboundRequest.getId() == null) return Result.fail("新增入库申请失败");
        return Result.ok(Collections.singletonMap("id", inboundRequest.getId()));
    }

    @Override
    public Result update(InboundRequest inboundRequest) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (inboundRequest == null || inboundRequest.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "入库申请ID不能为空");
        InboundRequest exist = inboundRequestMapper.getById(inboundRequest.getId(), currentUserId());
        if (exist == null) return Result.fail(ResultCode.NOT_FOUND, "入库申请不存在或无权限修改");
        if (!"00-待确认入库".equals(exist.getInboundStatus())) return Result.fail("仅待确认入库状态允许修改");
        inboundRequest.setProductionLineId(exist.getProductionLineId());
        inboundRequest.setInboundNo(exist.getInboundNo());
        Result check = validateReference(inboundRequest);
        if (check != null) return check;
        normalize(inboundRequest);
        inboundRequest.setInboundAmount(calcAmount(inboundRequest.getInboundQty(), inboundRequest.getUnitPrice()));
        inboundRequest.setUpdatedAt(nowString());
        int rows = inboundRequestMapper.updateById(inboundRequest);
        if (rows <= 0) return Result.fail("更新入库申请失败");
        return Result.ok();
    }

    @Override
    public Result confirmInbound(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "确认ID列表不能为空");
        for (Long id : ids) {
            InboundRequest exist = inboundRequestMapper.getById(id, currentUserId());
            if (exist == null) return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的入库申请");
            if (!"00-待确认入库".equals(exist.getInboundStatus())) return Result.fail("仅待确认入库状态允许确认");
        }
        int rows = inboundRequestMapper.confirmByIds(ids, nowString());
        if (rows <= 0) return Result.fail("确认入库失败");
        return Result.ok(String.format("成功确认%d条入库申请", rows), Collections.singletonMap("confirmedCount", rows));
    }

    @Override
    public Result batchDelete(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        for (Long id : ids) {
            InboundRequest exist = inboundRequestMapper.getById(id, currentUserId());
            if (exist == null) return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的入库申请");
            if (!"00-待确认入库".equals(exist.getInboundStatus())) return Result.fail("仅待确认入库状态允许删除");
        }
        int rows = inboundRequestMapper.batchDeleteByIds(ids);
        if (rows <= 0) return Result.fail("删除入库申请失败");
        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Result validateReference(InboundRequest inboundRequest) {
        MaterialCode query = new MaterialCode();
        query.setMaterialCode(inboundRequest.getMaterialCode());
        List<MaterialCode> materialCodes = materialCodeMapper.list(query);
        if (materialCodes == null || materialCodes.isEmpty()) return Result.fail("所选物料不存在");
        MaterialCode materialCode = materialCodes.get(0);
        if (!"02-激活".equals(materialCode.getStatus())) return Result.fail("所选物料状态不是02-激活");
        WarehouseLocation warehouseLocation = warehouseLocationMapper.getById(inboundRequest.getWarehouseLocationId(), currentUserId());
        if (warehouseLocation == null) return Result.fail("所选库区库位不存在");
        if (!inboundRequest.getProductionLineId().equals(warehouseLocation.getProductionLineId())) return Result.fail("所选库区库位与当前生产线不一致");
        return null;
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private void normalize(InboundRequest inboundRequest) {
        inboundRequest.setInboundNo(trimToNull(inboundRequest.getInboundNo()));
        inboundRequest.setMaterialCode(trimToNull(inboundRequest.getMaterialCode()));
        inboundRequest.setMaterialName(trimToNull(inboundRequest.getMaterialName()));
        inboundRequest.setModelSpecification(trimToNull(inboundRequest.getModelSpecification()));
        inboundRequest.setAreaCode(trimToNull(inboundRequest.getAreaCode()));
        inboundRequest.setLocationCode(trimToNull(inboundRequest.getLocationCode()));
        inboundRequest.setInventoryProperty(trimToNull(inboundRequest.getInventoryProperty()));
        inboundRequest.setInboundType(trimToNull(inboundRequest.getInboundType()));
        inboundRequest.setInboundDate(trimToNull(inboundRequest.getInboundDate()));
    }

    private Double calcAmount(Integer qty, Double price) {
        double q = qty == null ? 0D : qty.doubleValue();
        double p = price == null ? 0D : price;
        return Math.round(q * p * 100.0) / 100.0;
    }

    private String generateInboundNo() {
        return "RK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim();
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
