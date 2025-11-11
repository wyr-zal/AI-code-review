-- AI代码审查平台数据库初始化脚本

CREATE DATABASE IF NOT EXISTS code_review DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE code_review;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（MD5加密）',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT(1) DEFAULT 0 COMMENT '用户角色（0-普通用户，1-管理员）',
    `status` TINYINT(1) DEFAULT 1 COMMENT '账号状态（0-禁用，1-正常）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 代码审查任务表
CREATE TABLE `review_task` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `code_content` TEXT NOT NULL COMMENT '代码内容',
    `language` VARCHAR(50) NOT NULL COMMENT '编程语言',
    `ai_model` VARCHAR(50) NOT NULL COMMENT 'AI模型',
    `status` TINYINT(1) DEFAULT 0 COMMENT '审查状态（0-待审查，1-审查中，2-已完成，3-审查失败）',
    `review_result` TEXT COMMENT '审查结果（JSON格式）',
    `quality_score` INT(3) DEFAULT NULL COMMENT '质量评分（0-100）',
    `security_score` INT(3) DEFAULT NULL COMMENT '安全评分（0-100）',
    `performance_score` INT(3) DEFAULT NULL COMMENT '性能评分（0-100）',
    `issue_count` INT(5) DEFAULT 0 COMMENT '问题数量',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码审查任务表';

-- 插入测试用户（用户名：admin，密码：123456）
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`, `status`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@codereview.com', '管理员', 1, 1);

INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`, `status`)
VALUES ('testuser', 'e10adc3949ba59abbe56e057f20f883e', 'test@codereview.com', '测试用户', 0, 1);
