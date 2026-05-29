# 校园二手交易平台

基于 Spring Boot + Vue 3 的校园二手物品交易系统。

## 技术栈

**后端**
- Spring Boot
- MyBatis
- MySQL

**前端**
- Vue 3
- Element Plus
- Pinia
- Vue Router
- Axios

## 项目结构

```
├── Database/        # 数据库脚本
├── Spring/          # 后端服务
│   ├── src/         # Java 源码
│   └── pom.xml      # Maven 配置
└── Vue/             # 前端项目
    ├── src/         # Vue 源码
    └── package.json # 依赖配置
```

## 功能模块

- 用户注册与登录
- 商品发布与浏览（二手市场）
- 订单管理
- 即时聊天
- 投诉反馈
- 后台管理

## 快速开始

### 1. 数据库

```bash
mysql -u root -p < Database/secondhand.sql
```

### 2. 后端

```bash
cd Spring
# 修改 src/main/resources/application.yml 中的数据库配置
mvn spring-boot:run
```

### 3. 前端

```bash
cd Vue
npm install
npm run dev
```
