# **FurryBlack - Mirai - Extensions**

## 自豪的使用[Mirai](https://github.com/mamoe/mirai)

### 许可证

See LICENSE

```text
           BTS Anti-Commercial GNU AFFERO GENERAL PUBLIC LICENSE

  This GNU Affero General Public License is extends from The GNU Affero General
Public License, But as Additional Terms, Here is override:

  1: Override A-GPL No.5, "I" keep power to "discriminate against any person or
group of persons.", But for one proper's and for one proper's only:
Any form commercial activity relay on this framework is forbidden, And I will
revoke the permission of "You" to see/use/modify. otherwise, If you use it in
commercial activity (framework itself or your extension or ghostwrite) You will
banned by this project, This project equivalent close source for you. Any read/
copy/use/modify/develop-base-on-it will be legal.

For example:

    If you are charged service provider, You MUST provide OpenAPI for everyone
and create wrapper by FurryBlack into QQ, Clients pay for you OpenAPI NOT the
FurryBlack or your extensions, You extensions must implements A-GPL. Must be
opensource and free to use.

  2: Downstream code/project/your-extension must annotation as BTS Anti 
Commercial GNU AFFERO GENERAL PUBLIC LICENSE, Using another LICENSE equivalent
as commercial usage. You is banned if you do it.
```

## 说明

将模块拆分出来，单独作为一个repo发布，使FurryBlack框架真正的成为框架而不是大杂烩

## CHANGELOG

### 2.0.4

- 适配FurryBlack-Core-1.0.1
- 优化部分写法

### 2.0.3-R

- 撤销 2.0.3

### 2.0.3

- 修复jrjt日志没有对齐
- 调整pom.xml版本依赖为`[0.9.0,0.10.0)`

### 2.0.2

- 调整pom.xml版本依赖为`[0.8.0,0.9.0)`

### 2.0.1

- 修复错误的注解

### 2.0.0

- 更新内核0.8.1重写所有模块

### 1.0.0

- 独立发布插件包

### 0.8.0

- 更新内核
- 取消0.4.1的版本跟随设计，插件包以后保持兼容就不再发布新版本

### 0.7.8

- 更新内核

### 0.7.7

- 更新内核

### 0.7.6

- 更新内核
- cleanup代码

### 0.7.5

- 更新内核

### 0.7.4

- 更新Mirai-2.6.6

### 0.7.3

- 更新Mirai-2.6.5

### 0.7.2

- 更新GitIgnore

### 0.7.1

- 更新内核

### 0.7.0

- 更新内核

### 0.6.7

- 更新内核

### 0.6.6

- 更新内核

### 0.6.5

- 更新内核
- 统一时间功能调用新的API

### 0.6.4

- 更新内核
- 修复Acon的格式

### 0.6.3

- 更新内核

### 0.6.2

- 更新内核

### 0.6.1

- 更新内核

### 0.6.0

- 更新内核

### 0.5.8

- 更新内核

### 0.5.7

- 更新内核

### 0.5.6

- 更新内核

### 0.5.5-M

- 更新内核

### 0.5.4-M

- 更新内核

### 0.5.3

- 优化代码

### 0.5.2

- 更新Mirai-2.5.1

### 0.5.1

- 更新内核
- 鸡汤有可能带换行，改用BASE64保存

### 0.5.0

- 更新内核
- 适配getRunner

### 0.4.21-1

- 修复jrjt的一个BUG

### 0.4.21

- 定时器bug修好了 去掉所有verbose

### 0.4.20

- 更新内核
- 使用统一定时调度器

### 0.4.19

- 更新内核
- 定时器貌似有BUG

### 0.4.18

- 更新内核
- 增加jrjt

### 0.4.17

- 更新内核
- 抛弃Timer自定义线程定时任务

### 0.4.16

- 更新内核

### 0.4.15

- 更新内核

### 0.4.14

- 更新内核
- 修复JRRP的bug

### 0.4.13

- 更新内核
- 添加JRRP的debug项目

### 0.4.12

- 更新内核

### 0.4.11

- 修改配置文件名称

### 0.4.10

- 更新内核
- 更新jrrp修复不会出现100%的BUG

### 0.4.8

- 更新内核

### 0.4.7

- 更新内核

### 0.4.6

- 更新Mirai 2.2.2

### 0.4.5

- 统一消息发送方法
- 尝试优化轮盘赌但是失败了
- 添加API方法
- 微调格式化

### 0.4.4

- 修复日志打印错误
- 美化日志

### 0.4.3

- 修复了一个没有用Driver发消息的BUG
- 持久化的JRRP

### 0.4.1

- 决定还是跟随Core的版本号
- 使用新添加的NickAPI

### 1.0.0-RC-1

- 规范化异常
- 将模块剥离
