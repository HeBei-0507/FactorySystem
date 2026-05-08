package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.SupplierManage;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.SupplierManageMapper;
import com.hebei.systemdemo.service.ISupplierManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierManageService implements ISupplierManageService {
    @Autowired
    private SupplierManageMapper supplierManageMapper;

    @Override
    public Result addSupplierManage(SupplierManage supplierManage) {
        normalize(supplierManage);
        if (currentUserId() != null && supplierManageMapper.getBySupplierCodeAndCreatorId(supplierManage.getSupplierCode(), currentUserId()) != null) {
            return Result.fail("供应商代码已存在");
        }
        if (supplierManage.getCreatedAt() == null) {
            supplierManage.setCreatedAt(LocalDate.now());
        }
        int rows = supplierManageMapper.addSupplierManage(supplierManage);
        if (rows <= 0 || supplierManage.getId() == null) {
            return Result.fail("新增供应商失败");
        }
        return Result.ok(Collections.singletonMap("id", supplierManage.getId()));
    }

    @Override
    public Result updateSupplierManage(SupplierManage supplierManage) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (supplierManage.getId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "供应商ID不能为空");
        }
        SupplierManage exist = supplierManageMapper.getById(supplierManage.getId(), currentUserId());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "供应商不存在或无权限修改");
        }
        normalize(supplierManage);
        SupplierManage sameCode = supplierManageMapper.getBySupplierCodeAndCreatorId(supplierManage.getSupplierCode(), currentUserId());
        if (sameCode != null && !supplierManage.getSupplierCode().equals(exist.getSupplierCode())) {
            return Result.fail("供应商代码已存在");
        }
        int rows = supplierManageMapper.updateSupplierManage(supplierManage);
        if (rows <= 0) {
            return Result.fail("修改供应商失败");
        }
        return Result.ok("修改成功", null);
    }

    @Override
    public Result getById(Long id) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "供应商ID不能为空");
        }
        SupplierManage supplierManage = supplierManageMapper.getById(id, currentUserId());
        if (supplierManage == null) {
            return Result.fail(ResultCode.NOT_FOUND, "供应商不存在或无权限访问");
        }
        return Result.ok(supplierManage);
    }

    @Override
    public Result page(Integer current, Integer size, String supplierCode, String supplierName, String supplierCategory) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }
        Page<SupplierManage> page = PageHelper.startPage(current, size);
        List<SupplierManage> records = supplierManageMapper.page(trimToNull(supplierCode), trimToNull(supplierName), trimToNull(supplierCategory), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "供应商ID不能为空");
        }
        SupplierManage exist = supplierManageMapper.getById(id, currentUserId());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "供应商不存在或无权限删除");
        }
        int rows = supplierManageMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除供应商失败");
        }
        return Result.ok("删除成功", Collections.singletonMap("deletedCount", rows));
    }

    @Override
    public Result batchDeleteSupplierManage(List<Long> ids) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        }
        for (Long id : ids) {
            SupplierManage exist = supplierManageMapper.getById(id, currentUserId());
            if (exist == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的供应商");
            }
        }
        int rows = supplierManageMapper.batchDeleteByIds(ids);
        if (rows <= 0) {
            return Result.fail("删除供应商失败");
        }
        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private void normalize(SupplierManage supplierManage) {
        supplierManage.setSupplierCategory(trimToNull(supplierManage.getSupplierCategory()));
        supplierManage.setSupplierCode(trimToNull(supplierManage.getSupplierCode()));
        supplierManage.setSupplierName(trimToNull(supplierManage.getSupplierName()));
        supplierManage.setAddress(trimToNull(supplierManage.getAddress()));
        supplierManage.setContactPerson(trimToNull(supplierManage.getContactPerson()));
        supplierManage.setContactId(trimToNull(supplierManage.getContactId()));
        supplierManage.setContactEmail(trimToNull(supplierManage.getContactEmail()));
        supplierManage.setFax(trimToNull(supplierManage.getFax()));
        supplierManage.setCreatorUsername(trimToNull(supplierManage.getCreatorUsername()));
        supplierManage.setCreatorName(trimToNull(supplierManage.getCreatorName()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
