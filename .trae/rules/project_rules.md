# RuoYi-Vue-Plus 项目开发规范

## 一、项目概述

### 1.1 项目简介

RuoYi-Vue-Plus 是基于 RuoYi-Vue 的升级版本，采用 Spring Boot 3.x + Java 21 + Sa-Token + MyBatis-Plus 等技术栈构建的企业级后台管理系统。

### 1.2 技术架构

- **后端框架**: Spring Boot 3.4.7
- **Java版本**: Java 21
- **权限认证**: Sa-Token 1.44.0
- **ORM框架**: MyBatis-Plus 3.5.12
- **数据库**: MySQL (支持多数据源)
- **缓存**: Redis + Redisson 3.50.0
- **工具库**: Hutool 5.8.38
- **文档**: SpringDoc 2.8.8
- **工作流**: Warm-Flow 1.7.4

## 二、项目结构规范

### 2.1 模块组织

```
ruoyi-vue-qkd90/
├── ruoyi-admin/           # 主启动模块
├── ruoyi-common/          # 通用模块集合
│   ├── ruoyi-common-core/      # 核心工具类
│   ├── ruoyi-common-web/       # Web相关
│   ├── ruoyi-common-mybatis/   # MyBatis增强
│   ├── ruoyi-common-redis/     # Redis配置
│   ├── ruoyi-common-satoken/   # Sa-Token配置
│   ├── ruoyi-common-log/       # 日志处理
│   ├── ruoyi-common-excel/     # Excel处理
│   ├── ruoyi-common-oss/       # 对象存储
│   └── ...                     # 其他通用模块
├── ruoyi-modules/         # 业务模块
│   ├── ruoyi-system/           # 系统管理模块
│   ├── ruoyi-generator/        # 代码生成模块
│   └── ruoyi-demo/             # 演示模块
├── ruoyi-extend/          # 扩展模块
└── script/                # 脚本文件
```

### 2.2 包结构规范

```
org.dromara.{module}/
├── controller/            # 控制器层
├── service/              # 服务接口层
│   └── impl/             # 服务实现层
├── mapper/               # 数据访问层
├── domain/               # 实体类
│   ├── bo/               # 业务对象 (Business Object)
│   ├── vo/               # 视图对象 (View Object)
│   └── dto/              # 数据传输对象 (Data Transfer Object)
├── config/               # 配置类
├── enums/                # 枚举类
└── utils/                # 工具类
```

## 三、编码规范

### 3.1 分层架构规范

#### Controller层

```java
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
  
    private final ISysUserService userService;
  
    /**
     * 查询用户列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public TableDataInfo<SysUserVo> list(SysUserBo user, PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }
  
    /**
     * 新增用户
     */
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysUserBo user) {
        return toAjax(userService.insertUser(user));
    }
}
```

**Controller层规范要点:**

- 继承 `BaseController` 获得通用方法
- 使用 `@RequiredArgsConstructor` 进行依赖注入
- 使用 `@SaCheckPermission` 进行权限控制
- 使用 `@Log` 记录操作日志
- 使用 `@RepeatSubmit` 防止重复提交
- 使用 `@Validated` 进行参数校验

#### Service层

```java
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService {
  
    private final SysUserMapper baseMapper;
  
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUserBo bo) {
        SysUser user = MapstructUtils.convert(bo, SysUser.class);
        validEntityBeforeSave(user);
        return baseMapper.insert(user) > 0;
    }
  
    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysUser entity) {
        // 数据校验逻辑
    }
}
```

**Service层规范要点:**

- 接口与实现分离
- 使用 `@Transactional` 进行事务管理
- 使用 `MapstructUtils` 进行对象转换
- 私有方法进行数据校验

#### Mapper层

```java
@Mapper
public interface SysUserMapper extends BaseMapperPlus<SysUser, SysUserVo> {
  
    /**
     * 根据条件分页查询用户列表
     */
    Page<SysUserVo> selectPageUserList(@Param("page") Page<SysUser> page, 
                                       @Param("queryWrapper") Wrapper<SysUser> queryWrapper);
}
```

### 3.2 实体类设计规范

#### Entity (实体类)

```java
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
  
    @TableId(value = "user_id")
    private Long userId;
  
    @TableField("user_name")
    private String userName;
  
    @TableField("nick_name") 
    private String nickName;
}
```

#### BO (业务对象)

```java
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysUser.class, reverseConvertGenerate = false)
public class SysUserBo extends BaseEntity {
  
    @NotNull(message = "用户ID不能为空", groups = {EditGroup.class})
    private Long userId;
  
    @NotBlank(message = "用户账号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 0, max = 30, message = "用户账号长度不能超过{max}个字符")
    private String userName;
}
```

#### VO (视图对象)

```java
@Data
@AutoMapper(target = SysUser.class)
public class SysUserVo implements Serializable {
  
    private Long userId;
    private String userName;
    private String nickName;
  
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
    private String deptName;
}
```

### 3.3 数据传输对象规范

- **Entity**: 数据库表映射对象，仅用于数据库交互
- **BO**: 业务对象，用于接收前端参数，包含校验注解
- **VO**: 视图对象，用于返回给前端，可包含关联数据
- **DTO**: 数据传输对象，用于系统间数据传输

## 四、数据库规范

### 4.1 多数据源配置

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: true
      datasource:
        master:
          type: com.zaxxer.hikari.HikariDataSource
          driverClassName: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/ry-vue
          username: root
          password: password
```

### 4.2 MyBatis-Plus配置

- 使用 `BaseMapperPlus` 扩展基础CRUD功能
- 支持自动VO转换: `selectVoById()`, `selectVoList()`
- 支持分页查询: `selectVoPage()`

## 五、安全规范

### 5.1 权限控制

```java
// 方法级权限控制
@SaCheckPermission("system:user:list")

// 角色权限控制  
@SaCheckRole("admin")

// 登录校验
@SaCheckLogin
```

### 5.2 数据校验

```java
// 参数校验
@Validated(AddGroup.class)
@RequestBody SysUserBo user

// 字段校验
@NotBlank(message = "用户名不能为空")
@Size(min = 0, max = 30, message = "用户名长度不能超过{max}个字符")
private String userName;
```

## 六、日志规范

### 6.1 操作日志

```java
@Log(title = "用户管理", businessType = BusinessType.INSERT)
public R<Void> add(@RequestBody SysUserBo user) {
    // 业务逻辑
}
```

### 6.2 系统日志

```java
@Slf4j
public class UserService {
  
    public void processUser() {
        log.info("开始处理用户数据");
        log.error("处理用户数据失败", e);
    }
}
```

## 七、缓存规范

### 7.1 Redis缓存

```java
// 使用Spring Cache注解
@Cacheable(cacheNames = "user", key = "#userId")
public SysUserVo getUserById(Long userId) {
    return baseMapper.selectVoById(userId);
}

@CacheEvict(cacheNames = "user", key = "#userId")
public void deleteUser(Long userId) {
    baseMapper.deleteById(userId);
}
```

### 7.2 Redisson分布式锁

```java
@Lock(name = "user:#{#userId}", keys = {"#userId"})
public void updateUser(Long userId, SysUserBo user) {
    // 业务逻辑
}
```

## 八、异常处理规范

### 8.1 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
  
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        return R.fail(e.getMessage());
    }
}
```

### 8.2 统一响应格式

```java
// 成功响应
R.ok(data)
R.ok("操作成功", data)

// 失败响应  
R.fail("操作失败")
R.fail(500, "系统异常")
```

## 九、代码生成规范

### 9.1 代码生成器使用

- 支持单表、主子表、树表代码生成
- 自动生成Controller、Service、Mapper、Entity、BO、VO
- 支持前后端代码同时生成

### 9.2 生成代码规范

- 遵循项目统一的包结构
- 自动添加权限注解和日志注解
- 支持数据权限和字典翻译

## 十、部署规范

### 10.1 环境配置

```yaml
# 开发环境
spring:
  profiles:
    active: dev

# 生产环境  
spring:
  profiles:
    active: prod
```

### 10.2 Docker部署

```dockerfile
FROM openjdk:21-jre-slim
COPY ruoyi-admin.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 十一、性能优化规范

### 11.1 数据库优化

- 使用连接池 HikariCP
- 启用批处理优化 `rewriteBatchedStatements=true`
- 合理使用索引和分页查询

### 11.2 缓存优化

- 合理设置缓存过期时间
- 避免缓存穿透和雪崩
- 使用分布式锁防止缓存击穿

### 11.3 代码优化

- 使用 `@Lazy` 延迟加载
- 合理使用异步处理
- 避免N+1查询问题

## 十二、测试规范

### 12.1 单元测试

```java
@SpringBootTest
class UserServiceTest {
  
    @Autowired
    private ISysUserService userService;
  
    @Test
    void testGetUser() {
        SysUserVo user = userService.selectUserById(1L);
        assertNotNull(user);
    }
}
```

### 12.2 集成测试

- 使用 `@Transactional` 保证测试数据回滚
- 模拟真实环境进行接口测试
- 覆盖主要业务流程

---

**注意事项:**

1. 严格遵循分层架构，禁止跨层调用
2. 统一使用项目提供的工具类和基础类
3. 注重代码注释和文档编写
4. 遵循阿里巴巴Java开发手册规范
5. 定期进行代码审查和重构优化
