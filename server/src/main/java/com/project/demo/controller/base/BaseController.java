package com.project.demo.controller.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.project.demo.service.base.BaseService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础控制器类 - 提供RESTful API接口
 * 
 * 本类实现了系统中最核心的Web层抽象
 * 封装了所有实体的通用CRUD操作接口
 * 提供统一的API响应格式和错误处理
 * 
 * 主要功能模块：
 * 1. 基础CRUD接口：add, set, del, obj
 * 2. 列表查询接口：getList（分页）, listGroup（分组）
 * 3. 统计分析接口：count, sum, avg, barGroup
 * 4. 文件操作接口：upload, import_db, export_db
 * 5. 统一响应格式：success, error方法
 * 
 * 设计特点：
 * - RESTful风格：遵循REST API设计规范
 * - 泛型设计：通过泛型E和S实现类型安全
 * - 统一响应：所有接口返回统一的数据结构
 * - 事务管理：使用@Transactional确保数据一致性
 * - 参数解析：自动解析HTTP请求参数和请求体
 * 
 * API接口规范：
 * - POST /add：添加新记录
 * - POST /set：更新记录
 * - GET/POST /del：删除记录
 * - GET /get_obj：获取单个对象
 * - GET /get_list：获取分页列表
 * - GET /list_group：获取分组列表
 * - GET /count_group：分组统计
 * - GET /sum_group：分组求和
 * - GET /avg_group：分组平均值
 * - POST /upload：文件上传
 * - POST /import_db：数据导入
 * - GET /export_db：数据导出
 * 
 * 响应格式：
 * - 成功：{"result": data}
 * - 失败：{"error": {"code": code, "message": message}}
 */
@Slf4j
public class BaseController<E, S extends BaseService<E>> {

    /**
     * 服务层实例：通过泛型S注入对应的服务类
     * 
     * 设计说明：
     * - 使用Lombok的Setter注解自动生成setter方法
     * - 泛型S必须是BaseService<E>的子类
     * - 在子类中通过依赖注入初始化此属性
     * 
     * 使用方式：
     * 子类Controller通过@Autowired注入时，Spring会自动调用setService方法
     * 将对应的服务实例设置到此属性中
     */
    @Setter
    protected S service;


    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(HttpServletRequest request) throws IOException {
        service.insert(service.readBody(request.getReader()));
        return success(1);
    }

    @Transactional
    public Map<String, Object> addMap(Map<String,Object> map){
        service.insert(map);
        return success(1);
    }

    @PostMapping("/set")
	@Transactional
    public Map<String, Object> set(HttpServletRequest request) throws IOException {
        service.update(service.readQuery(request), service.readConfig(request), service.readBody(request.getReader()));
        return success(1);
    }


    @RequestMapping(value = "/del")
    @Transactional
    public Map<String, Object> del(HttpServletRequest request) {
        service.delete(service.readQuery(request), service.readConfig(request));
        return success(1);
    }

    /**
     * 获取单个对象接口 - 根据查询条件获取唯一记录
     * 
     * 功能说明：
     * - 通过查询条件获取数据库中的单条记录
     * - 如果查询结果为空，返回null
     * - 如果查询结果有多条，只返回第一条记录
     * - 适用于详情查看、编辑表单数据加载等场景
     * 
     * @param request HTTP请求对象，包含查询参数
     * @return 统一响应格式，包含查询结果对象
     *         - 成功且有数据：{"result": {"obj": data}}
     *         - 成功但无数据：{"result": null}
     * 
     * 查询特点：
     * - 支持精确查询和模糊查询模式
     * - 支持字段选择（指定要查询的字段）
     * - 支持排序（用于确定返回哪条记录）
     * - 支持条件组合查询
     * 
     * 使用场景：
     * - 用户详情页面：GET /user/get_obj?id=123
     * - 编辑表单数据加载：GET /product/get_obj?code=ABC001
     * - 关联对象查询：GET /order/get_obj?orderNo=ORD20241231
     * 
     * 注意事项：
     * - 如果查询条件不唯一，可能返回意外的第一条记录
     * - 建议在查询条件中包含主键或唯一标识符
     * - 返回格式固定为obj包装，便于前端处理
     */
    @RequestMapping("/get_obj")
    public Map<String, Object> obj(HttpServletRequest request) {
        Query select = service.select(service.readQuery(request), service.readConfig(request));
        List resultList = select.getResultList();
        if (resultList.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("obj",resultList.get(0));
            return success(jsonObject);
        } else {
            return success(null);
        }
    }


    /**
     * 获取分页列表接口 - 支持分页、排序、筛选的列表查询
     * 
     * 功能说明：
     * - 提供分页查询功能，是系统中最常用的列表接口
     * - 同时返回当前页数据和总记录数
     * - 支持复杂的查询条件、排序和分页参数
     * - 适用于数据表格、列表页面等需要分页的场景
     * 
     * @param request HTTP请求对象，包含查询参数和分页配置
     * @return 统一响应格式，包含分页结果数据
     *         {"result": {"list": [...], "count": 总记录数}}
     * 
     * 支持的查询参数：
     * - page: 页码（从1开始，默认1）
     * - size: 每页大小（默认10）
     * - like: 模糊查询开关（1:开启，0:关闭）
     * - orderby: 排序字段和方向（如：id desc）
     * - field: 指定查询字段（逗号分隔）
     * - 自定义字段: 根据实际业务需求设置查询条件
     * 
     * 前端调用示例：
     * GET /user/get_list?page=1&size=10&userName=张三&like=1&orderby=create_time desc
     * 返回：{"result": {"list": [...], "count": 50}}
     * 
     * 应用场景：
     * - 用户管理列表：支持按用户名、状态等筛选
     * - 商品列表：支持按分类、价格区间筛选
     * - 订单列表：支持按状态、时间范围筛选
     * - 数据表格：所有需要分页展示的数据列表
     * 
     * 性能特点：
     * - 一次查询获取分页数据和总记录数
     * - 支持大数据集分页，避免内存溢出
     * - 数据库级分页，查询效率高
     * - 支持索引优化，提升查询速度
     */
    @RequestMapping("/get_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        Map<String, Object> map = service.selectToPage(service.readQuery(request), service.readConfig(request));
        return success(map);
    }

    /**
     * 获取分组列表接口 - 按指定字段分组并返回各组数据
     * 
     * 功能说明：
     * - 支持按指定字段对数据进行分组
     * - 返回每个分组内的完整记录列表
     * - 支持分组内的筛选、排序和分页功能
     * - 适用于需要按维度分析数据的场景
     * 
     * @param request HTTP请求对象，包含分组配置和查询条件
     * @return 统一响应格式，包含分组结果数据
     *         {"result": {"list": [...分组数据...]}}
     * 
     * 分组参数：
     * - groupby: 分组字段（必填）
     * - field: 指定要查询的字段（可选）
     * - 其他查询参数：支持WHERE条件筛选
     * 
     * 使用示例：
     * GET /user/list_group?groupby=user_group&status=1&orderby=create_time desc
     * 返回按用户组分组的用户列表
     * 
     * 应用场景：
     * - 用户分组管理：按用户组查看所有用户
     * - 地区销售分析：按地区分组查看订单
     * - 时间维度分析：按月份分组查看数据
     * - 状态分类查看：按状态分组查看记录
     * 
     * 数据特点：
     * - 分组内的记录保持原有完整性
     * - 支持分组级别的排序和筛选
     * - 可与其他查询条件组合使用
     * - 返回结果适合前端分组展示
     */
    @RequestMapping("/list_group")
    public Map<String, Object> listGroup(HttpServletRequest request) {
        Map<String, Object> map = service.selectToList(service.readQuery(request), service.readConfig(request));
        return success(map);
    }

    @RequestMapping("/bar_group")
    public Map<String, Object> barGroup(HttpServletRequest request) {
        Map<String, Object> map = service.selectBarGroup(service.readQuery(request), service.readConfig(request));
        return success(map);
    }

    /**
     * 统计查询接口 - 计算满足条件的记录数量
     * 
     * 功能说明：
     * - 提供COUNT统计功能，支持无条件统计和分组统计
     * - 用于获取数据的总量信息，支持复杂的查询条件
     * - 为前端分页组件提供总记录数，支持数据分析
     * 
     * 支持的路径：
     * - GET /{entity}/count_group：分组统计
     * - GET /{entity}/count：总记录统计
     * 
     * @param request HTTP请求对象，包含统计查询参数
     * @return 统一响应格式，包含统计结果列表
     *         {"result": [...统计结果...]}
     * 
     * 统计模式：
     * 1. 总记录统计：返回总记录数
     *    GET /user/count → [{"count": 150}]
     * 2. 分组统计：按字段分组并统计每组数量
     *    GET /user/count_group?groupby=status → [{"status": 1, "count": 100}, {"status": 0, "count": 50}]
     * 
     * 应用场景：
     * - 分页查询：计算总页数，总记录数 / 每页大小
     * - 数据概览：了解系统中各种数据的分布情况
     * - 业务统计：统计不同状态、类型的数据量
     * - 监控仪表板：显示系统中的数据统计信息
     * 
     * 查询参数：
     * - groupby: 分组字段（可选）
     * - 其他WHERE条件：支持条件筛选
     * - like: 模糊查询模式
     */
    @RequestMapping(value = {"/count_group", "/count"})
    public Map<String, Object> count(HttpServletRequest request) {
        Query count = service.count(service.readQuery(request), service.readConfig(request));
        return success(count.getResultList());
    }

    /**
     * 求和统计接口 - 计算数值字段的总和
     * 
     * 功能说明：
     * - 提供SUM聚合函数，计算指定数值字段的总和
     * - 支持无条件求和和分组求和两种模式
     * - 常用于财务报表、销售统计等场景
     * 
     * 支持的路径：
     * - GET /{entity}/sum_group：分组求和
     * - GET /{entity}/sum：总求和
     * 
     * @param request HTTP请求对象，包含求和查询参数
     * @return 统一响应格式，包含求和结果列表
     *         {"result": [...求和结果...]}
     * 
     * 求和模式：
     * 1. 总求和：计算所有记录指定字段的总和
     *    GET /order/sum?field=amount → [{"SUM(amount)": 50000.00}]
     * 2. 分组求和：按字段分组并计算每组的总和
     *    GET /order/sum_group?field=amount&groupby=region → [{"region": "北京", "SUM(amount)": 25000.00}]
     * 
     * 必需参数：
     * - field: 要进行求和的数值字段名（必填）
     * - groupby: 分组字段（可选）
     * 
     * 应用场景：
     * - 销售统计：计算各地区的销售总额
     * - 财务统计：计算收入、支出、成本等总和
     * - 库存统计：计算各分类商品的总价值
     * - 用户统计：计算用户积分、余额等总和
     * 
     * 注意事项：
     * - 只能对数值类型字段进行求和
     * - NULL值不参与求和计算
     * - 分组求和时，每组独立计算总和
     */
    @RequestMapping(value = {"/sum_group", "/sum"})
    public Map<String, Object> sum(HttpServletRequest request) {
        Query count = service.sum(service.readQuery(request), service.readConfig(request));
        return success(count.getResultList());
    }

    /**
     * 平均值统计接口 - 计算数值字段的平均值
     * 
     * 功能说明：
     * - 提供AVG聚合函数，计算指定数值字段的平均值
     * - 支持无条件平均值和分组平均值两种模式
     * - 常用于绩效分析、价格分析等场景
     * 
     * 支持的路径：
     * - GET /{entity}/avg_group：分组平均值
     * - GET /{entity}/avg：总平均值
     * 
     * @param request HTTP请求对象，包含平均值查询参数
     * @return 统一响应格式，包含平均值结果列表
     *         {"result": [...平均值结果...]}
     * 
     * 平均值模式：
     * 1. 总平均值：计算所有记录指定字段的平均值
     *    GET /product/avg?field=price → [{"AVG(price)": 299.50}]
     * 2. 分组平均值：按字段分组并计算每组的平均值
     *    GET /product/avg_group?field=price&groupby=category → [{"category": "电子产品", "AVG(price)": 1599.00}]
     * 
     * 必需参数：
     * - field: 要计算平均值的数值字段名（必填）
     * - groupby: 分组字段（可选）
     * 
     * 应用场景：
     * - 绩效分析：计算员工的平均绩效得分
     * - 价格分析：计算各类商品的平均价格
     * - 评分统计：计算用户评分的平均值
     * - 质量控制：计算产品各项指标的平均值
     * 
     * 统计特点：
     * - 只对数值类型字段计算平均值
     * - NULL值不参与平均值计算
     * - 分组平均值每组独立计算
     * - 返回结果保留适当的小数位数
     */
    @RequestMapping(value = {"/avg_group", "/avg"})
	public Map<String, Object> avg(HttpServletRequest request) {
        Query count = service.avg(service.readQuery(request), service.readConfig(request));
        return success(count.getResultList());
    }


    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        log.info("进入方法");
        if (file.isEmpty()) {
            return error(30000, "没有选择文件");
        }
        try {
            //判断有没路径，没有则创建
            String filePath = System.getProperty("user.dir") + "\\target\\classes\\static\\upload\\";
            File targetDir = new File(filePath);
            if (!targetDir.exists() && !targetDir.isDirectory()) {
                if (targetDir.mkdirs()) {
                    log.info("创建目录成功");
                } else {
                    log.error("创建目录失败");
                }
            }
//            String path = ResourceUtils.getURL("classpath:").getPath() + "static/upload/";
//            String filePath = path.replace('/', '\\').substring(1, path.length());
            String fileName = file.getOriginalFilename();
            File dest = new File(filePath + fileName);
            log.info("文件路径:{}", dest.getPath());
            log.info("文件名:{}", dest.getName());
            file.transferTo(dest);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", "/api/upload/" + fileName);
            return success(jsonObject);
        } catch (IOException e) {
            log.info("上传失败：{}", e.getMessage());
        }
        return error(30000, "上传失败");
    }

    @PostMapping("/import_db")
    public Map<String, Object> importDb(@RequestParam("file") MultipartFile file) throws IOException {
        service.importDb(file);
        return success(1);
    }

    @RequestMapping("/export_db")
    public void exportDb(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HSSFWorkbook sheets = service.exportDb(service.readQuery(request), service.readConfig(request));
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=employee.xls");
        response.flushBuffer();
        sheets.write(response.getOutputStream());
        sheets.close();
    }

    public Map<String, Object> success(Object o) {
        Map<String, Object> map = new HashMap<>();
        if (o == null) {
            map.put("result", null);
            return map;
        }
        if (o instanceof List) {
            if (((List) o).size() == 1) {
               o =  ((List) o).get(0);
                map.put("result", o);
            }else {
                String jsonString = JSONObject.toJSONString(o);
                JSONArray objects = service.covertArray(JSONObject.parseArray(jsonString));
                map.put("result", objects);
            }
        } else if (o instanceof Integer || o instanceof String) {
            map.put("result", o);
        } else {
            String jsonString = JSONObject.toJSONString(o);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            JSONObject j = service.covertObject(jsonObject);
            map.put("result", j);
        }
        return map;
    }

    public Map<String, Object> error(Integer code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", new HashMap<String, Object>(4) {{
            put("code", code);
            put("message", message);
        }});
        return map;
    }
}
