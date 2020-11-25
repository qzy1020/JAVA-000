


DROP TABLE IF EXISTS `customer_info`;
CREATE TABLE customer_info(
  customer_id INT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT '自增主键ID',
  customer_name VARCHAR(20) NOT NULL COMMENT '用户真实姓名',
  customer_nick_name VARCHAR(20) NOT NULL COMMENT '用户昵称',
  identity_card_type TINYINT NOT NULL DEFAULT 1 COMMENT '证件类型：1 二代身份证，2 军官证，3 护照',
  identity_card_number VARCHAR(20) NOT NULL COMMENT '证件号码',
  mobile_phone_number INT UNSIGNED NOT NULL COMMENT '手机号',
  customer_email VARCHAR(50) COMMENT '邮箱',
  customer_gender CHAR(1) COMMENT '性别',
  customer_point INT NOT NULL DEFAULT 0 COMMENT '用户积分',
  customer_register_time TIMESTAMP NOT NULL COMMENT '注册时间',
  customer_birthday DATETIME COMMENT '会员生日',
  customer_level TINYINT NOT NULL DEFAULT 1 COMMENT '会员级别：1 青铜 2 白银 3 黄金 4 铂金 5钻石',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_customerid(customer_id)
) ENGINE = innodb COMMENT '用户信息表';


DROP TABLE IF EXISTS `customer_address`;
CREATE TABLE customer_address(
  customer_address_id INT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT '自增主键ID',
  customer_id INT UNSIGNED NOT NULL COMMENT 'customer_info表的自增ID',
  zip_code SMALLINT NOT NULL COMMENT '邮编',
  province VARCHAR(10) NOT NULL COMMENT '省份',
  city VARCHAR(20) NOT NULL COMMENT '城市',
  district VARCHAR(20) NOT NULL COMMENT '区或县',
  town VARCHAR(20) NOT NULL COMMENT '镇或乡',
  address VARCHAR(200) NOT NULL COMMENT '具体地址',
  is_default TINYINT NOT NULL COMMENT '是否默认地址',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_customeraddressid(customer_address_id)
) ENGINE = innodb COMMENT '用户地址表';


DROP TABLE IF EXISTS `product_info`;
CREATE TABLE product_info(
  product_id INT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT '商品ID',
  product_core CHAR(20) NOT NULL COMMENT '商品编码',
  product_name VARCHAR(20) NOT NULL COMMENT '商品名称',
  bar_code VARCHAR(50) NOT NULL COMMENT '条形码',
  brand_id INT UNSIGNED NOT NULL COMMENT '品牌表的ID',
  one_category_id SMALLINT UNSIGNED NOT NULL COMMENT '一级分类ID',
  two_category_id SMALLINT UNSIGNED NOT NULL COMMENT '二级分类ID',
  three_category_id SMALLINT UNSIGNED NOT NULL COMMENT '三级分类ID',
  supplier_id INT UNSIGNED NOT NULL COMMENT '商品的供应商ID',
  product_price DECIMAL(8,2) NOT NULL COMMENT '商品销售价格',
  product_cost DECIMAL(18,2) NOT NULL COMMENT '商品成本',
  publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '上下架状态：0下架1上架',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0未审核，1已审核',
  product_weight FLOAT COMMENT '商品重量',
  product_size VARCHAR(20) COMMENT '商品尺寸',
  color_type ENUM('红','绿','蓝','黑'),
  production_date DATETIME NOT NULL COMMENT '生产日期',
  production_life INT NOT NULL COMMENT '商品有效期',
  product_descript VARCHAR(300) NOT NULL COMMENT '商品描述',
  product_indate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品录入时间',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_productid(product_id)
) ENGINE = innodb COMMENT '商品信息表';


DROP TABLE IF EXISTS `supplier_info`;
CREATE TABLE supplier_info(
  supplier_id INT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT '供应商ID',
  supplier_code CHAR(8) NOT NULL COMMENT '供应商编码',
  supplier_name CHAR(50) NOT NULL COMMENT '供应商名称',
  supplier_type TINYINT NOT NULL COMMENT '供应商类型：1.自营，2.平台',
  link_man VARCHAR(10) NOT NULL COMMENT '供应商联系人',
  phone_number VARCHAR(50) NOT NULL COMMENT '联系电话',
  bank_name VARCHAR(50) NOT NULL COMMENT '供应商开户银行名称',
  bank_account VARCHAR(50) NOT NULL COMMENT '银行账号',
  address VARCHAR(200) NOT NULL COMMENT '供应商地址',
  supplier_status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0禁止，1启用',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_supplierid(supplier_id)
) ENGINE = innodb COMMENT '供应商信息表';


DROP TABLE IF EXISTS `brand_info`;
CREATE TABLE brand_info(
  brand_id SMALLINT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT '品牌ID',
  brand_name VARCHAR(50) NOT NULL COMMENT '品牌名称',
  telephone VARCHAR(50) NOT NULL COMMENT '联系电话',
  brand_desc VARCHAR(150) COMMENT '品牌描述',
  brand_status TINYINT NOT NULL DEFAULT 0 COMMENT '品牌状态,0禁用,1启用',
  brand_order TINYINT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_brandid (brand_id)
)ENGINE=innodb COMMENT '品牌信息表';


DROP TABLE IF EXISTS `product_category`;
CREATE TABLE product_category(
  category_id SMALLINT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT '分类ID',
  category_name VARCHAR(10) NOT NULL COMMENT '分类名称',
  category_code VARCHAR(10) NOT NULL COMMENT '分类编码',
  parent_id SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID',
  category_level TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级',
  category_status TINYINT NOT NULL DEFAULT 1 COMMENT '分类状态',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_categoryid(category_id)
)ENGINE=innodb COMMENT '商品分类表'



DROP TABLE IF EXISTS `order_info`;
CREATE TABLE order_info(
  order_id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  order_num BIGINT UNSIGNED NOT NULL COMMENT '订单编号',
  customer_id INT UNSIGNED NOT NULL COMMENT '下单人ID',
  shipping_user VARCHAR(10) NOT NULL COMMENT '收货人姓名',
  payment_method TINYINT NOT NULL COMMENT '支付方式：1现金，2余额，3网银，4支付宝，5微信',
  order_money DECIMAL(8,2) NOT NULL COMMENT '订单金额',
  district_money DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  shipping_money DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  payment_money DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  shipping_num VARCHAR(50) COMMENT '快递单号',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  ship_id INT UNSIGNED NOT NULL COMMENT 'shipping_info表的自增ID',
  shipping_time DATETIME COMMENT '发货时间',
  pay_time DATETIME COMMENT '支付时间',
  receive_time DATETIME COMMENT '收货时间',
  order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态',
  order_point INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单积分',
  is_invoice tinyint(1) NOT NULL COMMENT '是否开具发票：0 是 1否',
  invoice_title VARCHAR(100) COMMENT '发票抬头',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_orderid(order_id)
)ENGINE = innodb COMMENT '订单主表';


DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE order_detail(
  order_detail_id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单详情表ID',
  order_id INT UNSIGNED NOT NULL COMMENT '订单表ID',
  product_id INT UNSIGNED NOT NULL COMMENT '订单商品ID',
  product_name VARCHAR(50) NOT NULL COMMENT '商品名称',
  product_num INT NOT NULL DEFAULT 1 COMMENT '购买商品数量',
  product_price DECIMAL(8,2) NOT NULL COMMENT '购买商品单价',
  product_cost DECIMAL(8,2) NOT NULL COMMENT '实际价格',
  product_weight FLOAT COMMENT '商品重量',
  free_money DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_orderdetailid(order_detail_id)
)ENGINE = innodb COMMENT '订单详情表'


DROP TABLE IF EXISTS `shipping_info`;
CREATE TABLE shipping_info(
  ship_id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  ship_name VARCHAR(20) NOT NULL COMMENT '快递公司名称',
  ship_contact VARCHAR(20) NOT NULL COMMENT '快递公司联系人',
  telephone VARCHAR(20) NOT NULL COMMENT '快递公司联系电话',
  price DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '配送价格',
  create_time DATETIME(0) NOT NULL '创建时间',
  update_time DATETIME(0)  NOT NULL '更新时间',
  PRIMARY KEY pk_shipid(ship_id)
)ENGINE = innodb COMMENT '物流公司信息表';

