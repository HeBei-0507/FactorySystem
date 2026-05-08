# Equipment 接口文档

本文档用于前端对接 `EquipmentController` 提供的设备管理接口。

## 统一返回结构

所有接口统一返回如下结构：

```json
{
  "success": true,
  "errorMsg": null,
  "data": {},
  "total": null
}
```

字段说明：

- `success`：是否成功
- `errorMsg`：失败时的错误信息
- `data`：返回数据
- `total`：当前项目中顶层通常为 `null`，分页总数放在 `data.total` 中

---

## 1. 分页查询设备

### 接口地址

`GET /equipment/page`

### 接口说明

按条件分页查询设备列表。

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| current | Integer | 是 | 当前页码，从 1 开始 |
| size | Integer | 是 | 每页条数 |
| unitCode | String | 否 | 设备单元编码，模糊查询 |
| equipmentCode | String | 否 | 设备编码，模糊查询 |
| equipmentName | String | 否 | 设备名称，模糊查询 |

### 请求示例

```http
GET /equipment/page?current=1&size=10&unitCode=DY&equipmentCode=S2&equipmentName=机械臂
```

### 返回示例

```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "records": [
      {
        "id": 1,
        "unitCode": "DY2",
        "unitName": "上料搬运单元",
        "equipmentCode": "S201",
        "equipmentName": "搬运机械臂",
        "maintainerName": "天津职业师范大学",
        "equipmentCategory": "搬运设备",
        "equipmentImportance": "A类重要设备",
        "repairStrategy": "预防性维护",
        "overhaulTeam": "03-机械组",
        "createdAt": "2026-04-23 10:00:00",
        "updatedAt": "2026-04-23 10:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  },
  "total": null
}
```

---

## 2. 根据 ID 查询设备详情

### 接口地址

`GET /equipment/{id}`

### 接口说明

根据设备主键 ID 查询单条设备信息。

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | Long | 是 | 设备主键 ID |

### 请求示例

```http
GET /equipment/1
```

### 返回示例

```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1,
    "unitCode": "DY2",
    "unitName": "上料搬运单元",
    "equipmentCode": "S201",
    "equipmentName": "搬运机械臂",
    "maintainerName": "天津职业师范大学",
    "equipmentCategory": "搬运设备",
    "equipmentImportance": "A类重要设备",
    "repairStrategy": "预防性维护",
    "overhaulTeam": "03-机械组",
    "createdAt": "2026-04-23 10:00:00",
    "updatedAt": "2026-04-23 10:00:00"
  },
  "total": null
}
```

---

## 3. 新增设备

### 接口地址

`POST /equipment/add`

### 接口说明

新增一条设备记录。

### 请求头

```http
Content-Type: application/json
```

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| unitCode | String | 是 | 设备单元编码 |
| unitName | String | 是 | 设备单元名称 |
| equipmentCode | String | 是 | 设备编码 |
| equipmentName | String | 是 | 设备名称 |
| maintainerName | String | 否 | 设备维护人 |
| equipmentCategory | String | 否 | 设备类别 |
| equipmentImportance | String | 否 | 设备重要度 |
| repairStrategy | String | 否 | 维修策略 |
| overhaulTeam | String | 否 | 检修班组 |
| createdAt | String | 否 | 创建时间，推荐格式 `yyyy-MM-dd HH:mm:ss` |
| updatedAt | String | 否 | 更新时间，推荐格式 `yyyy-MM-dd HH:mm:ss` |

### 请求示例

```json
{
  "unitCode": "DY2",
  "unitName": "上料搬运单元",
  "equipmentCode": "S201",
  "equipmentName": "搬运机械臂",
  "maintainerName": "天津职业师范大学",
  "equipmentCategory": "搬运设备",
  "equipmentImportance": "A类重要设备",
  "repairStrategy": "预防性维护",
  "overhaulTeam": "03-机械组",
  "createdAt": "2026-04-23 10:00:00",
  "updatedAt": "2026-04-23 10:00:00"
}
```

### 返回示例

```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1
  },
  "total": null
}
```

---

## 4. 更新设备

### 接口地址

`PUT /equipment/update`

### 接口说明

根据主键 ID 更新设备信息。

### 请求头

```http
Content-Type: application/json
```

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | Long | 是 | 设备主键 ID |
| unitCode | String | 是 | 设备单元编码 |
| unitName | String | 是 | 设备单元名称 |
| equipmentCode | String | 是 | 设备编码 |
| equipmentName | String | 是 | 设备名称 |
| maintainerName | String | 否 | 设备维护人 |
| equipmentCategory | String | 否 | 设备类别 |
| equipmentImportance | String | 否 | 设备重要度 |
| repairStrategy | String | 否 | 维修策略 |
| overhaulTeam | String | 否 | 检修班组 |
| createdAt | String | 否 | 创建时间，推荐格式 `yyyy-MM-dd HH:mm:ss` |
| updatedAt | String | 否 | 更新时间，推荐格式 `yyyy-MM-dd HH:mm:ss` |

### 请求示例

```json
{
  "id": 1,
  "unitCode": "DY2",
  "unitName": "上料搬运单元",
  "equipmentCode": "S201",
  "equipmentName": "搬运机械臂",
  "maintainerName": "天津职业师范大学",
  "equipmentCategory": "搬运设备",
  "equipmentImportance": "A类重要设备",
  "repairStrategy": "预防性维护",
  "overhaulTeam": "03-机械组",
  "updatedAt": "2026-04-23 11:00:00"
}
```

### 返回示例

```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

---

## 5. 删除设备

### 接口地址

`DELETE /equipment/{id}`

### 接口说明

根据主键 ID 删除设备记录。

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | Long | 是 | 设备主键 ID |

### 请求示例

```http
DELETE /equipment/1
```

### 返回示例

```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

---

## 备注

1. `createdAt` 和 `updatedAt` 支持传完整时间，如 `2026-04-23 10:00:00`。
2. 如果只传日期，如 `2026-04-23`，后端会自动补齐为 `2026-04-23 00:00:00`。
3. 新增设备时，`unitCode` 需要在 `device_unit` 表中存在，否则可能触发外键约束异常。
4. `equipmentCode` 在数据库中是唯一键，新增重复值会失败。
