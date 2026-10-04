# 后端开发规范

新代码一律按这份规范写。老模块（`common` / `pojo` / `web`）是迁移前的历史代码，
只在修复问题时才动，不要往里加新功能。

## 一、模块与依赖方向

```
product-common   通用基础设施：响应体、分页、异常、错误码、枚举、常量
product-domain   领域模型：实体 + Mapper，按领域分包
product-app      应用层：接口 / 业务编排 / 基础设施查询
```

依赖方向固定为 `product-app → product-domain → (无)`，`product-common` 被 app 依赖、
domain 不依赖任何业务模块。**不允许反向依赖，也不允许 common 依赖 domain。**

```
server/
├── pom.xml                 聚合 POM
├── product-common/
│   └── src/main/java/com/qingmang/common/
│       ├── api/            ApiResponse、PageResult
│       ├── constant/       BizConstants
│       ├── enums/          CodeEnum 及各业务枚举
│       └── exception/      BizException、ErrorCode、GlobalExceptionHandler
├── product-domain/
│   └── src/main/java/com/qingmang/domain/
│       ├── user/           entity + mapper
│       ├── video/
│       ├── interaction/
│       ├── favorite/
│       ├── social/
│       ├── watchroom/
│       └── ops/
└── product-app/
    ├── src/main/java/com/qingmang/
    │   ├── interfaces/     Controller + dto + vo
    │   ├── application/    业务编排 Service
    │   ├── infrastructure/ 跨表查询 Mapper + XML
    │   ├── config/         配置类
    │   └── support/        工具类
    └── src/main/resources/mapper/*.xml
```

## 二、分层职责

| 层 | 能做 | 不能做 |
|---|---|---|
| Controller | 收参、校验、调 Service、包响应体 | 写 SQL、拼业务逻辑、直接用 Mapper |
| Service | 业务规则、事务边界、编排多个 Mapper | 直接碰 HTTP（HttpServletRequest 之类） |
| Mapper(XML) | SQL | 业务判断 |
| Entity | 只映射一张表 | 放业务方法 |

**Controller 里不允许出现内嵌类**。请求体、响应体都是独立文件，放在 `interfaces/dto`
和 `interfaces/vo` 下，命名 `XxxRequest` / `XxxVO`。

## 三、接口约定

- 路径：`/api/{领域}/{资源}`，如 `/api/video/list`、`/api/auth/login`
- 动词：查询用 GET，动作（改变状态）用 POST/DELETE，不要用 GET 做写操作
- 响应体统一 `ApiResponse`，`code == 1` 成功
- 失败一律抛 `BizException(ErrorCode.XXX)`，不要在 Controller 里 `ApiResponse.fail(...)`
- 参数校验用 `@Valid` + DTO 上的注解，错误文案写在注解里

## 四、错误码

`ErrorCode` 里按段划分，新增只追加不复用：

| 段 | 含义 |
|---|---|
| 1xxxx | 通用 / 参数 |
| 2xxxx | 账号与权限 |
| 3xxxx | 内容（视频 / 评论 / 动态） |
| 4xxxx | 社交（关注 / 私信 / 一起看） |
| 5xxxx | 收藏与观看记录 |
| 9xxxx | 服务端 |

## 五、命名约定

| 对象 | 规则 | 例 |
|---|---|---|
| 表 | 单数名词 + 下划线，带归属的一律带前缀 | `video_stats`、`user_follow` |
| 主键 / 外键 | `<实体>_id` | `owner_id`、`folder_id` |
| 时间 | `created_at` / `updated_at`，业务时间点用业务名 | `published_at`、`last_login_at` |
| 布尔 | `is_xxx`，`TINYINT(1)` 0/1 | `is_default` |
| 软删除 | `deleted_at DATETIME NULL`，NULL 表示未删除 | |
| 状态枚举 | 数字编码 + Java 枚举 | `VideoStatus.PUBLISHED` |
| DTO | `XxxRequest` / `XxxDTO` | `CoinRequest` |
| VO | `XxxVO` | `VideoListItemVO` |

**禁止中文魔法值**。以前散落着 `"全部"`、`"发布时间排序"`、`"稍后再看"` 这类字符串，
前后端各写一遍，改一次漏一处就出线上故障。要存进数据库的关联一律用外键。

## 六、SQL 与性能

这几条是硬性要求：

1. **列表接口不允许 N+1**。需要「视频 + 作者 + 计数 + 当前用户状态」就一次 join 出来；
   一页数据里的批量关联（比如某条评论下的所有回复）用 `IN` 一次取回，不要逐条查。
2. **计数列一律用数据库自增**。`UPDATE ... SET like_count = like_count + 1`，
   不要「先 select 出计数 → Java 加一 → update 回去」，并发会互相覆盖。
3. **余额扣减把判断和扣减放在同一条 SQL**：
   `UPDATE user SET coin_balance = coin_balance - ? WHERE id = ? AND coin_balance >= ?`，
   再看影响行数。不要先查余额再扣。
4. **深分页用游标**，参数是 `cursorId`（上一页最后一条的 id），不要用大 offset。
5. **单表 CRUD 走 `BaseMapper`**；连表或批量查询写在 XML 里，
   不要为了一个统计查询开 `@Select` 注解把 SQL 散在 Java 代码中。
6. **XML 里的 `<sql>` 片段如果含 JOIN，必须把 ON 条件通过 `<property>` 传进来**。
   漏掉 ON 条件会直接变成笛卡尔积，而且不报错，只是结果变多——这类 bug 最难查。

## 七、缓存

- 读多写少、允许短暂不一致的才缓存（分类、系统配置、搜索热榜）
- `product-app/src/main/java/com/qingmang/config/CacheConfig.java` 统一配 TTL 和序列化
- **`@Cacheable` / `@CacheEvict` 靠 Spring 代理生效**：
  同类内部 `this.xxx()` 调用会绕过代理导致注解失效。
  需要注解生效的逻辑必须放在独立 bean 里，由别的 bean 调用。

## 八、软删除

逻辑删除配置统一写在 `application.yml`：

```yaml
logic-delete-field: deletedAt
logic-delete-value: "now()"
logic-not-delete-value: "null"
```

**这两个值必须加引号**。写成裸 `null` 会被 YAML 解析成空值，
MyBatis-Plus 会拼出 `AND deleted_at=` 这种语法错误的 SQL。

`deleted_at` 是 `DATETIME`，删除标记用 `now()`，不要写死日期串。
另外 MyBatis-Plus 3.5.6 的 `@TableLogic` 没有 `deletion` 属性，逻辑删除值只能在 yml 里配。

## 九、注释

只写「为什么」，不写「是什么」。

```java
// 要：说明为什么这么做、不这么做会怎样
/** 业务异常。Service 层遇到可预期的失败抛这个，由 GlobalExceptionHandler 统一转换。 */

// 不要：把方法名和方法体翻译一遍
/** 增加投币数的方法 */
public void addCoin() {}
```

实体字段的注释不用写，数据库里已经有，直接对着 schema 看。

## 十、提交前检查

```bash
cd server
mvn -o clean install -DskipTests     # 必须干净通过
```

改了 XML 之后一定要 `clean`：MyBatis 的增量编译偶尔会把没变的类留下旧产物，
表现为「代码明明写了，运行时报找不到符号」。