# 智盒 AIBox —— 基于 Spring AI 的多模态 AI 助手平台

一个前后端分离的 AI 应用平台，集成了**多模态对话、AI 角色扮演游戏、智能客服（课程咨询预约）、PDF 文档问答（RAG）、AI 绘图、代码助手、文本摘要、Function Calling 智能 Agent** 等八大功能场景。后端基于 Spring Boot 3 + Spring AI Alibaba（阿里云百炼 / 通义千问），前端基于 Vue 3 + Vite。

---

## 目录

- [一、功能介绍](#一功能介绍)
- [二、技术栈](#二技术栈)
- [三、项目结构](#三项目结构)
- [四、整体架构](#四整体架构)
- [五、环境准备](#五环境准备)
- [六、创建数据库](#六创建数据库)
- [七、后端配置与启动](#七后端配置与启动)
- [八、前端配置与启动](#八前端配置与启动)
- [九、打包部署](#九打包部署)
- [十、接口一览](#十接口一览)
- [十一、常见问题](#十一常见问题)

---

## 一、功能介绍

| 功能模块 | 说明 | 实现要点 |
| --- | --- | --- |
| AI 聊天 | 多模态流式对话，支持发送**图片**进行图文问答 | SSE 流式输出、通义千问多模态模型 |
| 哄哄模拟器 | 角色扮演游戏《哄女友大作战》，AI 扮演生气的女友，带原谅值数值系统 | 长 System Prompt 角色扮演 |
| 智能客服 | 培训学校客服「小黑」，可咨询课程、查询校区、**自动生成课程预约单** | Function Calling 真实读写数据库 |
| ChatPDF | 上传 PDF 后基于文档内容问答，**不会编造文档中没有的答案** | RAG：PDF 分页切片 + 向量化 + 相似度检索 |
| AI 图像生成 | 输入一句话生成创意图片 | 通义万相 `wanx2.1-t2i-turbo` 文生图 |
| AI 代码助手 | 代码编写、Bug 排查、代码解释与优化建议 | 通义千问 Coder 专用模型 |
| AI 文本摘要 | 长文本快速提炼核心要点 | 低温度参数保证摘要稳定 |
| 智能 Agent 助手 | 自动调用工具完成数学计算、商城商品查询、门店查询、下单/删单 | 多工具 Function Calling 编排 |

此外，所有对话模块都支持**多会话管理**：会话列表查看、历史消息回放、批量删除（同时清除 AI 记忆）。

---

## 二、技术栈

### 后端（`ai-box`）

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| JDK | 17 | 运行环境 |
| Spring Boot | 3.4.5 | Web 基础框架 |
| Spring AI | 1.0.0 | AI 应用编排（ChatClient / Advisor / ChatMemory / RAG） |
| Spring AI Alibaba | 1.0.0.2 | 阿里云百炼 DashScope 接入（通义千问、通义万相、Embedding） |
| MyBatis-Plus | 3.5.10.1 | ORM 框架，操作业务表 |
| MySQL | 8.x | 业务数据 + AI 会话记忆持久化 |
| Spring JDBC | 随 Boot 版本 | JdbcTemplate、Spring AI JDBC ChatMemory |
| Reactor（Flux） | 随 Spring AI | 流式（SSE）响应 |
| Lombok | 随 Boot 版本 | 简化样板代码 |
| SimpleVectorStore | Spring AI 内置 | 内存向量库，支撑 PDF RAG 检索 |
| PagePdfDocumentReader | Spring AI PDF | PDF 按页解析切片 |

**使用到的大模型：**

- `qwen-turbo`：默认对话模型（客服、游戏、摘要、Agent）
- 通义千问 Omni 多模态模型：图文理解（AI 聊天）
- `qwen3-coder-flash`：代码助手
- `text-embedding-v3`：文本向量化（1024 维，用于 PDF RAG）
- `wanx2.1-t2i-turbo`：文生图

### 前端（`spring-ai-protal`）

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Vue | 3.4 | 前端框架 |
| Vite | 6.x | 构建与开发服务器 |
| Vue Router | 4 | 路由 |
| Pinia | 3 | 状态管理 |
| Naive UI | 2.41 | 组件库 |
| @heroicons/vue | 2.x | 图标 |
| marked + highlight.js + dompurify | - | Markdown 渲染、代码高亮、XSS 防护 |
| @pdftron/webviewer | 11.x | PDF 在线预览 |
| TypeScript | 5.7 | 类型支持 |
| Sass | 1.x | 样式预处理 |

---

## 三、项目结构

```
AIBox/
├── ai-box/                          # 后端：Spring Boot 3 工程
│   ├── src/main/java/com/yule/ai/
│   │   ├── AIBoxApplication.java    # 启动类
│   │   ├── config/                  # ChatClient / VectorStore / 跨域配置
│   │   ├── constants/               # 各场景 System Prompt 提示词
│   │   ├── controller/              # REST 控制器（聊天/客服/PDF/绘图等）
│   │   ├── entity/                  # po（持久化对象）/ query / vo
│   │   ├── mapper/                  # MyBatis-Plus Mapper 接口
│   │   ├── repository/              # 会话历史、PDF 文件存取实现
│   │   ├── service/                 # 课程、商城等业务 Service
│   │   ├── tools/                   # Function Calling 工具（课程/商城/数学）
│   │   └── utils/                   # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/                  # MyBatis XML
│   │   ├── application.yaml         # 公共配置
│   │   └── application-dev.yaml     # 本地敏感配置（已被 .gitignore 忽略，需自建）
│   └── pom.xml
│
└── spring-ai-protal/                # 前端：Vue 3 + Vite 工程
    ├── src/
    │   ├── views/                   # 8 个功能页面
    │   ├── components/              # 聊天消息、PDF 预览等公共组件
    │   ├── services/api.js          # 后端接口统一封装
    │   ├── router/                  # 路由表
    │   └── stores/                  # Pinia
    ├── vite.config.js
    └── package.json
```

---

## 四、整体架构

```
 浏览器（Vue 3 + Naive UI）
        │  HTTP 请求 / SSE 流式响应（fetch ReadableStream 逐字渲染）
        ▼
 Spring Boot 3（为每个场景装配独立 ChatClient：系统提示词 + 模型 + 工具）
        │
        ├── ChatMemory（JDBC 持久化到 MySQL，窗口保留最近 15 条消息）
        ├── Function Calling ── CourseTools / MallTools / MathTools
        │                          └── MyBatis-Plus 读写业务表
        ├── RAG 检索增强 ── PDF 分页切片 → text-embedding-v3 向量化
        │                    → SimpleVectorStore 相似度检索 → 注入上下文
        └── 文生图 / 多模态理解
        │
        ▼
 阿里云百炼大模型平台（DashScope SDK）
```

---

## 五、环境准备

开始前请确保本机已安装以下环境：

| 软件 | 版本要求 | 验证命令 |
| --- | --- | --- |
| JDK | 17 及以上 | `java -version` |
| Maven | 3.6.3 及以上 | `mvn -v` |
| MySQL | 8.x（5.7 亦可） | `mysql --version` |
| Node.js | 18 及以上（推荐 20 LTS） | `node -v` |
| npm | 随 Node 安装即可 | `npm -v` |

另外需要一个**阿里云百炼 API-KEY**：

1. 打开[阿里云百炼控制台](https://bailian.console.aliyun.com/)注册登录；
2. 在「API-KEY 管理」中创建一个 API-KEY（形如 `sk-xxxx`）；
3. 确保账号已开通通义千问文本模型、多模态模型、`text-embedding-v3` 向量模型、通义万相文生图模型的服务（新用户通常有免费额度）。

> 使用 IDEA 启动后端时，请确保已安装 **Lombok 插件**并开启 Annotation Processing。

---

## 六、创建数据库

本项目共需要 **9 张表**：Spring AI 会话记忆表 1 张、会话索引表 1 张、PDF 绑定表 1 张、课程/商城业务表 6 张。

登录 MySQL 后，**依次执行下面的 SQL 即可**（包含建库、建表和演示数据）：

```sql
-- ============================================================
-- 0. 创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS aibox
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;
USE aibox;

-- ============================================================
-- 1. SPRING_AI_CHAT_MEMORY：Spring AI 官方会话记忆表
--    存放每个会话的用户/AI 历史消息（JSON 格式）
-- ============================================================
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(36)  NOT NULL COMMENT '会话ID',
    content         TEXT         NOT NULL COMMENT '消息内容(JSON)',
    type            VARCHAR(10)  NOT NULL COMMENT '消息类型: USER / ASSISTANT / SYSTEM / TOOL',
    `timestamp`     TIMESTAMP    NOT NULL COMMENT '消息时间',
    CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
    INDEX IDX_CHAT_MEMORY_CONV_TS (conversation_id, `timestamp`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'Spring AI 会话记忆表';

-- ============================================================
-- 2. chat_history：会话列表索引表（前端左侧会话栏）
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_history (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    type    VARCHAR(32) NOT NULL COMMENT '业务类型: chat/game/service/pdf/code/summary/agent',
    chat_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    UNIQUE KEY uk_type_chatid (type, chat_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '会话历史索引表';

-- ============================================================
-- 3. course：课程（学科）表
--    edu 学历要求：0-无，1-初中，2-高中，3-大专，4-本科以上
-- ============================================================
CREATE TABLE IF NOT EXISTS course (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL COMMENT '学科名称',
    edu      TINYINT      NOT NULL DEFAULT 0 COMMENT '学历背景要求: 0-无 1-初中 2-高中 3-大专 4-本科以上',
    type     VARCHAR(50)  COMMENT '课程类型: 编程/设计/自媒体/其它',
    price    BIGINT       NOT NULL DEFAULT 0 COMMENT '课程价格(单位: 分)',
    duration INT          COMMENT '学习时长(单位: 天)'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '课程表';

-- ============================================================
-- 4. school：校区表
-- ============================================================
CREATE TABLE IF NOT EXISTS school (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '校区名称',
    city VARCHAR(50)  COMMENT '校区所在城市'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '校区表';

-- ============================================================
-- 5. course_reservation：课程预约单表（客服调用工具生成）
-- ============================================================
CREATE TABLE IF NOT EXISTS course_reservation (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    course       VARCHAR(100) COMMENT '预约课程',
    student_name VARCHAR(50)  COMMENT '学生姓名',
    contact_info VARCHAR(50)  COMMENT '联系方式',
    school       VARCHAR(100) COMMENT '预约校区',
    remark       VARCHAR(255) COMMENT '备注'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '课程预约单表';

-- ============================================================
-- 6. product：商城商品表
-- ============================================================
CREATE TABLE IF NOT EXISTS product (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL COMMENT '商品名称',
    type  VARCHAR(50)  COMMENT '商品类型: 数码/服饰/食品/家居/其它',
    price BIGINT       NOT NULL DEFAULT 0 COMMENT '商品价格(单位: 分)',
    stock INT          NOT NULL DEFAULT 0 COMMENT '库存数量'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '商品表';

-- ============================================================
-- 7. store：门店表
-- ============================================================
CREATE TABLE IF NOT EXISTS store (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    city VARCHAR(50)  COMMENT '门店所在城市'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '门店表';

-- ============================================================
-- 8. orders：商品订单表（Agent 调用工具生成）
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    product       VARCHAR(100) COMMENT '订购商品',
    store         VARCHAR(100) COMMENT '取货门店',
    customer_name VARCHAR(50)  COMMENT '客户姓名',
    contact_info  VARCHAR(50)  COMMENT '联系电话',
    remark        VARCHAR(255) COMMENT '备注'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '商品订单表';

-- ============================================================
-- 9. pdf_chat_file：PDF 会话与文件绑定表
-- ============================================================
CREATE TABLE IF NOT EXISTS pdf_chat_file (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id     VARCHAR(64)  NOT NULL COMMENT '会话ID',
    file_name   VARCHAR(255) NOT NULL COMMENT 'PDF 文件名',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'PDF 会话文件绑定表';

-- ============================================================
-- 演示数据（智能客服和 Agent 商城功能依赖这些数据，建议执行）
-- ============================================================
INSERT INTO course (name, edu, type, price, duration) VALUES
    ('Java 零基础就业班',   1, '编程',   899900, 120),
    ('Python 数据分析实战', 2, '编程',   699900, 90),
    ('Web 前端工程师班',    2, '编程',   799900, 100),
    ('UI/UX 设计精品课',    3, '设计',   599900, 60),
    ('平面设计基础班',      1, '设计',   299900, 30),
    ('短视频自媒体运营',    0, '自媒体', 399900, 45);

INSERT INTO school (name, city) VALUES
    ('北京朝阳校区', '北京'),
    ('上海浦东校区', '上海'),
    ('广州天河校区', '广州'),
    ('深圳南山校区', '深圳'),
    ('成都武侯校区', '成都');

INSERT INTO product (name, type, price, stock) VALUES
    ('无线蓝牙耳机', '数码', 29900, 200),
    ('智能运动手表', '数码', 89900, 80),
    ('机械键盘',     '数码', 45900, 150),
    ('纯棉短袖 T 恤', '服饰', 7900, 500),
    ('连帽卫衣',     '服饰', 15900, 300),
    ('每日坚果礼盒', '食品', 12900, 120),
    ('北欧护眼台灯', '家居', 19900, 60),
    ('陶瓷马克杯',   '家居', 4900, 400);

INSERT INTO store (name, city) VALUES
    ('北京中关村店', '北京'),
    ('上海静安店',   '上海'),
    ('广州体育西店', '广州'),
    ('杭州西湖店',   '杭州'),
    ('成都春熙路店', '成都');
```

> 业务表价格字段单位均为**分**（如 29900 = 299 元）。预约单、订单两张表无需初始化数据，由 AI 调用工具后自动写入。

---

## 七、后端配置与启动

### 1. 创建本地配置文件

`application-dev.yaml` 保存数据库密码和 API-KEY 等敏感信息，**已被 `.gitignore` 忽略，不会提交到 Git**，所以 clone 项目后需要手动创建。

在 `ai-box/src/main/resources/` 目录下新建 `application-dev.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aibox?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码          # 改成你自己的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  ai:
    dashscope:
      api-key: sk-你的百炼API-KEY     # 改成你自己的阿里云百炼 API-KEY
```

公共配置 `application.yaml` 中已指定 `spring.profiles.active: dev`，并配置好了默认模型，**一般无需修改**：

- 对话模型：`qwen-turbo`（temperature 0.8）
- 向量模型：`text-embedding-v3`（1024 维）
- 画图模型：`wanx2.1-t2i-turbo`
- ChatMemory 自动建表已关闭（`initialize-schema: never`），所以**必须先执行第六节的 SQL 手动建表**。

### 2. 启动后端

**方式一：IDEA 启动（推荐）**

找到启动类 [AIBoxApplication.java](ai-box/src/main/java/com/yule/ai/AIBoxApplication.java)，右键 Run。

**方式二：命令行启动**

```bash
cd ai-box
mvn spring-boot:run
```

启动成功后后端运行在 **http://localhost:8080**，控制台看到 `Started AIBoxApplication` 即表示成功。

> 注意：请在 `ai-box` 目录下启动后端。PDF 问答上传的文件会保存在后端进程的工作目录中。

---

## 八、前端配置与启动

前端已在 [services/api.js](spring-ai-protal/src/services/api.js) 中把后端地址写死为 `http://localhost:8080`，且后端已全局放开跨域（CORS），**无需额外配置**。

```bash
cd spring-ai-protal
npm install        # 安装依赖（第一次运行需要）
npm run dev        # 启动开发服务器
```

启动后浏览器访问 **http://localhost:5173** 即可看到「智盒」首页，依次启动后端、前端后就能体验全部功能。

> 如果 `npm install` 速度慢，可切换淘宝镜像：`npm config set registry https://registry.npmmirror.com`

---

## 九、打包部署

### 后端打包为 Jar

```bash
cd ai-box
mvn clean package -DskipTests
java -jar target/ai-box-0.0.1-SNAPSHOT.jar
```

### 前端打包静态资源

```bash
cd spring-ai-protal
npm run build
```

产物输出在 `spring-ai-protal/dist/` 目录，可使用任意静态服务器 / Nginx 部署，或本地预览：

```bash
npm run preview
```

> 生产部署时如需修改后端地址，更改 `src/services/api.js` 中的 `BASE_URL` 后重新打包即可。

---

## 十、接口一览

| 功能 | 请求方式 | 接口路径 |
| --- | --- | --- |
| AI 多模态聊天 | POST | `/ai/chat`（表单：prompt、chatId、files 可选） |
| 哄哄模拟器 | GET | `/ai/game?prompt=&chatId=` |
| 智能客服 | GET | `/ai/service?prompt=&chatId=` |
| PDF 上传 | POST | `/ai/pdf/upload/{chatId}`（multipart 文件） |
| PDF 问答 | GET | `/ai/pdf/chat?prompt=&chatId=` |
| PDF 下载 | GET | `/ai/pdf/file/{chatId}` |
| AI 绘图 | GET | `/ai/image?prompt=` |
| 代码助手 | GET | `/ai/code?prompt=&chatId=` |
| 文本摘要 | GET | `/ai/summary?prompt=&chatId=` |
| Agent 助手 | GET | `/ai/agent?prompt=&chatId=` |
| 会话列表 | GET | `/ai/history/{type}` |
| 会话消息详情 | GET | `/ai/history/{type}/{chatId}` |
| 删除会话 | DELETE | `/ai/history/{type}` |

---

## 十一、常见问题

**1. 启动报错：Access denied for user 'root' 或 Communications link failure**
数据库连接信息不对。检查 `application-dev.yaml` 中的用户名密码、MySQL 是否已启动、3306 端口是否被占用，以及是否已创建 `aibox` 数据库。

**2. 调用 AI 接口报 401 / 403 / Invalid API-KEY**
百炼 API-KEY 无效或欠费。检查 `application-dev.yaml` 中的 `api-key`，并到百炼控制台确认对应模型服务已开通、额度充足。

**3. 启动报表不存在，如 `Table 'aibox.SPRING_AI_CHAT_MEMORY' doesn't exist`**
说明第六节的建表 SQL 没有执行完整。项目关闭了框架自动建表，9 张表必须手动创建。

**4. 前端页面能打开但对话无响应 / 报网络错误**
后端没启动或端口不是 8080。请先启动后端，再刷新页面。

**5. 5173 或 8080 端口被占用怎么办？**
- 后端：在 `application.yaml` 中加 `server.port: 8081`，同时修改前端 `api.js` 的 `BASE_URL`；
- 前端：`npm run dev -- --port 5174`。

**6. 智能客服查不到课程、Agent 查不到商品？**
业务表是空的，请执行第六节 SQL 中的演示数据插入语句。

**7. PDF 问答重启后为什么需要重新上传？**
当前向量库使用内存版 SimpleVectorStore，向量数据随进程生命周期；会话与文件的绑定关系在 MySQL 中持久保存，但重新上传 PDF 后即可恢复问答。

**8. 聊天记录保存在哪里？**
消息内容保存在 MySQL 的 `SPRING_AI_CHAT_MEMORY` 表，会话索引保存在 `chat_history` 表。每个会话最多保留最近 15 条消息（窗口策略自动淘汰最早消息）。
