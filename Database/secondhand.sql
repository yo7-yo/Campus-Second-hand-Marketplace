if db_id('SecondHandDB') is null
begin
    create database SecondHandDB;
end

use SecondHandDB;

declare @sql nvarchar(max) = '';
select @sql += 'ALTER TABLE [' + schema_name(schema_id) + '].[' + object_name(parent_object_id) + '] DROP CONSTRAINT [' + name + '];'
from sys.foreign_keys;
exec sp_executesql @sql;

if object_id('Complaint', 'U') is not null drop table Complaint;
if object_id('Message', 'U') is not null drop table Message;
if object_id('TradeOrder', 'U') is not null drop table TradeOrder;
if object_id('Product', 'U') is not null drop table Product;
if object_id('Category', 'U') is not null drop table Category;
if object_id('[User]', 'U') is not null drop table [User];
go

create table [user] (
    id int identity(1,1) primary key,
    username nvarchar(50) not null,
    password nvarchar(100) not null,
    creditscore int default 100,
    is_admin bit default 0,
    studentid nvarchar(50),
    gender nvarchar(10)
);

create table category (
    id int identity(1,1) primary key,
    name nvarchar(50) not null
);

create table product (
    id int identity(1,1) primary key,
    title nvarchar(100) not null,
    description nvarchar(500),
    price decimal(10,2) not null,
    imagepath nvarchar(200),
    ownerid int not null,
    categoryid int null,
    status nvarchar(20) default 'available',
    location nvarchar(100),
    createdtime datetime2 default sysdatetime(),
    constraint fk_product_user foreign key (ownerid) references [user](id),
    constraint fk_product_category foreign key (categoryid) references category(id)
);

create table tradeorder (
    id int identity(1,1) primary key,
    productid int not null,
    buyerid int not null,
    sellerid int not null,
    status nvarchar(20) default 'pending',
    refund_reason nvarchar(500),
    refund_response nvarchar(500),
    refund_status nvarchar(20) default 'none',
    createdtime datetime2 default sysdatetime(),
    constraint fk_order_product foreign key (productid) references product(id),
    constraint fk_order_buyer foreign key (buyerid) references [user](id),
    constraint fk_order_seller foreign key (sellerid) references [user](id)
);

create table message (
    id int identity(1,1) primary key,
    senderid int not null,
    receiverid int not null,
    content nvarchar(1000) not null,
    createdtime datetime2 default sysdatetime(),
    constraint fk_message_sender foreign key (senderid) references [user](id),
    constraint fk_message_receiver foreign key (receiverid) references [user](id)
);

create table complaint (
    id int identity(1,1) primary key,
    order_id int,
    complainant_id int not null,
    respondent_id int not null,
    reason nvarchar(500) not null,
    respondent_reply nvarchar(1000),
    status nvarchar(20) default 'pending',
    admin_reply nvarchar(1000),
    created_time datetime2 default sysdatetime(),
    resolved_time datetime2,
    constraint fk_complaint_order foreign key (order_id) references tradeorder(id),
    constraint fk_complaint_complainant foreign key (complainant_id) references [user](id),
    constraint fk_complaint_respondent foreign key (respondent_id) references [user](id)
);
go

-- 管理员账号
insert into [user](username, password, is_admin) values('dayo', '123456', 1);
-- 卖家账号
insert into [user](username, password, is_admin) values('yooo', '123456', 0);

insert into category(name) values('数码'), ('教材'), ('生活'), ('服饰'), ('运动'), ('其他');

insert into product(title, description, price, ownerid, categoryid) values('蓝牙耳机', '九成新蓝牙耳机', 50.00, 2, 1);
insert into product(title, description, price, ownerid, categoryid) values('高等数学教材', '大一上册教材', 15.00, 2, 2);
insert into product(title, description, price, ownerid, categoryid) values('折叠小桌', '可折叠便携小桌子，宿舍神器', 35.00, 2, 3);
insert into product(title, description, price, ownerid, categoryid) values('iPhone13', '自用iPhone13 128G 国行全网通', 3200.00, 2, 1);
insert into product(title, description, price, ownerid, categoryid) values('台灯', 'LED护眼台灯，三档调光', 25.00, 2, 3);
insert into product(title, description, price, ownerid, categoryid) values('考研政治', '肖秀荣考研政治全套，含笔记', 30.00, 2, 2);
insert into product(title, description, price, ownerid, categoryid) values('运动鞋', 'Nike Air Force 1 白色 42码', 280.00, 2, 4);
insert into product(title, description, price, ownerid, categoryid) values('无线鼠标', '罗技M330无线静音鼠标', 45.00, 2, 1);
insert into product(title, description, price, ownerid, categoryid) values('保温杯', '500ml不锈钢保温杯，保温12小时', 20.00, 2, 3);
insert into product(title, description, price, ownerid, categoryid) values('线性代数', '同济版线性代数教材，八成新', 12.00, 2, 2);
insert into product(title, description, price, ownerid, categoryid) values('瑜伽垫', '加厚防滑瑜伽垫，送收纳袋', 40.00, 2, 5);
insert into product(title, description, price, ownerid, categoryid) values('扑克牌', '全新未拆封扑克牌两副', 8.00, 2, 6);
go

update product set imagepath = '蓝牙耳机.png' where id = 1 and title like '%蓝牙耳机%';
update product set imagepath = '高等数学教材.png' where id = 2 and title like '%高等数学%';
update product set imagepath = '折叠小桌.png' where id = 3 and title like '%折叠%桌%';
update product set imagepath = 'iPhone13.png' where id = 4 and title like '%iPhone%';
update product set imagepath = '台灯.png' where id = 5 and title like '%台灯%';
update product set imagepath = '考研政治.png' where id = 6 and title like '%考研政治%';
update product set imagepath = '运动鞋.png' where id = 7 and title like '%运动鞋%';
update product set imagepath = '无线鼠标.png' where id = 8 and title like '%鼠标%';
update product set imagepath = '保温杯.png' where id = 9 and title like '%保温杯%';
update product set imagepath = '线性代数.png' where id = 10 and title like '%线性代数%';
update product set imagepath = '瑜伽垫.png' where id = 11 and title like '%瑜伽%';
update product set imagepath = '扑克牌.png' where id = 12 and title like '%扑克%';