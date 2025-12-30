# PictureManager
## B/S大程设计 - 图片管理系统
本次实验的目的是设计一个包括用户注册登录、图片上传、图片浏览、图片删除等功能的图片管理系统。  
系统采用B/S架构，前端使用HTML、CSS和JavaScript，后端使用Java语言开发，数据库使用MySQL。

### 后端启动
1. 启动MySQL容器
```angular2html
# 在项目根目录执行：
docker-compose up mysql -d

# 检查是否运行成功
docker-compose ps

# 执行SQL脚本初始化数据库
docker exec -it pic-manager-db mysql -uroot -proot

```
2. 编译项目
后端：
```angular2html
# 进入backend目录
cd backend

# 清理并编译项目
mvn clean compile

# 或者下载依赖
mvn spring-boot:run
```

前端：
```angular2html
# 进入frontend目录
cd frontend

# 如果是第一次，请安装依赖
npm install

# 启动前端开发服务器
npm run dev
```

数据库
```angular2html
# 登录 MySQL
mysql -h 127.0.0.1 -P 3307 -u root -p picture_manager

# 输入密码root后，查看数据库
show databases;

# 切换到 picture_manager 数据库
use picture_manager;
show tables;

# 查看 user 表结构
desc user;

# 退出数据库
quit / exit
```
