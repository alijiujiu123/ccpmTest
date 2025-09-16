# CV Agent - 智能简历优化系统

## 项目简介

CV Agent是一个基于AI的简历优化工具，帮助技术求职者根据招聘需求优化简历，提高求职成功率。

## 技术栈

- **后端**: Spring Boot 3.2.0, Java 17
- **AI集成**: langchain4j, OpenAI GPT
- **数据库**: MongoDB
- **文件处理**: Apache PDFBox, Apache POI
- **缓存**: Caffeine
- **安全**: Spring Security
- **部署**: Docker

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- OpenAI API Key

### 配置环境变量

创建 `.env` 文件：

```bash
OPENAI_API_KEY=your-openai-api-key
OPENAI_BASE_URL=https://api.openai.com
OPENAI_MODEL=gpt-3.5-turbo
FILE_STORAGE_PATH=./uploads
```

### 本地开发启动

1. 安装依赖
   \`\`\`bash
   mvn clean install
   \`\`\`

2. 启动应用
   \`\`\`bash
   mvn spring-boot:run
   \`\`\`

3. 访问应用
   - API地址: http://localhost:8080/api
   - 健康检查: http://localhost:8080/api/actuator/health

### Docker启动

```bash
docker-compose up -d
```

## 项目结构

\`\`\`
src/
├── main/
│   ├── java/com/cvagent/
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── service/        # 服务层
│   │   ├── repository/     # 数据访问层
│   │   ├── model/          # 数据模型
│   │   ├── dto/            # 数据传输对象
│   │   ├── security/       # 安全配置
│   │   └── exception/      # 异常处理
│   └── resources/
│       ├── static/        # 静态资源
│       ├── templates/      # 模板文件
│       └── application.yml # 应用配置
└── test/                  # 测试文件
\`\`\`

## 开发指南

### 代码风格

- 使用统一的代码格式化配置
- 遵循Spring Boot最佳实践
- 添加必要的注释和文档

### API文档

启动应用后访问：http://localhost:8080/api/swagger-ui.html

## 许可证

MIT License
