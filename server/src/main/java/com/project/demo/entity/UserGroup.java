package com.project.demo.entity;


import java.io.Serializable;
import java.sql.Timestamp;

import lombok.*;

import javax.persistence.*;


/**
 * 用户组：用于用户前端身份和鉴权(UserGroup)表实体类
 *
 * @author xxx
 *@since 202X-XX-XX
 */
@Setter
@Getter
@Entity
public class UserGroup implements Serializable {

    private static final long serialVersionUID = 968356951391304707L;

    /**
     * 用户组ID：[0,8388607] 用户组的唯一标识符
     * 
     * 作用：
     * 1. 主键标识：唯一标识一个用户组
     * 2. 外键关联：其他表通过此ID引用用户组
     * 3. 数据查询：用于快速定位特定用户组
     * 4. 权限管理：通过用户组ID控制权限范围
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId;

    /**
     * 显示顺序：[0,1000] 用户组在界面中的显示优先级
     * 
     * 用途：
     * 1. 界面排序：决定用户组在下拉列表、选择框中的显示顺序
     * 2. 用户体验：重要用户组优先显示
     * 3. 管理便利：便于管理员快速找到常用用户组
     * 4. 权限层次：数值越小优先级越高
     * 
     * 取值范围：通常0-1000，0表示最高优先级
     */

    @Basic
    @Column(name = "display")
    private Integer display;

    /**
     * 名称：[0,16] 用户组的名称标识
     * 
     * 功能：
     * 1. 标识作用：唯一标识用户组的名称
     * 2. 权限关联：通过名称与用户实体关联
     * 3. 界面显示：在用户界面中显示的用户组名称
     * 4. 管理操作：管理员通过名称识别和管理用户组
     * 
     * 命名规范：
     * - 使用英文或拼音，便于系统处理
     * - 具有业务含义，如"ordinary_users"
     * - 避免重复，确保唯一性
     */

    @Basic
    @Column(name = "name")
    private String name;

    /**
     * 描述：[0,255] 详细描述该用户组的特点、权限范围和使用场景
     * 
     * 内容包括：
     * 1. 用户组定位：说明该用户组的主要用途
     * 2. 权限范围：详细描述用户组具有的权限
     * 3. 适用对象：说明哪些用户适合加入此组
     * 4. 业务场景：描述用户组适用的业务场景
     * 
     * 示例：
     * "普通用户组：可以进行学习室预约、签到、评价等基础功能"
     */

    @Basic
    @Column(name = "description")
    private String description;

    /**
     * 来源表：用于动态权限检查的业务表名
     * 
     * 作用机制：
     * 1. 动态审核：当不为空时，登录时需要检查用户在对应业务表中的审核状态
     * 2. 业务关联：将用户组与具体的业务实体（如学习室管理员）关联
     * 3. 权限扩展：通过数据库查询实现动态权限验证
     * 4. 灵活配置：不同用户组可以关联不同的业务表
     * 
     * 使用场景：
     * - 学习室管理员：关联"study_room_administrators"表
     * - 商家用户：关联"merchants"表
     * - 系统管理员：可能为空或关联特定的管理员表
     */

    @Basic
    @Column(name = "source_table")
    private String sourceTable;

    /**
     * 来源字段：业务表中用于关联用户ID的字段名
     * 
     * 功能：
     * 1. 字段定位：指定业务表中用户ID对应的字段名称
     * 2. 查询依据：在动态权限检查时作为WHERE条件的字段
     * 3. 灵活适配：支持不同业务表使用不同的字段名
     * 
     * 常见取值：
     * - "user_id"：最常见的用户ID字段名
     * - "admin_id"：管理员表可能使用的字段名
     * - "member_id"：会员表可能使用的字段名
     */

    @Basic
    @Column(name = "source_field")
    private String sourceField;

    /**
     * 注册位置：标识用户注册的入口或注册时分配的用户组
     * 
     * 用途：
     * 1. 注册分流：不同注册入口分配不同的默认用户组
     * 2. 业务区分：区分普通用户注册和管理员注册
     * 3. 流程控制：控制特定入口用户的权限范围
     * 4. 数据统计：统计不同注册渠道的用户数量
     * 
     * 示例值：
     * - "front"：前端用户注册
     * - "admin"：管理员后台注册
     * - "merchant"：商家注册入口
     */

    @Basic
    @Column(name = "register")
    private String register;

    /**
     * 创建时间：用户组记录创建的时间戳
     * 
     * 用途：
     * 1. 审计记录：记录用户组的创建时间
     * 2. 排序依据：用于按创建时间排序显示用户组
     * 3. 历史追踪：追踪用户组的历史变更
     * 4. 数据分析：统计用户组的创建趋势
     */

    @Basic
    @Column(name = "create_time")
    private Timestamp createTime;

    /**
     * 更新时间：用户组记录最后更新的时间戳
     * 
     * 作用：
     * 1. 变更追踪：记录用户组信息的最后修改时间
     * 2. 同步控制：确保数据的一致性和时效性
     * 3. 缓存更新：辅助缓存策略的更新判断
     * 4. 版本控制：帮助识别数据的新旧程度
     */

    @Basic
    @Column(name = "update_time")
    private Timestamp updateTime;

}

