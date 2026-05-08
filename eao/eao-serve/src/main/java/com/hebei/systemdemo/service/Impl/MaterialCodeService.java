package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.MaterialCode;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.MaterialCodeMapper;
import com.hebei.systemdemo.service.IMaterialCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaterialCodeService implements IMaterialCodeService {
    @Autowired
    private MaterialCodeMapper materialCodeMapper;

    @Override
    public Result addMaterialCode(MaterialCode materialCode) {
        materialCode.setMaterialCode(trimToNull(materialCode.getMaterialCode()));
        materialCode.setMaterialName(trimToNull(materialCode.getMaterialName()));
        materialCode.setMaterialDescription(trimToNull(materialCode.getMaterialDescription()));
        materialCode.setModelSpecification(trimToNull(materialCode.getModelSpecification()));
        materialCode.setDrawingNo(trimToNull(materialCode.getDrawingNo()));
        materialCode.setMaterialMajorCategory(trimToNull(materialCode.getMaterialMajorCategory()));
        materialCode.setMaterialSubCategory(trimToNull(materialCode.getMaterialSubCategory()));
        materialCode.setMaterialProperty(trimToNull(materialCode.getMaterialProperty()));
        materialCode.setStatus(trimToNull(materialCode.getStatus()));

        if (!materialCodeMapper.list(new MaterialCode().setMaterialCode(materialCode.getMaterialCode())).isEmpty()) {
            return Result.fail("物料代码已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        materialCode.setCreatedAt(now);
        materialCode.setModifiedAt(now);
        int rows = materialCodeMapper.addMaterialCode(materialCode);
        if (rows <= 0 || materialCode.getId() == null) {
            return Result.fail("新增物料代码失败");
        }
        return Result.ok(Collections.singletonMap("id", materialCode.getId()));
    }

    @Override
    public Result updateMaterialCode(MaterialCode materialCode) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (materialCode.getId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "物料代码ID不能为空");
        }

        MaterialCode existMaterialCode = materialCodeMapper.getById(materialCode.getId(), currentUserId());
        if (existMaterialCode == null) {
            return Result.fail(ResultCode.NOT_FOUND, "物料代码不存在或无权限修改");
        }

        materialCode.setMaterialCode(trimToNull(materialCode.getMaterialCode()));
        materialCode.setMaterialName(trimToNull(materialCode.getMaterialName()));
        materialCode.setMaterialDescription(trimToNull(materialCode.getMaterialDescription()));
        materialCode.setModelSpecification(trimToNull(materialCode.getModelSpecification()));
        materialCode.setDrawingNo(trimToNull(materialCode.getDrawingNo()));
        materialCode.setMaterialMajorCategory(trimToNull(materialCode.getMaterialMajorCategory()));
        materialCode.setMaterialSubCategory(trimToNull(materialCode.getMaterialSubCategory()));
        materialCode.setMaterialProperty(trimToNull(materialCode.getMaterialProperty()));
        materialCode.setStatus(trimToNull(materialCode.getStatus()));

        if (!materialCodeMapper.list(new MaterialCode().setMaterialCode(materialCode.getMaterialCode())).isEmpty()
                && !materialCode.getMaterialCode().equals(existMaterialCode.getMaterialCode())) {
            return Result.fail("物料代码已存在");
        }

        materialCode.setModifiedAt(LocalDateTime.now());
        int rows = materialCodeMapper.updateMaterialCode(materialCode);
        if (rows <= 0) {
            return Result.fail("修改物料代码失败");
        }
        return Result.ok("修改成功", null);
    }

    @Override
    public Result getById(Long id) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "物料代码ID不能为空");
        }
        MaterialCode materialCode = materialCodeMapper.getById(id, currentUserId());
        if (materialCode == null) {
            return Result.fail(ResultCode.NOT_FOUND, "物料代码不存在或无权限访问");
        }
        return Result.ok(materialCode);
    }

    @Override
    public Result page(Integer current, Integer size, String materialCode, String materialName,
                       String materialMajorCategory, String materialSubCategory,
                       String materialProperty, String status) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }

        Page<MaterialCode> page = PageHelper.startPage(current, size);
        List<MaterialCode> records = materialCodeMapper.page(
                trimToNull(materialCode),
                trimToNull(materialName),
                trimToNull(materialMajorCategory),
                trimToNull(materialSubCategory),
                trimToNull(materialProperty),
                trimToNull(status),
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
    public Result deleteById(Long id) {
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "物料代码ID不能为空");
        }
        int rows = materialCodeMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除物料代码失败");
        }
        return Result.ok("删除成功", Collections.singletonMap("deletedCount", rows));
    }

    @Override
    public Result batchDeleteMaterialCode(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        }
        int rows = materialCodeMapper.batchDeleteByIds(ids);
        if (rows <= 0) {
            return Result.fail("删除物料代码失败");
        }
        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
