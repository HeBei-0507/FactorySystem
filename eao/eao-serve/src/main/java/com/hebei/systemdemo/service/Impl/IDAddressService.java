package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.IDAddress;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.IDAddressMapper;
import com.hebei.systemdemo.service.IIDAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IDAddressService implements IIDAddressService {
    @Autowired
    private IDAddressMapper idAddressMapper;

    @Override
    public Result addIDAddress(IDAddress idAddress) {
        normalize(idAddress);
        if (currentUsername() != null && idAddressMapper.getByCodeAndCreatorUsername(idAddress.getIdLocationCode(), currentUsername()) != null) {
            return Result.fail("ID位置编码已存在");
        }
        if (idAddress.getCreatedAt() == null) {
            idAddress.setCreatedAt(LocalDate.now());
        }
        int rows = idAddressMapper.addIDAddress(idAddress);
        if (rows <= 0 || idAddress.getId() == null) {
            return Result.fail("新增ID位置失败");
        }
        return Result.ok(Collections.singletonMap("id", idAddress.getId()));
    }

    @Override
    public Result updateIDAddress(IDAddress idAddress) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (idAddress.getId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "ID位置ID不能为空");
        }
        IDAddress exist = idAddressMapper.getById(idAddress.getId(), currentUsername());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "ID位置不存在或无权限修改");
        }
        normalize(idAddress);
        IDAddress sameCode = idAddressMapper.getByCodeAndCreatorUsername(idAddress.getIdLocationCode(), currentUsername());
        if (sameCode != null && !idAddress.getIdLocationCode().equals(exist.getIdLocationCode())) {
            return Result.fail("ID位置编码已存在");
        }
        int rows = idAddressMapper.updateIDAddress(idAddress);
        if (rows <= 0) {
            return Result.fail("修改ID位置失败");
        }
        return Result.ok("修改成功", null);
    }

    @Override
    public Result getById(Long id) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "ID位置ID不能为空");
        }
        IDAddress idAddress = idAddressMapper.getById(id, currentUsername());
        if (idAddress == null) {
            return Result.fail(ResultCode.NOT_FOUND, "ID位置不存在或无权限访问");
        }
        return Result.ok(idAddress);
    }

    @Override
    public Result page(Integer current, Integer size, String idLocationCode, String idLocationName,
                       String idLocationInnerCode, String locationType, String idLocationCategory) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }
        Page<IDAddress> page = PageHelper.startPage(current, size);
        List<IDAddress> records = idAddressMapper.page(
                trimToNull(idLocationCode),
                trimToNull(idLocationName),
                trimToNull(idLocationInnerCode),
                trimToNull(locationType),
                trimToNull(idLocationCategory),
                currentUsername()
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
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "ID位置ID不能为空");
        }
        IDAddress exist = idAddressMapper.getById(id, currentUsername());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "ID位置不存在或无权限删除");
        }
        int rows = idAddressMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除ID位置失败");
        }
        return Result.ok("删除成功", Collections.singletonMap("deletedCount", rows));
    }

    @Override
    public Result batchDeleteIDAddress(List<Long> ids) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        }
        for (Long id : ids) {
            IDAddress exist = idAddressMapper.getById(id, currentUsername());
            if (exist == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的ID位置");
            }
        }
        int rows = idAddressMapper.batchDeleteByIds(ids);
        if (rows <= 0) {
            return Result.fail("删除ID位置失败");
        }
        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private String currentUsername() {
        SysUser user = UserContext.getUser();
        return user == null ? null : trimToNull(user.getUsername());
    }

    private void normalize(IDAddress idAddress) {
        idAddress.setIdLocationCode(trimToNull(idAddress.getIdLocationCode()));
        idAddress.setIdLocationName(trimToNull(idAddress.getIdLocationName()));
        idAddress.setIdLocationInnerCode(trimToNull(idAddress.getIdLocationInnerCode()));
        idAddress.setLocationType(trimToNull(idAddress.getLocationType()));
        idAddress.setIdLocationCategory(trimToNull(idAddress.getIdLocationCategory()));
        idAddress.setCreatorUsername(trimToNull(idAddress.getCreatorUsername()));
        idAddress.setCreatorName(trimToNull(idAddress.getCreatorName()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
