# 气象数据预处理系统

本系统用于处理和合并不同格式的气象数据文件，包括地面填图数据、降水数据和相对湿度数据，并生成统一的CSV文件输出。系统支持多种时间尺度的数据处理，包括日、侯（五日）、旬、月、季度、年度以及全部数据的合并处理。

## 项目结构

- `com.epoch.climaticpreprocessing.util` - 工具类包
  - `MeteorologyDataMerger.java` - 核心数据处理类，负责从文件读取数据、合并数据并生成CSV输出
  - `DataParser.java` - 数据解析工具类，负责解析各种气象数据文件
  - `DataMerger.java` - 数据合并工具类，负责合并不同来源的气象数据
  - `CsvExporter.java` - CSV导出工具类，负责将气象数据导出为CSV文件
  - `FileUtil.java` - 文件处理工具类，提供文件路径解析和日期处理功能

- `com.epoch.climaticpreprocessing.domain` - 领域模型包
  - `po` - 持久化对象包，包含气象数据实体类
  - `dto` - 数据传输对象包，包含请求和响应数据对象
  - `enums` - 枚举类包，包含时间尺度等枚举定义
  - `vo` - 视图对象包，包含前端展示数据对象

- `com.epoch.climaticpreprocessing.service` - 服务类包
  - `ClimateDataService.java` - 服务类，提供气象数据合并功能的接口
  
- `com.epoch.climaticpreprocessing.test` - 测试类包
  - `MeteorologyDataMergerManualTest.java` - 测试类，用于手动测试数据合并功能

## 技术实现

- 使用 Java 17 开发
- 基于 Spring Boot 框架构建
- 使用 Apache Commons IO 实现文件读写操作
- 使用 OpenCSV 实现CSV文件的生成和读取
- 使用 Lombok 简化实体类开发
- 面向对象设计与领域驱动设计思想

## 数据处理流程

1. 从指定路径读取三个气象数据文件
2. 解析各文件中的数据
3. 根据站点ID合并数据
4. 按照时间尺度分组处理数据（可选）
5. 生成统一格式的CSV文件

## 支持的时间尺度

系统支持以下时间尺度的数据处理：

- 全部数据 (ALL) - 将所有时次数据合并到一个文件
- 年度数据 (YEAR) - 按年份分组处理数据
- 季度数据 (SEASON) - 按季节（春、夏、秋、冬）分组处理数据
- 月度数据 (MONTH) - 按月份分组处理数据
- 旬数据 (DEKAD) - 按旬（每月三旬：1-10日、11-20日、21-月底）分组处理数据
- 候数据 (PENTAD) - 按候（每月六候：每5天一候）分组处理数据
- 日数据 (DAY) - 按日期分组处理数据

## 输入文件格式

系统可以处理以下三种格式的输入文件：

1. 地面填图数据文件 (plot)
   - 第一行：文件头信息，如 `diamond 1 24年06月05日00时地面填图`
   - 第二行：时间信息，如 `24 06 05 00 2502`
   - 后续每两行：一条站点数据记录

2. 1小时降水数据文件 (rain1-p)
   - 第一行：文件头信息，如 `diamond 3 2024年06月05日00时1小时降水`
   - 第二行：时间信息
   - 后续每行：一条站点数据记录

3. 相对湿度数据文件 (rh-p)
   - 第一行：文件头信息，如 `diamond 3 2024年06月05日00时相对湿度`
   - 第二行：时间信息
   - 后续每行：一条站点数据记录

## 输出文件格式

输出为CSV格式，包含以下字段：
`time,sta,lon,lat,ele,stalev,总云量,风向,风速,海平面气压,3小时变压,过去天气1,过去天气2,6小时降水,低云状,低云量,低云高,露点,能见度,现在天气,温度,中云状,高云状,标志1,标志2,24小时变温,24小时变压,rain1,rh,p0`

## 测试方法

可以直接运行 `MeteorologyDataMergerManualTest` 类的 main 方法进行测试，这个测试类支持多种测试类型：

1. 单时次测试 (single) - 测试单个时次的数据合并
2. 多时次测试 (multiple) - 测试多个时次的数据合并
3. 日测试 (day) - 测试按日期处理数据
4. 全部数据测试 (all) - 测试处理全部数据
5. 年度数据测试 (year) - 测试按年份处理数据
6. 季度数据测试 (season) - 测试按季节处理数据
7. 月度数据测试 (month) - 测试按月份处理数据
8. 旬数据测试 (dekad) - 测试按旬处理数据
9. 候数据测试 (pentad) - 测试按候处理数据

运行测试时可以通过命令行参数指定测试类型，例如：
```
java -cp ... com.epoch.climaticpreprocessing.MeteorologyDataMergerManualTest month
```

## 使用示例

### 单时次数据处理
```java
String plotFilePath = "path/to/plot/file.txt";
String rainFilePath = "path/to/rain/file.txt";
String rhFilePath = "path/to/rh/file.txt";
String outputFilePath = "path/to/output/file.csv";

MeteorologyDataMerger.mergeAndProcessData(plotFilePath, rainFilePath, rhFilePath, outputFilePath);
```

### 按时间尺度处理数据
```java
File plotDir = new File("path/to/plot/dir");
File rainDir = new File("path/to/rain/dir");
File rhDir = new File("path/to/rh/dir");
File outputDir = new File("path/to/output/dir");
TimeScale timeScale = TimeScale.MONTH; // 按月处理

MeteorologyDataMerger.processMeteorologyByTimeScale(plotDir, rainDir, rhDir, outputDir, timeScale);
```

## 项目扩展

- 支持更多气象数据源的集成
- 提供Web界面进行数据处理和可视化
- 增加数据质量控制和异常值过滤
- 支持更多时间尺度和空间尺度的数据处理 