package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.maintenanceTicket.MaintenanceTicketAddDTO;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.domain.po.MaintenancePlan;
import com.hebei.systemdemo.domain.po.MaintenanceStandard;
import com.hebei.systemdemo.domain.po.MaintenanceTicket;
import com.hebei.systemdemo.domain.po.MaintenanceTicketSafetyTag;
import com.hebei.systemdemo.domain.po.MaintenanceTicketSparePart;
import com.hebei.systemdemo.domain.po.MaintenanceTicketTool;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.EquipmentPartMapper;
import com.hebei.systemdemo.mapper.MaintenancePlanMapper;
import com.hebei.systemdemo.mapper.MaintenanceStandardMapper;
import com.hebei.systemdemo.mapper.MaintenanceTicketMapper;
import com.hebei.systemdemo.mapper.MaintenanceTicketSafetyTagMapper;
import com.hebei.systemdemo.mapper.MaintenanceTicketSparePartMapper;
import com.hebei.systemdemo.mapper.MaintenanceTicketToolMapper;
import com.hebei.systemdemo.service.IMaintenanceTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaintenanceTicketService implements IMaintenanceTicketService {
    private static final String PENDING = "00-待提交";
    private static final String SUBMITTED = "01-待审核";
    private static final String APPROVED = "02-已通过";
    private static final String PLAN_OK = "20-已通过";

    @Autowired private MaintenanceTicketMapper ticketMapper;
    @Autowired private MaintenanceTicketSparePartMapper spareMapper;
    @Autowired private MaintenanceTicketToolMapper toolMapper;
    @Autowired private MaintenanceTicketSafetyTagMapper tagMapper;
    @Autowired private EquipmentPartMapper partMapper;
    @Autowired private MaintenancePlanMapper planMapper;
    @Autowired private MaintenanceStandardMapper standardMapper;

    public Result page(Integer current,Integer size,String ticketCode,String ticketName,String status,String partCode,String partName,String standardCode,String planCode){
        if(currentUserId()==null)return Result.fail("未登录，请先登录");
        if(current==null||current<1)return Result.fail("当前页码必须大于等于1");
        if(size==null||size<1)return Result.fail("每页条数必须大于等于1");
        Page<MaintenanceTicket> page=PageHelper.startPage(current,size);
        List<MaintenanceTicket> records=ticketMapper.page(t(ticketCode),t(ticketName),t(status),t(partCode),t(partName),t(standardCode),t(planCode),currentUserId());
        Map<String,Object> data=new HashMap<>(); data.put("records",records); data.put("total",page.getTotal()); data.put("current",current); data.put("size",size); return Result.ok(data);
    }

    public Result getById(Long id){
        if(currentUserId()==null)return Result.fail("未登录，请先登录");
        if(id==null)return Result.fail("工单ID不能为空");
        MaintenanceTicket x=ticketMapper.getById(id,currentUserId()); if(x==null)return Result.fail("工单不存在或无权限访问");
        Map<String,Object> d=new HashMap<>(); d.put("ticket",x); d.put("spareParts",spareMapper.listByTicketId(id)); d.put("tools",toolMapper.listByTicketId(id)); d.put("safetyTags",tagMapper.listByTicketId(id)); return Result.ok(d);
    }

    @Transactional public Result add(MaintenanceTicket x,MaintenanceTicketAddDTO dto){ Result r=fill(x); if(r!=null)return r; user(x); x.setTicketCode(code()); x.setStatus(StringUtils.hasText(x.getStatus())?t(x.getStatus()):PENDING); x.setCreatedAt(now()); x.setUpdatedAt(now()); if(ticketMapper.add(x)<=0||x.getId()==null)return Result.fail("新增工单失败"); save(x.getId(),dto); return Result.ok(Map.of("id",x.getId(),"ticketCode",x.getTicketCode())); }
    @Transactional public Result update(MaintenanceTicket x,MaintenanceTicketAddDTO dto){ if(x.getId()==null)return Result.fail("工单ID不能为空"); MaintenanceTicket e=ticketMapper.getById(x.getId(),currentUserId()); if(e==null)return Result.fail("工单不存在或无权限修改"); if(!PENDING.equals(e.getStatus()))return Result.fail("仅待提交状态的工单允许修改"); Result r=fill(x); if(r!=null)return r; x.setTicketCode(e.getTicketCode()); x.setStatus(StringUtils.hasText(x.getStatus())?t(x.getStatus()):PENDING); x.setCreatorId(e.getCreatorId()); x.setCreatorUsername(e.getCreatorUsername()); x.setCreatorName(e.getCreatorName()); x.setCreatedAt(e.getCreatedAt()); x.setUpdatedAt(now()); if(ticketMapper.updateById(x)<=0)return Result.fail("修改工单失败"); clear(x.getId()); save(x.getId(),dto); return Result.ok(); }
    @Transactional public Result deleteByIds(List<Long> ids){ List<Long> xs=ids(ids); if(xs.isEmpty())return Result.fail("请选择要删除的工单"); int r=ticketMapper.deleteByIds(xs,PENDING,currentUserId()); if(r<=0)return Result.fail("仅本人待提交状态的工单允许删除"); xs.forEach(this::clear); return Result.ok(); }
    @Transactional public Result submitByIds(List<Long> ids){ List<Long> xs=ids(ids); if(xs.isEmpty())return Result.fail("请选择待提交的工单送审"); int r=ticketMapper.updateStatusByIds(xs,PENDING,SUBMITTED,now(),currentUserId()); if(r<=0)return Result.fail("状态更新失败，请确认所选工单状态是否正确或是否属于本人"); return Result.ok(); }
    @Transactional public Result approveByIds(List<Long> ids){ List<Long> xs=ids(ids); if(xs.isEmpty())return Result.fail("请选择待审核的工单进行同意"); int r=ticketMapper.updateStatusByIds(xs,SUBMITTED,APPROVED,now(),currentUserId()); if(r<=0)return Result.fail("状态更新失败，请确认所选工单状态是否正确或是否属于本人"); return Result.ok(); }
    @Transactional public Result rollbackApproveByIds(List<Long> ids){ List<Long> xs=ids(ids); if(xs.isEmpty())return Result.fail("请选择待审核的工单进行回退"); int r=ticketMapper.updateStatusByIds(xs,SUBMITTED,PENDING,now(),currentUserId()); if(r<=0)return Result.fail("状态更新失败，请确认所选工单状态是否正确或是否属于本人"); return Result.ok(); }

    private Result fill(MaintenanceTicket x){
        x.setTicketName(t(x.getTicketName())); x.setMaintenancePlanName(t(x.getMaintenancePlanName())); x.setOperationTeam(t(x.getOperationTeam())); x.setTaskType(t(x.getTaskType())); x.setProductionImpact(t(x.getProductionImpact())); x.setEntrustType(t(x.getEntrustType())); x.setMaintenanceResource(t(x.getMaintenanceResource())); x.setMaintenanceContent(t(x.getMaintenanceContent())); x.setAbnormalPhenomenon(t(x.getAbnormalPhenomenon())); x.setHandlingOpinion(t(x.getHandlingOpinion())); x.setPlanCode(t(x.getPlanCode())); x.setMaintenanceCategory(t(x.getMaintenanceCategory())); x.setMaintenanceStartTime(t(x.getMaintenanceStartTime())); x.setMaintenanceEndTime(t(x.getMaintenanceEndTime())); x.setStandardCode(t(x.getStandardCode())); x.setPartCode(t(x.getPartCode())); x.setPartName(t(x.getPartName())); x.setItemName(t(x.getItemName())); x.setProfession(t(x.getProfession())); x.setRiskFactor(t(x.getRiskFactor())); x.setSafetyMeasure(t(x.getSafetyMeasure())); x.setWorkCategory(t(x.getWorkCategory())); x.setAcceptanceLevel(t(x.getAcceptanceLevel()));
        if(!StringUtils.hasText(x.getTicketName()))return Result.fail("工单名称不能为空"); if(!StringUtils.hasText(x.getPartCode()))return Result.fail("设备部位编码不能为空"); if(!StringUtils.hasText(x.getItemName()))return Result.fail("项目名称不能为空");
        List<EquipmentPart> parts=partMapper.list(new EquipmentPart().setPartCode(x.getPartCode())); if(parts==null||parts.isEmpty())return Result.fail("设备部位不存在"); if(!StringUtils.hasText(x.getPartName()))x.setPartName(t(parts.get(0).getPartName()));
        if(x.getPlanId()!=null){ MaintenancePlan p=planMapper.getById(x.getPlanId(),currentUserId()); if(p==null)return Result.fail("检修计划不存在或无权限引用"); if(!PLAN_OK.equals(p.getStatus()))return Result.fail("仅已通过的检修计划允许引用"); if(!StringUtils.hasText(x.getPlanCode()))x.setPlanCode(t(p.getPlanCode())); if(!StringUtils.hasText(x.getMaintenanceCategory()))x.setMaintenanceCategory(t(p.getMaintenanceCategory())); if(!StringUtils.hasText(x.getMaintenanceStartTime()))x.setMaintenanceStartTime(t(p.getMaintenanceStartTime())); if(!StringUtils.hasText(x.getMaintenanceEndTime()))x.setMaintenanceEndTime(t(p.getMaintenanceEndTime())); }
        if(x.getStandardId()!=null){ MaintenanceStandard s=standardMapper.getById(x.getStandardId(),currentUserId()); if(s==null)return Result.fail("检修标准不存在或无权限引用"); if(!StringUtils.hasText(x.getStandardCode()))x.setStandardCode(t(s.getStandardCode())); x.setPartCode(t(s.getPartCode())); if(!StringUtils.hasText(x.getPartName()))x.setPartName(t(s.getPartName())); x.setItemName(t(s.getItemName())); if(!StringUtils.hasText(x.getProfession()))x.setProfession(t(s.getProfession())); if(!StringUtils.hasText(x.getRiskFactor()))x.setRiskFactor(t(s.getRiskFactor())); if(!StringUtils.hasText(x.getSafetyMeasure()))x.setSafetyMeasure(t(s.getSafetyMeasure())); if(!StringUtils.hasText(x.getWorkCategory()))x.setWorkCategory(t(s.getWorkCategory())); if(!StringUtils.hasText(x.getAcceptanceLevel()))x.setAcceptanceLevel(t(s.getAcceptanceLevel())); if(!StringUtils.hasText(x.getMaintenanceCategory()))x.setMaintenanceCategory(t(s.getMaintenanceCategory())); }
        return null;
    }

    private void save(Long id,MaintenanceTicketAddDTO d){
        if(id==null||d==null)return;
        if(d.getSpareParts()!=null)d.getSpareParts().forEach(i->{ if(i.getMaterialCodeId()!=null&&i.getInboundRequestId()!=null)spareMapper.add(new MaintenanceTicketSparePart().setTicketId(id).setMaterialCodeId(i.getMaterialCodeId()).setInboundRequestId(i.getInboundRequestId()).setMaterialCode(t(i.getMaterialCode())).setMaterialName(t(i.getMaterialName())).setMaterialSubCategory(t(i.getMaterialSubCategory())).setModelSpecification(t(i.getModelSpecification())).setQuantity(i.getQuantity()).setQuantityUnit(t(i.getQuantityUnit())).setCreatedAt(now()).setUpdatedAt(now())); });
        if(d.getTools()!=null)d.getTools().forEach(i->{ if(i.getMaterialCodeId()!=null&&i.getInboundRequestId()!=null)toolMapper.add(new MaintenanceTicketTool().setTicketId(id).setMaterialCodeId(i.getMaterialCodeId()).setInboundRequestId(i.getInboundRequestId()).setMaterialCode(t(i.getMaterialCode())).setMaterialName(t(i.getMaterialName())).setMaterialSubCategory(t(i.getMaterialSubCategory())).setModelSpecification(t(i.getModelSpecification())).setQuantity(i.getQuantity()).setQuantityUnit(t(i.getQuantityUnit())).setCreatedAt(now()).setUpdatedAt(now())); });
        if(d.getSafetyTags()!=null)d.getSafetyTags().forEach(i->{ if(StringUtils.hasText(i.getTagNature())||StringUtils.hasText(i.getTagLocation())||StringUtils.hasText(i.getTagDeviceCode()))tagMapper.add(new MaintenanceTicketSafetyTag().setTicketId(id).setTagNature(t(i.getTagNature())).setTagLocation(t(i.getTagLocation())).setTagDeviceCode(t(i.getTagDeviceCode())).setCreatedAt(now()).setUpdatedAt(now())); });
    }

    private void clear(Long id){ spareMapper.deleteByTicketId(id); toolMapper.deleteByTicketId(id); tagMapper.deleteByTicketId(id); }
    private Long currentUserId(){ SysUser u=UserContext.getUser(); return u==null?null:u.getId(); }
    private void user(MaintenanceTicket x){ SysUser u=UserContext.getUser(); x.setCreatorId(u==null?null:u.getId()); x.setCreatorUsername(u==null?null:t(u.getUsername())); x.setCreatorName(u==null?null:t(u.getRealName())); }
    private List<Long> ids(List<Long> ids){ if(ids==null)return new ArrayList<>(); return ids.stream().filter(i->i!=null&&i>0).distinct().collect(Collectors.toList()); }
    private String code(){ return "MT"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); }
    private String t(String v){ return !StringUtils.hasText(v)?null:v.trim(); }
    private String now(){ return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
}
