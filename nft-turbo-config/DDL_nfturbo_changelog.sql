# 2024-12-31 held_collection 增加参考价格和稀有度

ALTER TABLE `held_collection`
	ADD COLUMN `reference_price` decimal(18,6)  NULL COMMENT ' 参考价格' AFTER `biz_type`,
	ADD COLUMN `rarity` varchar(64) NULL COMMENT ' 稀有度' AFTER `reference_price`


# 2024-12-31 新增盲盒相关表

/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = blind_box   */
/******************************************/
CREATE TABLE `blind_box` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '最后更新时间',
  `name` varchar(512) DEFAULT NULL COMMENT '盲盒名称',
  `cover` varchar(512) DEFAULT NULL COMMENT '盲盒封面',
  `detail` text COMMENT '详情',
  `identifier` varchar(128) DEFAULT NULL COMMENT '幂等号',
  `state` varchar(128) DEFAULT NULL COMMENT '状态',
  `quantity` bigint DEFAULT NULL COMMENT '盲盒数量',
  `price` decimal(18,6) DEFAULT NULL COMMENT '价格',
  `saleable_inventory` bigint DEFAULT NULL COMMENT '可销售库存',
  `occupied_inventory` bigint DEFAULT NULL COMMENT '已占用库存',
  `create_time` datetime DEFAULT NULL COMMENT '盲盒创建时间',
  `sale_time` datetime DEFAULT NULL COMMENT '盲盒发售时间',
  `allocate_rule` varchar(512) DEFAULT NULL COMMENT '盲盒分配规则',
  `sync_chain_time` datetime DEFAULT NULL COMMENT '上链时间',
  `creator_id` varchar(128) DEFAULT NULL COMMENT '创建者',
  `collection_configs` text COMMENT '藏品配置',
  `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
  `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_state_name` (`state`,`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3 COMMENT='盲盒表'
;

/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = blind_box_inventory_stream   */
/******************************************/
CREATE TABLE `blind_box_inventory_stream` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '最后更新时间',
  `blind_box_id` bigint DEFAULT NULL COMMENT '盲盒id',
  `changed_quantity` bigint DEFAULT NULL COMMENT '本次变更的数量',
  `price` decimal(18,6) DEFAULT NULL COMMENT '价格',
  `quantity` bigint DEFAULT NULL COMMENT '藏品数量',
  `state` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '状态',
  `saleable_inventory` bigint DEFAULT NULL COMMENT '可售库存',
  `occupied_inventory` bigint DEFAULT NULL COMMENT '已占库存',
  `stream_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流水类型',
  `identifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '幂等号',
  `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
  `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
  `extend_info` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '扩展信息',
  PRIMARY KEY (`id`),
  KEY `idx_cid_ident_type` (`identifier`,`stream_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=561 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci AVG_ROW_LENGTH=16384 ROW_FORMAT=DYNAMIC COMMENT='盲盒表库存流水'
;

/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = blind_box_item   */
/******************************************/
CREATE TABLE `blind_box_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '最后更新时间',
  `blind_box_id` bigint DEFAULT NULL COMMENT '盲盒id',
  `name` varchar(512) DEFAULT NULL COMMENT '盲盒名称',
  `cover` varchar(512) DEFAULT NULL COMMENT '盲盒封面',
  `collection_name` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '藏品名称',
  `collection_cover` varchar(512) DEFAULT NULL COMMENT '藏品封面',
  `collection_detail` text COMMENT '藏品详情',
  `collection_serial_no` varchar(128) DEFAULT NULL COMMENT '持有藏品的序列号',
  `state` varchar(128) DEFAULT NULL COMMENT '状态',
  `user_id` varchar(128) DEFAULT NULL COMMENT '持有人id',
  `purchase_price` decimal(18,6) DEFAULT NULL COMMENT '购入价格',
  `order_id` varchar(128) DEFAULT NULL COMMENT '订单号',
  `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
  `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
  `rarity` varchar(32) DEFAULT NULL COMMENT '稀有度',
  `reference_price` decimal(18,6) DEFAULT NULL COMMENT '市场参考价',
  `opened_time` datetime DEFAULT NULL COMMENT ' 开盒时间',
  `assign_time` datetime DEFAULT NULL COMMENT ' 分配时间',
  PRIMARY KEY (`id`),
  KEY `idx_state_box_id` (`blind_box_id`,`state`),
  KEY `idx_user` (`order_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4011 DEFAULT CHARSET=utf8mb3 COMMENT='盲盒条目表'
;


# 2024-09-20 pay_order 针对支付单号增加唯一性约束
ALTER TABLE `pay_order`
	DROP KEY `idx_pay_order`,
	ADD Unique KEY `uk_pay_order`(`pay_order_id`) USING BTREE

# 2024-08-31 trade_order 表新增reverse_buyer_id

ALTER TABLE `trade_order_0000`
	ADD COLUMN `reverse_buyer_id` varchar(32) NULL COMMENT '逆序的买家ID' AFTER `buyer_id`,
	ADD KEY `idx_rvbuyer_state`(`reverse_buyer_id`,`order_state`,`gmt_create`) USING BTREE
;

ALTER TABLE `trade_order_0001`
	ADD COLUMN `reverse_buyer_id` varchar(32) NULL COMMENT '逆序的买家ID' AFTER `buyer_id`,
	ADD KEY `idx_rvbuyer_state`(`reverse_buyer_id`,`order_state`,`gmt_create`) USING BTREE
;

ALTER TABLE `trade_order_0002`
	ADD COLUMN `reverse_buyer_id` varchar(32) NULL COMMENT '逆序的买家ID' AFTER `buyer_id`,
	ADD KEY `idx_rvbuyer_state`(`reverse_buyer_id`,`order_state`,`gmt_create`) USING BTREE
;

ALTER TABLE `trade_order_0003`
	ADD COLUMN `reverse_buyer_id` varchar(32) NULL COMMENT '逆序的买家ID' AFTER `buyer_id`,
	ADD KEY `idx_rvbuyer_state`(`reverse_buyer_id`,`order_state`,`gmt_create`) USING BTREE
;

update trade_order_0000 set `reverse_buyer_id`  = REVERSE(`buyer_id` );
update trade_order_0001 set `reverse_buyer_id`  = REVERSE(`buyer_id` );
update trade_order_0003 set `reverse_buyer_id`  = REVERSE(`buyer_id` );
update trade_order_0002 set `reverse_buyer_id`  = REVERSE(`buyer_id` );


# 2024-08-25 新增refund_order表

/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = refund_order   */
/******************************************/
CREATE TABLE `refund_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `refund_order_id` varchar(32) NOT NULL COMMENT '支付单号',
  `identifier` varchar(128) NOT NULL COMMENT '幂等号',
  `pay_order_id` varchar(32) NOT NULL COMMENT '支付单号',
  `pay_channel_stream_id` varchar(64) DEFAULT NULL COMMENT '支付的渠道流水号',
  `paid_amount` decimal(18,6) DEFAULT NULL COMMENT '已支付金额',
  `payer_id` varchar(32) NOT NULL COMMENT '付款方iD',
  `payer_type` varchar(32) NOT NULL COMMENT '付款方类型',
  `payee_id` varchar(32) NOT NULL COMMENT '收款方id',
  `payee_type` varchar(32) NOT NULL COMMENT '收款方类型',
  `apply_refund_amount` decimal(18,6) NOT NULL COMMENT '申请退款金额',
  `refunded_amount` decimal(18,6) DEFAULT NULL COMMENT '退款成功金额',
  `refund_channel_stream_id` varchar(64) DEFAULT NULL COMMENT '退款的渠道流水号',
  `refund_channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款方式',
  `memo` varchar(512) DEFAULT NULL COMMENT '备注',
  `refund_order_state` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款单状态',
  `refund_succeed_time` datetime DEFAULT NULL COMMENT '退款成功时间',
  `deleted` tinyint DEFAULT NULL COMMENT '逻辑删除标识',
  `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_pay_order` (`pay_order_id`) USING BTREE,
  KEY `uk_identifier` (`identifier`,`pay_order_id`,`refund_channel`),
  KEY `idx_refund_order` (`refund_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
;
