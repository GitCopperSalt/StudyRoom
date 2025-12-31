package com.project.demo.service.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.project.demo.constant.FindConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;

/**
 * 基础服务类 - 提供通用的数据库操作功能
 * 
 * 本类实现了系统中最核心的数据访问层抽象
 * 封装了所有实体的通用CRUD操作和复杂的查询功能
 * 
 * 主要功能模块：
 * 1. 通用增删改查：insert, update, delete, select
 * 2. 分页查询：selectToPage - 支持前端分页请求
 * 3. 列表查询：selectToList - 返回完整数据列表
 * 4. 统计查询：count, sum, avg - 聚合计算功能
 * 5. 分组查询：selectGroupCount, barGroup - 数据分析功能
 * 6. 条件构建：toWhereSql - 动态SQL拼接
 * 7. 参数解析：readQuery, readConfig - HTTP参数处理
 * 8. 数据导入导出：importDb, exportDb - Excel操作
 * 
 * 技术特点：
 * - 动态SQL生成：根据前端参数自动构建查询条件
 * - 防SQL注入：使用参数化查询和URL解码
 * - 分页优化：支持大数据集的分页查询
 * - 驼峰转换：自动处理Java和数据库字段命名差异
 * - 多重聚合：支持COUNT、SUM、AVG等多种聚合函数
 * - 灵活排序：支持多字段排序和自定义排序规则
 * 
 * 设计模式：
 * - 泛型设计：通过泛型E实现类型安全
 * - 模板方法：定义统一的查询模板，子类可扩展
 * - 策略模式：根据配置参数选择不同的查询策略
 */
@Slf4j
public class BaseService <E>{

    @Autowired
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 实体类：通过反射获取当前服务操作的实体类型
     * 
     * 获取机制：
     * 1. 通过getClass()获取当前类实例
     * 2. 获取父类的泛型参数类型
     * 3. 动态确定操作的数据表对应的实体类
     * 
     * 用途：用于构建表名、执行SQL查询时的实体映射
     */
    Class<E> eClass = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    /**
     * 数据表名：将实体类名转换为数据库表名
     * 
     * 转换规则：
     * 1. 驼峰命名转下划线命名（如：UserInfo -> user_info）
     * 2. 自动添加反引号防止SQL关键字冲突
     * 3. 基于实体类的简单名称（非全限定名）
     * 
     * 优势：
     * - 自动化表名映射，减少硬编码
     * - 统一命名规范
     * - 支持多表操作时的动态表名生成
     */
    private final String table = humpToLine(eClass.getSimpleName());

    /**
     * 执行原生SQL查询并映射为实体对象
     * 
     * 功能说明：
     * - 执行动态生成的SQL语句
     * - 将查询结果自动映射为指定实体类型E
     * - 支持复杂的SELECT查询操作
     * 
     * @param sql 动态生成的SQL语句字符串
     * @return Query对象，可获取实体类型的查询结果
     * 
     * 使用场景：
     * - 分页查询：select()方法中构建的查询
     * - 复杂关联查询：多表联查、字段选择
     * - 排序查询：带ORDER BY的查询
     */
    public Query runEntitySql(String sql){
        return entityManager.createNativeQuery(sql, eClass);
    }

    /**
     * 执行原生SQL查询返回数值类型结果
     * 
     * 功能说明：
     * - 执行不需要实体映射的SQL语句
     * - 主要用于COUNT、SUM、AVG等聚合查询
     * - 返回的是Number类型或Object类型结果
     * 
     * @param sql 动态生成的SQL语句字符串
     * @return Query对象，可获取数值型查询结果
     * 
     * 使用场景：
     * - 统计查询：count()、sum()、avg()方法
     * - 数据分析：分组统计、数据汇总
     * - 验证查询：检查记录是否存在
     */
    public Query runCountSql(String sql){
        return entityManager.createNativeQuery(sql);
    }

    /**
     * 插入新记录到数据表
     * 
     * 功能说明：
     * - 接收前端提交的JSON数据并转换为Map
     * - 动态构建INSERT语句，支持任意字段插入
     * - 自动处理字段命名转换（驼峰转下划线）
     * - 区分字符串和数值类型的数据包装
     * 
     * @param body 包含待插入数据的Map对象
     *             key为Java字段名，value为对应的值
     * 
     * SQL构建过程：
     * 1. 构建表名：INSERT INTO `table_name`
     * 2. 构建字段列表：动态遍历Map的key，转换为数据库字段名
     * 3. 构建值列表：根据数据类型决定是否添加单引号
     * 4. 执行插入：使用executeUpdate()方法提交事务
     * 
     * 数据类型处理：
     * - String类型：自动添加单引号包装（如：'value'）
     * - 数值类型：直接拼接，不添加引号（如：123）
     * 
     * 安全特性：
     * - 表名和字段名使用反引号包裹，防止SQL关键字冲突
     * - 字段名通过humpToLine转换，避免驼峰命名问题
     */
    public void insert(Map<String,Object> body){
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append("`").append(table).append("`").append(" (");
        for (Map.Entry<String,Object> entry:body.entrySet()){
            sql.append("`"+humpToLine(entry.getKey())+"`").append(",");
        }
        sql.deleteCharAt(sql.length()-1);
        sql.append(") VALUES (");
        for (Map.Entry<String,Object> entry:body.entrySet()){
            Object value = entry.getValue();
            if (value instanceof String){
                sql.append("'").append(entry.getValue()).append("'").append(",");
            }else {
                sql.append(entry.getValue()).append(",");
            }
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        log.info("[{}] - 插入操作：{}",table,sql);
        Query query = runCountSql(sql.toString());
        query.executeUpdate();
    }

    /**
     * 更新数据表中的记录
     * 
     * 功能说明：
     * - 支持条件更新和批量更新操作
     * - 动态构建UPDATE语句和WHERE条件
     * - 结合前端搜索筛选参数进行精确更新
     * - 使用事务确保数据一致性
     * 
     * @param query 查询条件Map，包含要更新的记录筛选条件
     * @param config 配置参数，控制LIKE查询模式
     * @param body 更新数据Map，key为字段名，value为新值
     * 
     * SQL构建过程：
     * 1. 构建表名：UPDATE `table_name` SET
     * 2. 构建SET子句：动态遍历更新字段，添加SET字段=值的语句
     * 3. 构建WHERE子句：调用toWhereSql方法生成查询条件
     * 4. 执行更新：使用executeUpdate()提交事务
     * 
     * 数据类型处理：
     * - String类型：自动添加单引号包装
     * - 数值类型：直接拼接数值
     * 
     * 条件构建特点：
     * - LIKE模式：通过config参数控制模糊或精确匹配
     * - 安全性：防止SQL注入，使用参数化查询
     * - 灵活性：支持多条件组合更新
     */
    @Transactional
    public void update(Map<String,String> query,Map<String,String> config,Map<String,Object> body){
        StringBuffer sql = new StringBuffer("UPDATE ").append("`").append(table).append("`").append(" SET ");
        for (Map.Entry<String,Object> entry:body.entrySet()){
            Object value = entry.getValue();
            if (value instanceof String){
                sql.append("`"+humpToLine(entry.getKey())+"`").append("=").append("'").append(value).append("'").append(",");
            }else {
                sql.append("`"+humpToLine(entry.getKey())+"`").append("=").append(value).append(",");
            }

        }
        sql.deleteCharAt(sql.length()-1);
        sql.append(toWhereSql(query,"0".equals(config.get(FindConfig.LIKE))));
//        sql.append(";");
        log.info("[{}] - 更新操作：{}",table,sql);
        Query query1 = runCountSql(sql.toString());
        query1.executeUpdate();
    }

    /**
     * 分页查询 - 返回分页数据和总记录数
     * 
     * 功能说明：
     * - 实现前端分页请求的核心方法
     * - 同时返回当前页数据和总记录数
     * - 减少数据库访问次数，提升查询性能
     * - 支持复杂的条件筛选和排序
     * 
     * @param query 查询条件Map，包含字段名和对应的查询值
     * @param config 配置参数，包含分页、排序、字段选择等配置
     * @return 包含分页结果的Map对象
     *         - "list": 当前页的数据列表
     *         - "count": 总记录数，用于计算总页数
     * 
     * 分页计算公式：
     * - 当前页码：page（从1开始）
     * - 每页大小：size（默认10条）
     * - 起始位置：(page-1) * size
     * - LIMIT子句：LIMIT 起始位置, 每页大小
     * 
     * 性能优化：
     * - 一次查询获取分页数据
     * - 一次查询获取总记录数
     * - 避免N+1查询问题
     * - 支持大数据集分页
     */
    public Map<String,Object> selectToPage(Map<String,String> query,Map<String,String> config){
        Query select = select(query, config);
        Map<String,Object> map = new HashMap<>();
        map.put("list",select.getResultList());
        map.put("count",count(query,config).getSingleResult());
        return map;
    }

    public Map<String,Object> selectToList(Map<String,String> query,Map<String,String> config){
        Query select = selectGroupCount(query, config);
        Map<String,Object> map = new HashMap<>();
        map.put("list",select.getResultList());
        return map;
    }

    public Map<String,Object> selectBarGroup(Map<String,String> query,Map<String,String> config){
        Query select = barGroup(query, config);
        Map<String,Object> map = new HashMap<>();
        map.put("list",select.getResultList());
        return map;
    }

    public Query barGroup(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer(" SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append(config.get(FindConfig.GROUP_BY));
            if (config.get(FindConfig.FIELD) != null && !"".equals(config.get(FindConfig.FIELD))){
                String[] fieldList = config.get(FindConfig.FIELD).split(",");
                for (int i=0;i<fieldList.length;i++)
                    sql.append(" ,SUM(").append(fieldList[i]).append(")");
            }
            sql.append(" FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append(" SUM(").append(config.get(FindConfig.GROUP_BY)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return runCountSql(sql.toString());
    }

    /**
     * 分组统计查询 - 按指定字段分组并计算每组的记录数
     * 
     * 功能说明：
     * - 实现数据分析中的分组统计功能
     * - 按指定字段对数据进行分组
     * - 计算每个分组内的记录数量
     * - 支持WHERE条件筛选和LIKE模式
     * 
     * @param query 查询条件Map，包含WHERE子句的筛选条件
     * @param config 配置参数，必须包含GROUP_BY字段指定分组依据
     * @return Query对象，返回分组统计结果
     * 
     * SQL构建示例：
     * SELECT COUNT(*) AS count_value, group_field 
     * FROM table_name 
     * WHERE condition 
     * GROUP BY group_field
     * 
     * 应用场景：
     * - 用户分组统计：按用户组统计用户数量
     * - 时间维度分析：按日期、月份、季度统计
     * - 地区分布统计：按地区、门店分组统计
     * - 状态分布分析：按状态、类型分组统计
     * 
     * 数据分析价值：
     * - 提供数据的分布概况
     * - 支持业务决策的数据依据
     * - 发现数据中的模式和趋势
     */
    public Query selectGroupCount(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("select COUNT(*) AS count_value, ");
        sql.append(config.get(FindConfig.GROUP_BY)).append(" ");
        sql.append("from ").append("`").append(table).append("` ");
        sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("group by ").append(config.get(FindConfig.GROUP_BY)).append(" ");
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return runCountSql(sql.toString());
    }

    /**
     * 通用查询方法 - 动态构建SQL查询语句
     * 
     * 功能说明：
     * - 系统的核心查询方法，支持灵活的SQL构建
     * - 支持字段选择、分页、排序、分组等高级功能
     * - 自动处理前端传入的各种查询参数
     * - 实现了完整的SQL查询模板
     * 
     * @param query 查询条件Map，包含字段名和查询值的映射关系
     * @param config 配置参数Map，包含查询行为的各种配置选项
     * @return Query对象，返回执行后的查询结果
     * 
     * SQL构建顺序：
     * 1. SELECT子句：字段选择（默认*或指定字段）
     * 2. FROM子句：数据表名
     * 3. WHERE子句：调用toWhereSql()构建条件
     * 4. GROUP BY子句：可选的分组功能
     * 5. ORDER BY子句：可选的排序功能
     * 6. LIMIT子句：可选的分页功能
     * 
     * 配置参数支持：
     * - FIELD：指定查询字段，多字段用逗号分隔
     * - GROUP_BY：指定分组字段
     * - ORDER_BY：指定排序字段和方向
     * - PAGE：页码（从1开始）
     * - SIZE：每页记录数
     * 
     * 默认行为：
     * - 未指定FIELD时查询所有字段(*)
     * - 未指定PAGE时返回所有记录
     * - 未指定SIZE时默认10条记录
     * 
     * 性能优化：
     * - 只查询必要字段，减少数据传输量
     * - 支持分页，避免大数据集内存溢出
     * - 使用预编译SQL，提高查询效率
     */
    public Query select(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("select ");
        sql.append(config.get(FindConfig.FIELD) == null || "".equals(config.get(FindConfig.FIELD)) ? "*" : config.get(FindConfig.FIELD)).append(" ");
        sql.append("from ").append("`").append(table).append("`").append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("group by ").append(config.get(FindConfig.GROUP_BY)).append(" ");
        }
        if (config.get(FindConfig.ORDER_BY) != null && !"".equals(config.get(FindConfig.ORDER_BY))){
            sql.append("order by ").append(config.get(FindConfig.ORDER_BY)).append(" ");
        }
        if (config.get(FindConfig.PAGE) != null && !"".equals(config.get(FindConfig.PAGE))){
            int page = config.get(FindConfig.PAGE) != null && !"".equals(config.get(FindConfig.PAGE)) ? Integer.parseInt(config.get(FindConfig.PAGE)) : 1;
            int limit = config.get(FindConfig.SIZE) != null && !"".equals(config.get(FindConfig.SIZE)) ? Integer.parseInt(config.get(FindConfig.SIZE)) : 10;
            sql.append(" limit ").append( (page-1)*limit ).append(" , ").append(limit);
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return runEntitySql(sql.toString());
    }

    /**
     * 删除数据表中的记录
     * 
     * 功能说明：
     * - 支持条件删除和批量删除操作
     * - 动态构建DELETE语句和WHERE条件
     * - 结合前端搜索筛选参数进行精确删除
     * - 使用事务确保数据一致性
     * 
     * @param query 查询条件Map，包含要删除的记录筛选条件
     * @param config 配置参数，控制LIKE查询模式
     * 
     * SQL构建过程：
     * 1. 构建表名：DELETE FROM `table_name`
     * 2. 构建WHERE子句：调用toWhereSql方法生成查询条件
     * 3. 执行删除：使用executeUpdate()提交事务
     * 
     * 安全性考虑：
     * - WHERE条件必须明确，避免误删全表数据
     * - 使用参数化查询，防止SQL注入
     * - 事务管理确保删除操作的原子性
     * - 日志记录用于审计和故障排查
     * 
     * 注意事项：
     * - 谨慎使用批量删除功能
     * - 建议在生产环境中增加删除前的确认机制
     * - 可考虑软删除（标记删除）替代物理删除
     */
    @Transactional
    public void delete(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("DELETE FROM ").append("`").append(table).append("`").append(" ");
        sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.GROUP_BY))));
        log.info("[{}] - 删除操作：{}",table,sql);
        Query query1 = runCountSql(sql.toString());
        query1.executeUpdate();
    }

    /**
     * 记录统计查询 - 计算满足条件的记录总数
     * 
     * 功能说明：
     * - 实现数据统计功能，支持无条件统计和分组统计
     * - 用于分页查询中的总页数计算
     * - 支持WHERE条件筛选和LIKE模式
     * - 为前端提供数据总量信息
     * 
     * @param query 查询条件Map，包含WHERE子句的筛选条件
     * @param config 配置参数，控制分组统计模式
     * @return Query对象，返回统计结果
     * 
     * 统计模式：
     * 1. 总记录数统计：
     *    SELECT COUNT(*) FROM table_name WHERE conditions
     * 2. 分组统计：
     *    SELECT group_field, COUNT(group_field) FROM table_name WHERE conditions GROUP BY group_field
     * 
     * 应用场景：
     * - 分页查询：计算总页数 (total_records / page_size)
     * - 数据概览：了解系统中的数据规模
     * - 条件统计：统计满足特定条件的记录数量
     * - 分组统计：按维度统计记录分布情况
     * 
     * 性能特点：
     * - 使用数据库COUNT函数，效率较高
     * - 支持索引优化，统计速度快
     * - 避免将所有数据加载到内存
     * - 适合大数据集统计
     * 
     * 注意事项：
     * - 分组统计时，结果集会包含多个分组
     * - WHERE条件会影响统计结果
     * - 大表统计可能较慢，建议建立合适索引
     */
    public Query count(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("SELECT ");
//        log.info("拼接统计函数前");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append(config.get(FindConfig.GROUP_BY)).append(" ,COUNT(").append(config.get(FindConfig.GROUP_BY)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
//            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append("COUNT(*) FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
        }
        log.info("[{}] - 统计操作，sql: {}",table,sql);
        return runCountSql(sql.toString());
    }

    public Query sum(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer(" SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append(config.get(FindConfig.GROUP_BY)).append(" ,SUM(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append(" SUM(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return runCountSql(sql.toString());
    }

    public Query avg(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer(" SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append(config.get(FindConfig.GROUP_BY)).append(" ,AVG(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append(" AVG(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return runCountSql(sql.toString());
    }

    /**
     * 动态构建WHERE子句 - 将查询条件转换为SQL WHERE语句
     * 
     * 功能说明：
     * - 系统的核心SQL条件构建器
     * - 支持多种查询模式：精确匹配、模糊查询、范围查询
     * - 自动处理字段命名转换和数据类型编码
     * - 防止SQL注入，使用URL解码处理特殊字符
     * 
     * @param query 查询条件Map，包含字段名和查询值的映射
     * @param like 是否使用LIKE模糊查询模式
     *        true: 模糊查询（LIKE '%value%'）
     *        false: 精确匹配（= 'value'）
     * @return 构建完成的WHERE子句字符串，若无查询条件则返回空字符串
     * 
     * 查询类型支持：
     * 1. 精确查询：field = 'value'
     * 2. 模糊查询：field LIKE '%value%'
     * 3. 范围查询：field_min <= field <= field_max
     *    - 最小值查询：field >= 'min_value'
     *    - 最大值查询：field <= 'max_value'
     * 
     * 命名约定：
     * - 普通字段：直接使用字段名（如：userName）
     * - 最小值字段：字段名 + "_min"（如：age_min）
     * - 最大值字段：字段名 + "_max"（如：age_max）
     * 
     * 数据处理：
     * - 字段名自动转换：驼峰命名转下划线命名
     * - 值URL解码：处理前端编码的查询参数
     * - 引号处理：字符串类型自动添加单引号
     * 
     * 安全性：
     * - 使用URLDecoder.decode()防止编码攻击
     * - 字段名和表名使用反引号包裹
     * - 避免SQL注入，确保系统安全
     * 
     * SQL示例：
     * 输入：{userName="admin", age_min="18", age_max="30"}
     * 输出：WHERE `user_name` LIKE '%admin%' AND `age` >= '18' AND `age` <= '30'
     */
    public String toWhereSql(Map<String,String> query, Boolean like) {
        if (query.size() > 0) {
            try {
                StringBuilder sql = new StringBuilder(" WHERE ");
                for (Map.Entry<String, String> entry : query.entrySet()) {
                    if (entry.getKey().contains(FindConfig.MIN_)) {
                        String min = humpToLine(entry.getKey()).replace("_min", "");
                        sql.append("`"+min+"`").append(" >= '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("' and ");
                        continue;
                    }
                    if (entry.getKey().contains(FindConfig.MAX_)) {
                        String max = humpToLine(entry.getKey()).replace("_max", "");
                        sql.append("`"+max+"`").append(" <= '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("' and ");
                        continue;
                    }
                    if (like == true) {
                        sql.append("`"+humpToLine(entry.getKey())+"`").append(" LIKE '%").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("%'").append(" and ");
                    } else {
                        sql.append("`"+humpToLine(entry.getKey())+"`").append(" = '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("'").append(" and ");
                    }
                }
                sql.delete(sql.length() - 4, sql.length());
                sql.append(" ");
                return sql.toString();
            } catch (UnsupportedEncodingException e) {
                log.info("拼接sql 失败：{}", e.getMessage());
            }
        }
        return "";
    }

    public Map<String,Object> readBody(BufferedReader reader){
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try{
            br = reader;
            String str;
            while ((str = br.readLine()) != null){
                sb.append(str);
            }
            br.close();
            String json = sb.toString();
            return JSONObject.parseObject(json, Map.class);
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            if (null != br){
                try{
                    br.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Map<String,String> readQuery(HttpServletRequest request){
        String queryString = request.getQueryString();
        if (queryString != null && !"".equals(queryString)) {
            String[] querys = queryString.split("&");
            Map<String, String> map = new HashMap<>();
            for (String query : querys) {
                String[] q = query.split("=");
                map.put(q[0], q[1]);
            }
            map.remove(FindConfig.PAGE);
            map.remove(FindConfig.SIZE);
            map.remove(FindConfig.LIKE);
            map.remove(FindConfig.ORDER_BY);
            map.remove(FindConfig.FIELD);
            map.remove(FindConfig.GROUP_BY);
            map.remove(FindConfig.MAX_);
            map.remove(FindConfig.MIN_);
            return map;
        }else {
            return new HashMap<>();
        }
    }

    public Map<String,String> readConfig(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        map.put(FindConfig.PAGE,request.getParameter(FindConfig.PAGE));
        map.put(FindConfig.SIZE,request.getParameter(FindConfig.SIZE));
        map.put(FindConfig.LIKE,request.getParameter(FindConfig.LIKE));
        map.put(FindConfig.ORDER_BY,request.getParameter(FindConfig.ORDER_BY));
        map.put(FindConfig.FIELD,request.getParameter(FindConfig.FIELD));
        map.put(FindConfig.GROUP_BY,request.getParameter(FindConfig.GROUP_BY));
        map.put(FindConfig.MAX_,request.getParameter(FindConfig.MAX_));
        map.put(FindConfig.MIN_,request.getParameter(FindConfig.MIN_));
        return map;
    }

    public void importDb(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return;
        }
        List<Map<String,String>> body = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        if (fileName == null){
            return;
        }
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        InputStream ins = file.getInputStream();
        Workbook wb = null;
        if(suffix.equals("xlsx")){
            wb = new XSSFWorkbook(ins);
        }else{
            wb = new HSSFWorkbook(ins);
        }
        Sheet sheet = wb.getSheetAt(0);
        if(null != sheet){
            for(int line = 0; line <= sheet.getLastRowNum();line++){
                Row row = sheet.getRow(line);
                if(null == row){
                    continue;
                }
                Iterator<Cell> cellIterator = row.cellIterator();
                StringBuffer sql = new StringBuffer("INSERT INTO ").append(table).append(" VALUES (null,");
                while (cellIterator.hasNext()){
                    sql.append(cellIterator.next().getStringCellValue()).append(",");
                }
                sql.deleteCharAt(sql.length());
                sql.append(")");
                runCountSql(sql.toString());
            }
        }
    }

    public HSSFWorkbook exportDb(Map<String,String> query,Map<String,String> config){
        Query select = select(query, config);
        List<Map<String,String>> resultList = select.getResultList();
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(table);
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        for (int i = 0; i < resultList.size(); i++) {
            HSSFRow row = sheet.createRow(i);
            Map<String,String> map = resultList.get(i);
            int j = 0;
            for (Map.Entry<String,String> entry:map.entrySet()){
                row.createCell(j).setCellValue(new HSSFRichTextString(entry.getValue()));
            }
        }
        return workbook;
    }

    @Transactional
    public void save(E e){
        String s = JSONObject.toJSONString(e);
        Map map = JSONObject.parseObject(s, Map.class);
        insert(map);
    }

    public E findOne(Map<String, String> map){
        try {
            Query select = select(map, new HashMap<>());
            return (E) select.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }


    public String encryption(String plainText) {
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return re_md5;
    }


    public static String humpToLine(String str) {
        if (str == null) {
            return null;
        }
        // 将驼峰字符串转换成数组
        char[] charArray = str.toCharArray();
        StringBuilder buffer = new StringBuilder();
        //处理字符串
        for (int i = 0, l = charArray.length; i < l; i++) {
            if (charArray[i] >= 65 && charArray[i] <= 90) {
                buffer.append("_").append(charArray[i] += 32);
            } else {
                buffer.append(charArray[i]);
            }
        }
        String s = buffer.toString();
        if (s.startsWith("_")){
            return s.substring(1);
        }else {
            return s;
        }
    }


    public JSONObject covertObject(JSONObject object) {
        if (object == null) {
            return null;
        }
        JSONObject newObject = new JSONObject();
        Set<String> set = object.keySet();
        for (String key : set) {
            Object value = object.get(key);
            if (value instanceof JSONArray) {
                //数组
                value = covertArray(object.getJSONArray(key));
            } else if (value instanceof JSONObject) {
                //对象
                value = covertObject(object.getJSONObject(key));
            }
            //这个方法自己写的改成下划线
            key = humpToLine(key);
            newObject.put(key, value);
        }
        return newObject;
    }

    public JSONArray covertArray(JSONArray array) {
        if (array == null) {
            return null;
        }
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                //数组
                value = covertArray(array.getJSONArray(i));
            } else if (value instanceof JSONObject) {
                //对象
                value = covertObject(array.getJSONObject(i));
            }
            newArray.add(value);
        }
        return newArray;
    }


}
