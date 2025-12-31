package com.project.demo.entity;


import java.sql.Timestamp;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;


/**
 * 用户账户：用于保存用户登录信息(User)表实体类
 *
 * @author xxx
 *@since 202X-XX-XX
 */
@Setter
@Getter
@Entity
public class User implements Serializable {

    private static final long serialVersionUID = -82540585424852966L;

    /**
     * 用户ID：[0,8388607]用户获取其他与用户相关的数据
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 账户状态：[0,10] 用户账户的当前状态，用于控制用户登录和操作权限
     * 
     * 状态码说明：
     * 1 - 可用状态：用户可以正常登录和使用系统功能
     * 2 - 异常状态：账户存在异常情况，需要管理员处理
     * 3 - 已冻结状态：用户因违规等原因被暂时冻结，无法登录
     * 4 - 已注销状态：用户主动或被动注销账户，无法登录
     * 
     * 此状态在登录时会进行严格验证，确保只有可用状态的用户能够登录系统
     */

    @Basic
    @Column(name = "state")
    private Integer state;

    /**
     * 所在用户组：[0,32767] 用户所属的用户组，决定用户的身份和权限范围
     * 
     * 用户组的作用：
     * 1. 权限控制：通过用户组来确定用户可以访问的功能模块
     * 2. 角色区分：区分不同类型的用户（如普通用户、管理员、超级管理员等）
     * 3. 数据隔离：不同用户组可能对应不同的业务数据访问权限
     * 4. 审核流程：某些用户组需要通过审核才能激活权限
     * 
     * 常见的用户组示例：
     * - "ordinary_users" - 普通用户
     * - "study_room_administrators" - 学习室管理员
     * - "system_administrators" - 系统管理员
     */

    @Basic
    @Column(name = "user_group")
    private String userGroup;

    /**
     * 上次登录时间：记录用户最后一次成功登录的时间戳
     * 
     * 用途：
     * 1. 安全监控：检测异常登录行为
     * 2. 用户体验：显示用户最后登录时间
     * 3. 会话管理：辅助判断用户活跃度
     * 4. 统计分析：用户活跃度统计和报告
     * 
     * 更新时机：用户成功登录后自动更新此字段
     */

    @Basic
    @Column(name = "login_time")
    private Timestamp loginTime;

    /**
     * 手机号码：[0,11] 用户的手机号码，用于找回密码、登录验证等操作
     * 
     * 功能作用：
     * 1. 账户安全：作为账户安全验证的重要手段
     * 2. 找回密码：通过手机号找回或重置密码
     * 3. 登录验证：支持手机号+密码的登录方式
     * 4. 通知推送：重要消息和通知的推送渠道
     * 5. 实名认证：部分业务场景下用于实名认证
     * 
     * 数据验证：格式验证、唯一性验证、隐私保护
     */

    @Basic
    @Column(name = "phone")
    private String phone;

    /**
     * 手机认证状态：[0,1] 用户手机号码的认证状态
     * 
     * 认证状态码：
     * 0 - 未认证：用户手机号尚未进行认证
     * 1 - 审核中：手机号认证申请已提交，正在审核
     * 2 - 已认证：手机号已经通过认证，可以正常使用
     * 
     * 认证流程：
     * 1. 用户提交手机号和验证码
     * 2. 系统验证手机号格式和验证码正确性
     * 3. 状态更新为"审核中"
     * 4. 管理员或系统自动审核通过后更新为"已认证"
     * 
     * 业务影响：
     * - 未认证：部分功能受限
     * - 审核中：等待审核结果
     * - 已认证：正常使用所有功能
     */

    @Basic
    @Column(name = "phone_state")
    private Integer phoneState;

    /**
     * 用户名：[0,16]用户登录时所用的账户名称
     */

    @Basic
    @Column(name = "username")
    private String username;

    /**
     * 昵称：[0,16]
     */

    @Basic
    @Column(name = "nickname")
    private String nickname;

    /**
     * 密码：[0,32]用户登录所需的密码，由6-16位数字或英文组成
     */

    @Basic
    @Column(name = "password")
    private String password;

    /**
     * 邮箱：[0,64]用户的邮箱，用于找回密码时或登录时
     */

    @Basic
    @Column(name = "email")
    private String email;

    /**
     * 邮箱认证：[0,1](0未认证|1审核中|2已认证)
     */

    @Basic
    @Column(name = "email_state")
    private Integer emailState;

    /**
     * 头像地址：[0,255]
     */

    @Basic
    @Column(name = "avatar")
    private String avatar;

    /**
     * 创建时间：
     */

    @Basic
    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @Basic
    @Transient
    private String code;
}

