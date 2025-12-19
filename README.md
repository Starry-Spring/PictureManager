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
```angular2html
# 进入backend目录
cd backend

# 清理并编译项目
mvn clean compile

# 或者下载依赖
mvn spring-boot:run
```
