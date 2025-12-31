package com.project.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.project.demo.entity.AccessToken;
import com.project.demo.entity.User;
import com.project.demo.entity.UserGroup;
import com.project.demo.service.AccessTokenService;
import com.project.demo.service.UserGroupService;
import com.project.demo.service.UserService;

import com.project.demo.controller.base.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * 用户账户：用于保存用户登录信息(User)表控制层
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController extends BaseController<User, UserService> {
    /**
     * 服务对象
     */
    @Autowired
    public UserController(UserService service) {
        setService(service);
    }

    /**
     * Token服务
     */
    @Autowired
    private AccessTokenService tokenService;

    @Autowired
    private UserGroupService userGroupService;

    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("register")
    public Map<String, Object> signUp(@RequestBody User user) {
        // 查询用户
        Map<String, String> query = new HashMap<>();
        query.put("username",user.getUsername());
        List list = service.select(query, new HashMap<>()).getResultList();
        if (list.size()>0){
            return error(30000, "用户已存在");
        }
        user.setUserId(null);
        user.setPassword(service.encryption(user.getPassword()));
        service.save(user);
        return success(1);
    }

    /**
     * 找回密码
     * @param form
     * @return
     */
    @PostMapping("forget_password")
    public Map<String, Object> forgetPassword(@RequestBody User form,HttpServletRequest request) {
        JSONObject ret = new JSONObject();
        String username = form.getUsername();
        String code = form.getCode();
        String password = form.getPassword();
        // 判断条件
        if(code == null || code.length() == 0){
            return error(30000, "验证码不能为空");
        }
        if(username == null || username.length() == 0){
            return error(30000, "用户名不能为空");
        }
        if(password == null || password.length() == 0){
            return error(30000, "密码不能为空");
        }

        // 查询用户
        Map<String, String> query = new HashMap<>();
        query.put("username",username);
        Query select = service.select(query, service.readConfig(request));
        List list = select.getResultList();
        if (list.size() > 0) {
            User o = (User) list.get(0);
            JSONObject query2 = new JSONObject();
            JSONObject form2 = new JSONObject();
            // 修改用户密码
            query2.put("user_id",o.getUserId());
            form2.put("password",service.encryption(password));
            service.update(query, service.readConfig(request), form2);
            return success(1);
        }
        return error(70000,"用户不存在");
    }

    /**
     * 登录
     * @param data
     * @param httpServletRequest
     * @return
     */
    @PostMapping("login")
    public Map<String, Object> login(@RequestBody Map<String, String> data, HttpServletRequest httpServletRequest) {
        log.info("[执行登录接口]");

        String username = data.get("username");
        String email = data.get("email");
        String phone = data.get("phone");
        String password = data.get("password");

        List resultList = null;
        Map<String, String> map = new HashMap<>();
        // ========== 用户身份验证阶段 ==========
        // 支持多种登录方式：用户名、邮箱、手机号
        if(username != null && "".equals(username) == false){
            // 用户名登录方式
            map.put("username", username);
            resultList = service.select(map, new HashMap<>()).getResultList();
        }
        else if(email != null && "".equals(email) == false){
            // 邮箱登录方式
            map.put("email", email);
            resultList = service.select(map, new HashMap<>()).getResultList();
        }
        else if(phone != null && "".equals(phone) == false){
            // 手机号登录方式
            map.put("phone", phone);
            resultList = service.select(map, new HashMap<>()).getResultList();
        }else{
            return error(30000, "账号或密码不能为空");
        }
        
        // 验证输入参数完整性
        if (resultList == null || password == null) {
            return error(30000, "账号或密码不能为空");
        }
        
        // ========== 用户存在性验证 ==========
        // 判断是否有这个用户
        if (resultList.size()<=0){
            return error(30000,"用户不存在");
        }

        // 提取用户信息
        User byUsername = (User) resultList.get(0);

        // ========== 用户组权限验证阶段 ==========
        // 验证用户所属的用户组是否存在且有效
        Map<String, String> groupMap = new HashMap<>();
        groupMap.put("name",byUsername.getUserGroup());
        List groupList = userGroupService.select(groupMap, new HashMap<>()).getResultList();
        if (groupList.size()<1){
            return error(30000,"用户组不存在");
        }

        UserGroup userGroup = (UserGroup) groupList.get(0);

        // ========== 用户审核状态验证阶段 ==========
        // 对于需要审核的用户组，验证用户的审核状态
        // 这是一种动态权限检查机制，通过sourceTable字段确定需要检查的业务表
        if (!StringUtils.isEmpty(userGroup.getSourceTable())){
            // 构建查询用户审核状态的SQL语句
            String sql = "select examine_state from "+ userGroup.getSourceTable() +" WHERE user_id = " + byUsername.getUserId();
            String res = String.valueOf(service.runCountSql(sql).getSingleResult());
            
            // 检查用户是否存在对应的审核记录
            if (res==null){
                return error(30000,"用户不存在");
            }
            
            // 验证审核状态是否通过
            if (!res.equals("已通过")){
                return error(30000,"该用户审核未通过");
            }
        }

        // ========== 用户账户状态验证阶段 ==========
        // 验证用户账户状态，确保只有可用状态的用户能够登录
        // 这是最关键的安全检查，防止冻结、注销等异常状态用户登录
        if (byUsername.getState()!=1){
            return error(30000,"用户非可用状态，不能登录");
        }

        // ========== 密码验证阶段 ==========
        // 对用户输入的密码进行加密处理
        String md5password = service.encryption(password);
        System.out.println("pwd:"+md5password);
        
        // 验证密码是否正确
        if (byUsername.getPassword().equals(md5password)) {
            // ========== Token生成和存储阶段 ==========
            // 密码验证成功，生成访问令牌
            AccessToken accessToken = new AccessToken();
            
            // 生成UUID格式的访问令牌（去除连字符）
            accessToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
            
            // 关联用户ID，便于后续的Token验证和用户信息获取
            accessToken.setUser_id(byUsername.getUserId());
            
            // 将Token存储到数据库中
            tokenService.save(accessToken);

            // ========== 登录成功响应阶段 ==========
            // 准备返回给前端的用户信息
            JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(byUsername));
            
            // 将生成的访问令牌添加到用户信息中
            user.put("token", accessToken.getToken());
            
            // 构建标准响应格式
            JSONObject ret = new JSONObject();
            ret.put("obj",user);
            
            return success(ret);
        } else {
            // ========== 登录失败处理 ==========
            return error(30000, "账号或密码不正确");
        }
    }

    /**
     * 修改密码
     * @param data
     * @param request
     * @return
     */
    @PostMapping("change_password")
    public Map<String, Object> change_password(@RequestBody Map<String, String> data, HttpServletRequest request){
        // 根据Token获取UserId
        String token = request.getHeader("x-auth-token");
        Integer userId = tokenGetUserId(token);
        // 根据UserId和旧密码获取用户
        Map<String, String> query = new HashMap<>();
        String o_password = data.get("o_password");
        query.put("user_id" ,String.valueOf(userId));
        query.put("password" ,service.encryption(o_password));
        Query ret = service.count(query, service.readConfig(request));
        List list = ret.getResultList();
        Object s = list.get(0);
        int count = Integer.parseInt(list.get(0).toString());
        if(count > 0){
            // 修改密码
            Map<String,Object> form = new HashMap<>();
            form.put("password",service.encryption(data.get("password")));
            service.update(query,service.readConfig(request),form);
            return success(1);
        }
        return error(10000,"密码修改失败！");
    }

    /**
     * 登录态
     * @param request
     * @return
     */
    @GetMapping("state")
    public Map<String, Object> state(HttpServletRequest request) {
        JSONObject ret = new JSONObject();
        // 获取状态
        String token = request.getHeader("x-auth-token");

        // 根据登录态获取用户ID
        Integer userId = tokenGetUserId(token);

        log.info("[返回userId] {}",userId);
        if(userId == null || userId == 0){
            return error(10000,"用户未登录!");
        }

        // 根据用户ID获取用户
        Map<String,String> query = new HashMap<>();
        query.put("user_id" ,String.valueOf(userId));

        // 根据用户ID获取
        Query select = service.select(query,service.readConfig(request));
        List resultList = select.getResultList();
        if (resultList.size() > 0) {
            JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(resultList.get(0)));
            user.put("token",token);
            ret.put("obj",user);
            return success(ret);
        } else {
            return error(10000,"用户未登录!");
        }
    }

    /**
     * 登录态
     * @param request
     * @return
     */
    @GetMapping("quit")
    public Map<String, Object> quit(HttpServletRequest request) {
        String token = request.getHeader("x-auth-token");
        JSONObject ret = new JSONObject();
        Map<String, String> query = new HashMap<>(16);
        query.put("token", token);
        try{
            tokenService.delete(query,service.readConfig(request));
        }catch (Exception e){
            e.printStackTrace();
        }
        return success("退出登录成功！");
    }

    /**
     * 获取登录用户ID
     * @param token
     * @return
     */
    public Integer tokenGetUserId(String token) {
        log.info("[获取的token] {}",token);
        // 根据登录态获取用户ID
        if(token == null || "".equals(token)){
            return 0;
        }
        Map<String, String> query = new HashMap<>(16);
        query.put("token", token);
        AccessToken byToken = tokenService.findOne(query);
        if(byToken == null){
            return 0;
        }
        return byToken.getUser_id();
    }

    /**
     * 重写add
     * @return
     */
    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(HttpServletRequest request) throws IOException {
        Map<String,Object> map = service.readBody(request.getReader());
        map.put("password",service.encryption(String.valueOf(map.get("password"))));
        service.insert(map);
        return success(1);
    }

}
