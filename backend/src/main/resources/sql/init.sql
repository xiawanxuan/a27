-- 创建数据库
CREATE DATABASE IF NOT EXISTS herbarium DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE herbarium;

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS `taxonomy` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '中文名称',
  `latin_name` VARCHAR(150) DEFAULT NULL COMMENT '拉丁名',
  `rank` VARCHAR(20) NOT NULL COMMENT '分类等级',
  `level` INT NOT NULL COMMENT '层级',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_rank` (`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物分类表';

-- 标本表
CREATE TABLE IF NOT EXISTS `specimen` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `specimen_no` VARCHAR(50) NOT NULL COMMENT '标本编号',
  `name` VARCHAR(100) NOT NULL COMMENT '中文名',
  `latin_name` VARCHAR(150) DEFAULT NULL COMMENT '拉丁名',
  `taxonomy_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `collector` VARCHAR(100) DEFAULT NULL COMMENT '采集人',
  `collection_date` DATE DEFAULT NULL COMMENT '采集日期',
  `collection_location` VARCHAR(255) DEFAULT NULL COMMENT '采集地点',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `habitat` VARCHAR(255) DEFAULT NULL COMMENT '生境',
  `description` TEXT COMMENT '描述',
  `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_specimen_no` (`specimen_no`),
  KEY `idx_taxonomy_id` (`taxonomy_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物标本表';

-- 标本图片表
CREATE TABLE IF NOT EXISTS `specimen_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `specimen_id` BIGINT NOT NULL COMMENT '标本ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `image_type` VARCHAR(20) DEFAULT 'specimen' COMMENT '图片类型',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_specimen_id` (`specimen_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标本图片表';

-- 特征数据表
CREATE TABLE IF NOT EXISTS `feature_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `specimen_id` BIGINT NOT NULL COMMENT '标本ID',
  `leaf_length` DECIMAL(10,2) DEFAULT NULL COMMENT '叶片长度(mm)',
  `leaf_width` DECIMAL(10,2) DEFAULT NULL COMMENT '叶片宽度(mm)',
  `leaf_area` DECIMAL(10,2) DEFAULT NULL COMMENT '叶片面积(mm²)',
  `leaf_perimeter` DECIMAL(10,2) DEFAULT NULL COMMENT '叶片周长(mm)',
  `aspect_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '长宽比',
  `leaf_shape` VARCHAR(50) DEFAULT NULL COMMENT '叶形',
  `leaf_margin` VARCHAR(50) DEFAULT NULL COMMENT '叶缘',
  `leaf_apex` VARCHAR(50) DEFAULT NULL COMMENT '叶端',
  `leaf_base` VARCHAR(50) DEFAULT NULL COMMENT '叶基',
  `texture` VARCHAR(50) DEFAULT NULL COMMENT '质地',
  `color_features` TEXT COMMENT '颜色特征JSON',
  `feature_vector` TEXT COMMENT '特征向量JSON',
  `extracted_at` DATETIME DEFAULT NULL COMMENT '提取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_specimen_id` (`specimen_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='形态特征数据表';

-- 识别记录表
CREATE TABLE IF NOT EXISTS `recognition_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `specimen_id` BIGINT DEFAULT NULL COMMENT '标本ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '识别图片',
  `predicted_name` VARCHAR(100) DEFAULT NULL COMMENT '预测名称',
  `confidence` DECIMAL(5,4) DEFAULT NULL COMMENT '置信度',
  `top_predictions` TEXT COMMENT 'TopN预测结果JSON',
  `is_confirmed` TINYINT DEFAULT 0 COMMENT '是否确认 0未确认 1已确认',
  `confirmed_by` BIGINT DEFAULT NULL COMMENT '确认人',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_specimen_id` (`specimen_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图像识别记录表';

-- 导出任务表
CREATE TABLE IF NOT EXISTS `export_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `export_type` VARCHAR(50) NOT NULL COMMENT '导出类型',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
  `file_url` VARCHAR(255) DEFAULT NULL COMMENT '文件地址',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending/processing/completed/failed',
  `total_count` INT DEFAULT 0 COMMENT '总记录数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导出任务表';

-- 插入角色数据
INSERT INTO `role` (`name`, `code`, `description`) VALUES
('系统管理员', 'ADMIN', '系统最高权限，可管理所有功能'),
('标本管理员', 'SPECIMEN_ADMIN', '负责标本的录入、编辑和管理'),
('普通用户', 'USER', '可浏览标本信息和进行识别分析');

-- 插入初始分类数据
INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(0, '植物界', 'Plantae', 'kingdom', 1, 1),
(0, '动物界', 'Animalia', 'kingdom', 1, 2);

-- 插入初始用户数据 (密码: admin123，使用BCrypt加密)
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6iAt6 是 admin123 的BCrypt哈希
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role_id`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin@herbarium.com', '系统管理员', 1, 1),
('curator', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'curator@herbarium.com', '标本管理员', 2, 1),
('user', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user@herbarium.com', '普通用户', 3, 1);

-- 插入示例分类数据
INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(1, '被子植物门', 'Angiospermae', 'phylum', 2, 1),
(1, '裸子植物门', 'Gymnospermae', 'phylum', 2, 2),
(1, '蕨类植物门', 'Pteridophyta', 'phylum', 2, 3),
(1, '苔藓植物门', 'Bryophyta', 'phylum', 2, 4);

INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(2, '双子叶植物纲', 'Dicotyledoneae', 'class', 3, 1),
(2, '单子叶植物纲', 'Monocotyledoneae', 'class', 3, 2);

INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(6, '蔷薇目', 'Rosales', 'order', 4, 1),
(6, '豆目', 'Fabales', 'order', 4, 2),
(6, '壳斗目', 'Fagales', 'order', 4, 3),
(6, '杨柳目', 'Salicales', 'order', 4, 4);

INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(10, '蔷薇科', 'Rosaceae', 'family', 5, 1),
(10, '榆科', 'Ulmaceae', 'family', 5, 2),
(11, '豆科', 'Fabaceae', 'family', 5, 3),
(13, '杨柳科', 'Salicaceae', 'family', 5, 4);

INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(14, '蔷薇属', 'Rosa', 'genus', 6, 1),
(14, '苹果属', 'Malus', 'genus', 6, 2),
(14, '梨属', 'Pyrus', 'genus', 6, 3),
(14, '桃属', 'Amygdalus', 'genus', 6, 4),
(16, '槐属', 'Sophora', 'genus', 6, 5),
(17, '杨属', 'Populus', 'genus', 6, 6),
(17, '柳属', 'Salix', 'genus', 6, 7);

INSERT INTO `taxonomy` (`parent_id`, `name`, `latin_name`, `rank`, `level`, `sort_order`) VALUES
(18, '月季', 'Rosa chinensis', 'species', 7, 1),
(18, '玫瑰', 'Rosa rugosa', 'species', 7, 2),
(19, '海棠', 'Malus spectabilis', 'species', 7, 3),
(21, '桃', 'Amygdalus persica', 'species', 7, 4),
(22, '国槐', 'Sophora japonica', 'species', 7, 5),
(24, '垂柳', 'Salix babylonica', 'species', 7, 6);

-- 插入示例标本数据
INSERT INTO `specimen` (`specimen_no`, `name`, `latin_name`, `taxonomy_id`, `collector`, `collection_date`, `collection_location`, `latitude`, `longitude`, `habitat`, `description`, `creator_id`, `status`) VALUES
('SP202401010001', '月季', 'Rosa chinensis', 30, '张三', '2024-03-15', '北京植物园', 39.992947, 116.284547, '温带落叶阔叶林区', '花多红色，偶有白色，可供观赏。', 2, 1),
('SP202401010002', '玫瑰', 'Rosa rugosa', 31, '李四', '2024-04-20', '山东青岛崂山', 36.154182, 120.570832, '海滨山地灌丛', '花紫红色，芳香浓郁。', 2, 1),
('SP202401010003', '海棠', 'Malus spectabilis', 32, '王五', '2024-04-10', '江苏南京中山植物园', 32.060255, 118.796877, '亚热带常绿阔叶林区', '花粉红色，果实近球形。', 2, 1),
('SP202401010004', '桃', 'Amygdalus persica', 33, '赵六', '2024-03-25', '浙江杭州西湖', 30.274085, 120.155070, '亚热带季风气候区', '花粉红色，果实可食用。', 2, 1),
('SP202401010005', '国槐', 'Sophora japonica', 34, '钱七', '2024-06-15', '北京颐和园', 39.999943, 116.275529, '温带季风气候区', '树冠圆形，花淡黄色。', 2, 1),
('SP202401010006', '垂柳', 'Salix babylonica', 35, '孙八', '2024-05-20', '江苏扬州瘦西湖', 32.403643, 119.420333, '亚热带湿润气候区', '枝条下垂，叶狭披针形。', 2, 1),
('SP202401020001', '银杏', 'Ginkgo biloba', 3, '周九', '2024-10-10', '陕西西安大雁塔', 34.222115, 108.954204, '温带落叶阔叶林', '落叶大乔木，叶扇形。', 2, 1),
('SP202401020002', '水杉', 'Metasequoia glyptostroboides', 3, '吴十', '2024-11-05', '湖北利川', 30.357841, 108.951234, '亚热带山地', '落叶乔木，线形叶对生。', 2, 1);
