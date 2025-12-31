package com.project.demo.entity;


import java.sql.Timestamp;
import java.io.Serializable;

import lombok.*;

import javax.persistence.*;


/**
 * 临时访问牌(AccessToken)表实体类
 *
 * @author xxx
 *@since 202X-XX-XX
 */
@Setter
@Getter
@Entity
public class AccessToken implements Serializable {

    private static final long serialVersionUID = 913269304437207500L;

    /**
     * 临时访问牌ID：访问令牌在数据库中的唯一标识
     * 
     * 作用：
     * 1. 主键标识：唯一标识一个Token记录
     * 2. 数据管理：便于Token的查询、更新和删除操作
     * 3. 索引优化：作为主键索引，提高查询性能
     * 4. 外键关联：其他表可能通过此ID引用Token记录
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    /**
     * 临时访问牌：用户身份验证的核心令牌
     * 
     * 生成机制：
     * 1. UUID生成：使用Java的UUID.randomUUID()方法
     * 2. 格式处理：去除UUID中的连字符"-"
     * 3. 唯一性保证：UUID确保令牌在全局范围内唯一
     * 4. 长度：32位十六进制字符串
     * 
     * 安全特性：
     * - 不可预测：UUID算法确保令牌难以猜测
     * - 不可重用：每个登录生成新的唯一令牌
     * - 易于验证：简单的字符串比较即可验证
     * 
     * 存储位置：
     * - 数据库：持久化存储在access_token表中
     * - 客户端：存储在浏览器的localStorage或sessionStorage中
     * - 请求头：作为x-auth-token请求头发送到服务端
     */

    @Basic
    @Column(name = "token")
    private String token;

    /**
     * 最大寿命：Token的有效期时间（秒）
     * 
     * 默认设置：
     * 1. 默认值：2小时（7200秒）
     * 2. 安全考虑：平衡安全性和用户体验
     * 3. 超期处理：Token过期后需要重新登录
     * 
     * 管理策略：
     * - 定时清理：定期清理过期的Token记录
     * - 续期机制：可以在Token即将过期时进行续期
     * - 强制失效：用户主动退出时立即使Token失效
     * 
     * 业务影响：
     * - 过短：频繁登录影响用户体验
     * - 过长：安全风险增加
     * - 动态调整：可以根据用户类型调整有效期
     */

    @Basic
    @Column(name = "maxage")
    private Integer maxage;

    /**
     * 创建时间：Token记录创建的时间戳
     * 
     * 用途：
     * 1. 生命周期管理：计算Token的剩余有效期
     * 2. 安全审计：记录Token的创建时间用于安全分析
     * 3. 清理策略：定期清理超过maxage时间的Token
     * 4. 用户体验：显示Token创建时间（调试时）
     * 
     * 计算逻辑：
     * 当前时间 - create_time < maxage = 有效Token
     * 当前时间 - create_time >= maxage = 过期Token
     */

    @Basic
    @Column(name = "create_time")
    private Timestamp createTime;

    /**
     * 更新时间：Token记录最后更新的时间戳
     * 
     * 更新场景：
     * 1. Token续期：当用户续期时更新此时间
     * 2. 权限变更：当用户权限发生变化时更新
     * 3. 状态检查：验证Token状态时更新时间
     * 4. 安全加固：定期更新以增强安全性
     * 
     * 作用：
     * - 活跃度追踪：记录Token的最后使用时间
     * - 缓存更新：辅助缓存策略的更新判断
     * - 安全监控：检测异常的使用模式
     * - 数据一致性：确保Token状态信息的时效性
     */

    @Basic
    @Column(name = "update_time")
    private Timestamp updateTime;

    /**
     * 用户信息：Token关联的用户ID
     * 
     * 关联机制：
     * 1. 一对多关系：一个用户可以拥有多个Token（多设备登录）
     * 2. 用户识别：通过user_id快速找到Token对应的用户
     * 3. 权限验证：基于用户ID获取用户的权限信息
     * 4. 数据隔离：确保Token只能用于对应的用户操作
     * 
     * 安全特性：
     * - 绑定关系：Token与用户ID强绑定，无法篡改
     * - 权限继承：Token继承用户的权限级别
     * - 操作追踪：通过user_id追踪用户的所有操作
     * - 安全管理：用户注销时清理所有相关Token
     * 
     * 查询优化：
     * - 索引优化：在user_id字段上建立索引提高查询效率
     * - 批量清理：可以根据user_id批量清理用户的Token
     * - 权限缓存：基于user_id缓存用户权限信息
     */

    @Basic
    @Column(name = "user_id")
    private Integer user_id;

}
