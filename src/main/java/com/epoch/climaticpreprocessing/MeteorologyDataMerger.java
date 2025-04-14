package com.epoch.climaticpreprocessing;

import org.apache.commons.io.FileUtils;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 气象数据合并处理工具类
 */
public class MeteorologyDataMerger {
    
    /**
     * 合并并处理气象数据
     * @param plotFilePath 地面填图数据文件路径
     * @param rainFilePath 降水数据文件路径
     * @param rhFilePath 相对湿度数据文件路径
     * @param outputFilePath 输出CSV文件路径
     * @throws IOException 如果文件读写过程中发生错误
     */
    public static void mergeAndProcessData(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath) throws IOException {
        // 使用Apache Commons IO从文件读取数据
        String plotData = FileUtils.readFileToString(new File(plotFilePath), StandardCharsets.UTF_8);
        String rainData = FileUtils.readFileToString(new File(rainFilePath), StandardCharsets.UTF_8);
        String rhData = FileUtils.readFileToString(new File(rhFilePath), StandardCharsets.UTF_8);

        // 从文件名中提取时间信息
        String timeStr = extractTimeFromFilePath(plotFilePath);
        System.out.println("从文件名中提取的时间: " + timeStr);

        // 解析各文件数据
        List<String[]> plotRecords = parsePlotData(plotData);
        Map<String, String[]> rainRecords = parseRainData(rainData);
        Map<String, String[]> rhRecords = parseRhData(rhData);
        
        // 打印各个数据集的大小用于调试
        System.out.println("地面填图数据记录数: " + plotRecords.size());
        System.out.println("降水数据记录数: " + rainRecords.size());
        System.out.println("相对湿度数据记录数: " + rhRecords.size());
        
        // 合并数据
        List<String[]> mergedData = mergeData(plotRecords, rainRecords, rhRecords, timeStr);
        System.out.println("合并后的数据记录数: " + mergedData.size());

        // 使用OpenCSV生成CSV文件
        generateCSV(mergedData, outputFilePath);
    }

    /**
     * 从文件路径中提取时间信息
     * @param filePath 文件路径
     * @return 格式化的时间字符串
     */
    private static String extractTimeFromFilePath(String filePath) {
        // 获取文件名（不含扩展名）
        File file = new File(filePath);
        String fileName = file.getName();
        // 移除扩展名
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        
        // 确保文件名符合预期格式（如：24060502）
        if (fileName.length() >= 8) {
            // 格式化为: 20240605020000
            // 假设文件名格式为: 年年月月日日时时
            String year = "20" + fileName.substring(0, 2); // 添加"20"前缀
            String month = fileName.substring(2, 4);
            String day = fileName.substring(4, 6);
            String hour = fileName.substring(6, 8);
            
            return year + month + day + hour + "0000";
        }
        
        // 如果文件名不符合预期格式，返回默认时间
        return "20240605000000";
    }

    // 使用OpenCSV生成CSV文件
    private static void generateCSV(List<String[]> mergedData, String outputFilePath) throws IOException {
        File outputFile = new File(outputFilePath);
        
        // 确保输出目录存在
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile), 
                                             ',',              // 分隔符使用逗号
                                             '\0',             // 不使用引号字符
                                             '\\',             // 转义字符
                                             "\n")) {          // 行结束符
            
            // 写入标题行
            String[] header = {"time", "sta", "lon", "lat", "ele", "stalev", "总云量", "风向", "风速", 
                              "海平面气压", "3小时变压", "过去天气1", "过去天气2", "6小时降水", "低云状", 
                              "低云量", "低云高", "露点", "能见度", "现在天气", "温度", "中云状", "高云状", 
                              "标志1", "标志2", "24小时变温", "24小时变压", "rain1", "rh", "p0"};
            writer.writeNext(header);
            
            // 处理数据行：确保空值为空字符串而不是逗号
            for (String[] record : mergedData) {
                if (record == null) continue; // 跳过null记录
                // 处理数据中的空值，将","替换为空字符串
                for (int i = 0; i < record.length; i++) {
                    if (record[i] != null && record[i].equals(",")) {
                        record[i] = ""; // 使用空字符串代替","
                    }
                }
                writer.writeNext(record);
            }
        }
    }

    // 解析地面填图数据
    private static List<String[]> parsePlotData(String data) {
        List<String[]> records = new ArrayList<>();
        String[] lines = data.split("\n");

        if (lines.length >= 2) {
            System.out.println("地面填图文件标头行: " + lines[0]);
            System.out.println("地面填图文件信息行: " + lines[1]);
            
            // 尝试获取记录数信息
            String[] infoLine = lines[1].trim().split("\\s+");
            if (infoLine.length >= 5) {
                System.out.println("地面填图文件记录数声明: " + infoLine[4]);
            }
        }

        // 每条记录占两行，从第三行开始解析
        for (int i = 2; i < lines.length; i += 2) {
            if (i + 1 < lines.length) {
                String line1 = lines[i].trim();
                String line2 = lines[i + 1].trim();

                if (!line1.isEmpty() && !line2.isEmpty()) {
                    String[] parts = line1.split("\\s+");
                    // 跳过站点ID为99999的记录
                    if (parts.length > 0 && "99999".equals(parts[0])) {
                        System.out.println("跳过地面填图中的99999站点数据");
                        continue;
                    }
                    
                    String[] record = parsePlotRecord(line1, line2);
                    records.add(record);
                }
            }
        }

        // 测试数据，检查是否有重复站点
        Set<String> uniqueStations = new HashSet<>();
        for (String[] record : records) {
            String stationId = record[0];
            if (uniqueStations.contains(stationId)) {
                System.out.println("发现重复站点ID: " + stationId);
            }
            uniqueStations.add(stationId);
        }
        System.out.println("地面填图数据中唯一站点数: " + uniqueStations.size());
        // 测试数据

        return records;
    }

    // 解析单条地面填图记录
    private static String[] parsePlotRecord(String line1, String line2) {
        String[] record = new String[26];

        // 解析第一行（站号、经纬度等基本信息和部分气象参数）
        String[] parts1 = line1.trim().split("\\s+");
        // 检查站点ID是否为空或格式异常
        if (parts1.length > 0) {
            String stationId = parts1[0].trim();
            // 如果站点ID全为0，打印警告信息
            if ("0000".equals(stationId)) {
                System.out.println("警告：发现全0站点ID：" + line1);
            }
            record[0] = stationId; // sta
        } else {
            System.out.println("警告：无法解析站点ID，行内容：" + line1);
            record[0] = "unknown"; // 使用默认值
        }

        // 如果有足够的字段，继续解析
        if (parts1.length >= 12) {
            record[1] = parts1[1]; // lon
            record[2] = parts1[2]; // lat
            record[3] = parts1[3]; // ele
            record[4] = parts1[4]; // stalev
            record[5] = parts1[5]; // 总云量
            record[6] = parts1[6]; // 风向
            record[7] = parts1[7]; // 风速
            record[8] = parts1[8]; // 海平面气压
            record[9] = parts1[9]; // 3小时变压
            record[10] = parts1[10]; // 过去天气1
            record[11] = parts1[11]; // 过去天气2
        } else {
            System.out.println("警告：字段数量不足，行内容：" + line1);
        }

        // 解析第二行（降水、云状等其他参数）
        String[] parts2 = line2.trim().split("\\s+");
        if (parts2.length >= 14) {
            record[12] = parts2[0]; // 6小时降水
            record[13] = parts2[1]; // 低云状
            record[14] = parts2[2]; // 低云量
            record[15] = parts2[3]; // 低云高
            record[16] = parts2[4]; // 露点
            record[17] = parts2[5]; // 能见度
            record[18] = parts2[6]; // 现在天气
            record[19] = parts2[7]; // 温度
            record[20] = parts2[8]; // 中云状
            record[21] = parts2[9]; // 高云状
            record[22] = parts2[10]; // 标志1
            record[23] = parts2[11]; // 标志2
            record[24] = parts2[12]; // 24小时变温
            record[25] = parts2[13]; // 24小时变压
        } else {
            System.out.println("警告：第二行字段数量不足，行内容：" + line2);
        }

        return record;
    }

    // 解析降水数据
    private static Map<String, String[]> parseRainData(String data) {
        Map<String, String[]> records = new HashMap<>();
        String[] lines = data.split("\n");

        if (lines.length >= 2) {
            System.out.println("降水文件标头行: " + lines[0]);
            System.out.println("降水文件信息行: " + lines[1]);
            
            // 尝试获取记录数信息
            String[] infoLine = lines[1].trim().split("\\s+");
            if (infoLine.length >= 11) {
                System.out.println("降水文件记录数声明: " + infoLine[10]);
            }
        }

        // 从第三行开始解析数据
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            if (parts.length >= 5) {
                String stationId = parts[0].trim();
                // 跳过站点ID为99999或0000的记录
                if ("99999".equals(stationId) || "0000".equals(stationId)) {
                    System.out.println("跳过降水数据中的" + stationId + "站点数据");
                    continue;
                }
                
                String[] record = new String[5];
                record[0] = stationId; // sta
                record[1] = parts[1]; // lon
                record[2] = parts[2]; // lat
                record[3] = parts[3]; // ele
                record[4] = parts[4]; // rain1

                records.put(stationId, record);
            }
        }
        return records;
    }

    // 解析相对湿度数据
    private static Map<String, String[]> parseRhData(String data) {
        Map<String, String[]> records = new HashMap<>();
        String[] lines = data.split("\n");

        if (lines.length >= 2) {
            System.out.println("相对湿度文件标头行: " + lines[0]);
            System.out.println("相对湿度文件信息行: " + lines[1]);
            
            // 尝试获取记录数信息
            String[] infoLine = lines[1].trim().split("\\s+");
            if (infoLine.length >= 11) {
                System.out.println("相对湿度文件记录数声明: " + infoLine[10]);
            }
        }

        // 从第三行开始解析数据
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            if (parts.length >= 5) {
                String stationId = parts[0].trim();
                // 跳过站点ID为99999或0000的记录
                if ("99999".equals(stationId) || "0000".equals(stationId)) {
                    System.out.println("跳过相对湿度数据中的" + stationId + "站点数据");
                    continue;
                }
                
                String[] record = new String[5];
                record[0] = stationId; // sta
                record[1] = parts[1]; // lon
                record[2] = parts[2]; // lat
                record[3] = parts[3]; // ele
                record[4] = parts[4]; // rh

                records.put(stationId, record);
            }
        }
        return records;
    }

    // 合并三种数据
    private static List<String[]> mergeData(List<String[]> plotRecords, Map<String, String[]> rainRecords, Map<String, String[]> rhRecords, String timeStr) {
        // 用于存储合并后的数据
        Map<String, String[]> mergedMap = new HashMap<>();
        Set<String> allStations = new HashSet<>();
        
        int plotStationsCount = 0;
        int rainOnlyStationsCount = 0;
        int rhOnlyStationsCount = 0;
        int negativeCoordinateStationsCount = 0;
        
        // 首先处理地面填图数据中的站点
        for (String[] plotRecord : plotRecords) {
            String stationId = plotRecord[0];
            
            // 检查站点ID是否异常
            if (stationId == null || stationId.isEmpty() || "0000".equals(stationId)) {
                System.out.println("警告：跳过无效站点ID：" + (stationId == null ? "null" : stationId));
                continue;
            }
            
            // 检查经纬度是否为负值
            try {
                double lon = Double.parseDouble(plotRecord[1]);
                double lat = Double.parseDouble(plotRecord[2]);
                if (lon < 0 || lat < 0) {
                    System.out.println("跳过负经纬度站点：" + stationId + ", 经度=" + lon + ", 纬度=" + lat);
                    negativeCoordinateStationsCount++;
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("警告：站点" + stationId + "的经纬度解析失败");
            }
            
            allStations.add(stationId);
            plotStationsCount++;

            String[] mergedRecord = new String[30];

            // 设置时间格式
            mergedRecord[0] = timeStr;

            // 复制地面填图数据的所有字段
            for (int i = 0; i < 26; i++) {
                mergedRecord[i + 1] = plotRecord[i];
            }

            // 添加1小时降水数据
            if (rainRecords.containsKey(stationId)) {
                mergedRecord[27] = rainRecords.get(stationId)[4];
            } else {
                mergedRecord[27] = ""; // 空值使用空字符串
            }

            // 添加相对湿度数据
            if (rhRecords.containsKey(stationId)) {
                mergedRecord[28] = rhRecords.get(stationId)[4];
            } else {
                mergedRecord[28] = ""; // 空值使用空字符串
            }

            // 计算p0值(海平面气压+1000)
            try {
                double slp = Double.parseDouble(plotRecord[8]);
                int p0 = (int)(slp + 1000);
                mergedRecord[29] = String.valueOf(p0);
            } catch (NumberFormatException e) {
                mergedRecord[29] = ""; // 空值使用空字符串
            }

            mergedMap.put(stationId, mergedRecord);
        }

        // 处理仅存在于降水数据中的站点
        for (Map.Entry<String, String[]> entry : rainRecords.entrySet()) {
            String stationId = entry.getKey();
            if (!allStations.contains(stationId)) {
                String[] rainRecord = entry.getValue();
                
                // 检查经纬度是否为负值
                try {
                    double lon = Double.parseDouble(rainRecord[1]);
                    double lat = Double.parseDouble(rainRecord[2]);
                    if (lon < 0 || lat < 0) {
                        System.out.println("跳过负经纬度站点：" + stationId + ", 经度=" + lon + ", 纬度=" + lat);
                        negativeCoordinateStationsCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("警告：站点" + stationId + "的经纬度解析失败");
                }
                
                allStations.add(stationId);
                rainOnlyStationsCount++;

                String[] mergedRecord = new String[30];

                // 设置时间
                mergedRecord[0] = timeStr;

                // 添加站点基本信息
                mergedRecord[1] = rainRecord[0]; // sta
                mergedRecord[2] = rainRecord[1]; // lon
                mergedRecord[3] = rainRecord[2]; // lat
                mergedRecord[4] = rainRecord[3]; // ele

                // 地面填图数据字段用空字符串占位
                for (int i = 5; i < 27; i++) {
                    mergedRecord[i] = ""; // 空值使用空字符串
                }

                // 添加降水数据
                mergedRecord[27] = rainRecord[4];

                // 添加湿度数据(如有)
                if (rhRecords.containsKey(stationId)) {
                    mergedRecord[28] = rhRecords.get(stationId)[4];
                } else {
                    mergedRecord[28] = ""; // 空值使用空字符串
                }

                // p0字段占位
                mergedRecord[29] = ""; // 空值使用空字符串

                mergedMap.put(stationId, mergedRecord);
            }
        }

        // 处理仅存在于湿度数据中的站点
        for (Map.Entry<String, String[]> entry : rhRecords.entrySet()) {
            String stationId = entry.getKey();
            if (!allStations.contains(stationId)) {
                String[] rhRecord = entry.getValue();
                
                // 检查经纬度是否为负值
                try {
                    double lon = Double.parseDouble(rhRecord[1]);
                    double lat = Double.parseDouble(rhRecord[2]);
                    if (lon < 0 || lat < 0) {
                        System.out.println("跳过负经纬度站点：" + stationId + ", 经度=" + lon + ", 纬度=" + lat);
                        negativeCoordinateStationsCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("警告：站点" + stationId + "的经纬度解析失败");
                }
                
                allStations.add(stationId);
                rhOnlyStationsCount++;
                
                String[] mergedRecord = new String[30];

                // 设置时间
                mergedRecord[0] = timeStr;

                // 添加站点基本信息
                mergedRecord[1] = rhRecord[0]; // sta
                mergedRecord[2] = rhRecord[1]; // lon
                mergedRecord[3] = rhRecord[2]; // lat
                mergedRecord[4] = rhRecord[3]; // ele

                // 地面填图数据字段用空字符串占位
                for (int i = 5; i < 27; i++) {
                    mergedRecord[i] = ""; // 空值使用空字符串
                }

                // 降水数据字段占位
                mergedRecord[27] = ""; // 空值使用空字符串

                // 添加湿度数据
                mergedRecord[28] = rhRecord[4];

                // p0字段占位
                mergedRecord[29] = ""; // 空值使用空字符串

                mergedMap.put(stationId, mergedRecord);
            }
        }

        System.out.println("地面填图站点数: " + plotStationsCount);
        System.out.println("仅降水数据中的站点数: " + rainOnlyStationsCount);
        System.out.println("仅相对湿度数据中的站点数: " + rhOnlyStationsCount);
        System.out.println("负经纬度站点数（已跳过）: " + negativeCoordinateStationsCount);
        System.out.println("总唯一站点数: " + allStations.size());

        // 对合并数据进行排序：首先是地面填图数据中的站点，保持原顺序
        List<String[]> sortedData = new ArrayList<>();

        // 按地面填图数据的原始顺序添加站点
        for (String[] plotRecord : plotRecords) {
            String stationId = plotRecord[0];
            String[] mergedRecord = mergedMap.get(stationId);
            if (mergedRecord != null) {
                sortedData.add(mergedRecord);
            }
            mergedMap.remove(stationId);
        }

        // 添加其余站点(仅存在于降水或湿度数据中的站点)
        for (String stationId : new ArrayList<>(mergedMap.keySet())) {
            sortedData.add(mergedMap.get(stationId));
        }

        return sortedData;
    }

    /**
     * 从文件名中提取日期时间信息
     * @param fileName 文件名，格式如"24060502.000"
     * @return 日期时间对象，包含年、月、日、时
     */
    private static Map<String, Integer> extractDateTimeFromFileName(String fileName) {
        Map<String, Integer> dateTime = new HashMap<>();
        
        // 移除扩展名
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        
        // 确保文件名符合预期格式（如：24060502）
        if (fileName.length() >= 8) {
            int year = 2000 + Integer.parseInt(fileName.substring(0, 2));
            int month = Integer.parseInt(fileName.substring(2, 4));
            int day = Integer.parseInt(fileName.substring(4, 6));
            int hour = Integer.parseInt(fileName.substring(6, 8));
            
            dateTime.put("year", year);
            dateTime.put("month", month);
            dateTime.put("day", day);
            dateTime.put("hour", hour);
        }
        
        return dateTime;
    }
    
    /**
     * 生成气象一天的文件名
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 格式为YYYYMMDD的文件名
     */
    private static String generateMeteorologicalDayFileName(int year, int month, int day) {
        return String.format("%04d%02d%02d", year, month, day);
    }
    
    /**
     * 按照气象一天处理数据
     * @param plotDirPath 地面填图数据文件夹路径
     * @param rainDirPath 降水数据文件夹路径
     * @param rhDirPath 相对湿度数据文件夹路径
     * @param outputDirPath 输出目录路径
     * @throws IOException 如果文件读写过程中发生错误
     */
    public static void processMeteorologyDays(String plotDirPath, String rainDirPath, String rhDirPath, String outputDirPath) throws IOException {
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);
        
        if (!plotDir.isDirectory() || !rainDir.isDirectory() || !rhDir.isDirectory()) {
            throw new IOException("输入路径必须是目录");
        }
        
        // 获取所有地面填图数据文件并排序
        File[] plotFiles = plotDir.listFiles();
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }
        
        System.out.println("找到 " + plotFiles.length + " 个地面填图数据文件");
        
        // 按文件名排序
        Arrays.sort(plotFiles, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        
        // 按气象一天组织文件
        Map<String, List<File>> meteorologicalDays = new HashMap<>();
        
        // 遍历所有文件，按气象一天分组
        for (File plotFile : plotFiles) {
            String fileName = plotFile.getName();
            
            // 查找对应的降水文件和相对湿度文件
            File rainFile = new File(rainDir, fileName);
            File rhFile = new File(rhDir, fileName);
            
            // 检查三个文件是否都存在
            if (!rainFile.exists() || !rhFile.exists()) {
                System.out.println("跳过文件 " + fileName + "，因为缺少对应的降水或湿度文件");
                continue;
            }
            
            // 从文件名中提取日期时间信息
            Map<String, Integer> dateTime = extractDateTimeFromFileName(fileName);
            
            if (dateTime.isEmpty()) {
                System.out.println("跳过文件 " + fileName + "，因为无法解析日期时间信息");
                continue;
            }
            
            int year = dateTime.get("year");
            int month = dateTime.get("month");
            int day = dateTime.get("day");
            int hour = dateTime.get("hour");
            
            // 确定此文件属于哪个气象一天
            String meteorologicalDay;
            
            if (hour >= 21) {
                // 如果是21点到23点，属于下一天的气象一天
                // 计算下一天的日期
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day); // 月份从0开始
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                
                meteorologicalDay = generateMeteorologicalDayFileName(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1, // 月份从0开始，需要+1
                    calendar.get(Calendar.DAY_OF_MONTH)
                );
            } else if (hour <= 20) {
                // 如果是0点到20点，属于当天的气象一天
                meteorologicalDay = generateMeteorologicalDayFileName(year, month, day);
            } else {
                System.out.println("跳过文件 " + fileName + "，因为小时值异常: " + hour);
                continue;
            }
            
            // 将文件添加到对应的气象一天中
            if (!meteorologicalDays.containsKey(meteorologicalDay)) {
                meteorologicalDays.put(meteorologicalDay, new ArrayList<>());
            }
            meteorologicalDays.get(meteorologicalDay).add(plotFile);
        }
        
        // 检查每个气象一天是否包含完整的24小时数据
        Map<String, List<File>> completeMeteorologyDays = new HashMap<>();
        
        for (Map.Entry<String, List<File>> entry : meteorologicalDays.entrySet()) {
            String day = entry.getKey();
            List<File> files = entry.getValue();
            
            // 检查是否包含足够的文件（气象一天应该有24个小时）
            // 考虑到可能存在的缺失，如果超过20个小时就认为是完整的气象一天
            if (files.size() >= 20) {
                completeMeteorologyDays.put(day, files);
                System.out.println("找到完整的气象一天: " + day + "，包含 " + files.size() + " 个小时的数据");
            } else {
                System.out.println("跳过不完整的气象一天: " + day + "，仅包含 " + files.size() + " 个小时的数据");
            }
        }
        
        // 处理每个完整的气象一天
        for (Map.Entry<String, List<File>> entry : completeMeteorologyDays.entrySet()) {
            String day = entry.getKey();
            List<File> plotFiles_day = entry.getValue();
            
            // 按文件名排序，确保时间顺序
            plotFiles_day.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
            
            // 用于存储所有时次的合并数据
            List<String[]> allMergedData = new ArrayList<>();
            // 用于记录已处理过的时次
            Set<String> processedTimes = new HashSet<>();
            
            System.out.println("处理气象一天: " + day + ", 包含 " + plotFiles_day.size() + " 个小时数据");
            
            for (File plotFile : plotFiles_day) {
                String fileName = plotFile.getName();
                
                // 查找对应的降水文件和相对湿度文件
                File rainFile = new File(rainDir, fileName);
                File rhFile = new File(rhDir, fileName);
                
                // 检查三个文件是否都存在
                if (!rainFile.exists() || !rhFile.exists()) {
                    continue; // 已经在前面过滤过了，这里不应该发生
                }
                
                // 提取时间信息
                String timeStr = extractTimeFromFilePath(plotFile.getAbsolutePath());
                
                // 如果这个时次已经处理过，跳过
                if (processedTimes.contains(timeStr)) {
                    System.out.println("时次 " + timeStr + " 已处理过，跳过");
                    continue;
                }
                
                try {
                    // 读取各文件数据
                    String plotData = FileUtils.readFileToString(plotFile, StandardCharsets.UTF_8);
                    String rainData = FileUtils.readFileToString(rainFile, StandardCharsets.UTF_8);
                    String rhData = FileUtils.readFileToString(rhFile, StandardCharsets.UTF_8);
                    
                    // 解析数据
                    List<String[]> plotRecords = parsePlotData(plotData);
                    Map<String, String[]> rainRecords = parseRainData(rainData);
                    Map<String, String[]> rhRecords = parseRhData(rhData);
                    
                    // 合并当前时次的数据
                    List<String[]> mergedData = mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                    
                    // 将合并后的数据添加到总数据集
                    allMergedData.addAll(mergedData);
                    
                    // 记录已处理的时次
                    processedTimes.add(timeStr);
                    
                    System.out.println("时次 " + timeStr + " 处理完成，添加 " + mergedData.size() + " 条记录");
                } catch (Exception e) {
                    System.err.println("处理文件 " + fileName + " 时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("气象一天 " + day + " 处理完成，共有 " + allMergedData.size() + " 条记录");
            
            // 生成CSV文件
            String outputFilePath = outputDirPath + File.separator + day + ".csv";
            File outputFile = new File(outputFilePath);
            
            // 确保输出目录存在
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            
            try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile), 
                                                ',',              // 分隔符使用逗号
                                                '\0',             // 不使用引号字符
                                                '\\',             // 转义字符
                                                "\n")) {          // 行结束符
                
                // 写入标题行
                String[] header = {"time", "sta", "lon", "lat", "ele", "stalev", "总云量", "风向", "风速", 
                                  "海平面气压", "3小时变压", "过去天气1", "过去天气2", "6小时降水", "低云状", 
                                  "低云量", "低云高", "露点", "能见度", "现在天气", "温度", "中云状", "高云状", 
                                  "标志1", "标志2", "24小时变温", "24小时变压", "rain1", "rh", "p0"};
                writer.writeNext(header);
                
                // 处理数据行：确保空值为空字符串而不是逗号
                for (String[] record : allMergedData) {
                    if (record == null) continue; // 跳过null记录
                    // 处理数据中的空值，将","替换为空字符串
                    for (int i = 0; i < record.length; i++) {
                        if (record[i] != null && record[i].equals(",")) {
                            record[i] = ""; // 使用空字符串代替","
                        }
                    }
                    writer.writeNext(record);
                }
            }
            
            System.out.println("气象一天 " + day + " 数据已写入文件: " + outputFilePath);
        }
        
        System.out.println("所有气象一天处理完成，共生成 " + completeMeteorologyDays.size() + " 个文件");
    }

    /**
     * 处理文件夹中的多个时次数据，将结果合并到单个CSV文件中
     * @param plotDirPath 地面填图数据文件夹路径
     * @param rainDirPath 降水数据文件夹路径
     * @param rhDirPath 相对湿度数据文件夹路径
     * @param outputFilePath 输出CSV文件路径
     * @throws IOException 如果文件读写过程中发生错误
     */
    public static void processMultipleTimeData(String plotDirPath, String rainDirPath, String rhDirPath, String outputFilePath) throws IOException {
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);
        
        if (!plotDir.isDirectory() || !rainDir.isDirectory() || !rhDir.isDirectory()) {
            throw new IOException("输入路径必须是目录");
        }
        
        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles();
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }
        
        System.out.println("找到 " + plotFiles.length + " 个地面填图数据文件");
        
        // 用于存储所有时次的合并数据
        List<String[]> allMergedData = new ArrayList<>();
        // 用于记录已处理过的时次
        Set<String> processedTimes = new HashSet<>();
        // 记录标题行
        String[] header = {"time", "sta", "lon", "lat", "ele", "stalev", "总云量", "风向", "风速", 
                          "海平面气压", "3小时变压", "过去天气1", "过去天气2", "6小时降水", "低云状", 
                          "低云量", "低云高", "露点", "能见度", "现在天气", "温度", "中云状", "高云状", 
                          "标志1", "标志2", "24小时变温", "24小时变压", "rain1", "rh", "p0"};
        
        // 处理每个地面填图数据文件
        for (File plotFile : plotFiles) {
            String fileName = plotFile.getName();
            
            // 查找对应的降水文件和相对湿度文件
            File rainFile = new File(rainDir, fileName);
            File rhFile = new File(rhDir, fileName);
            
            // 检查三个文件是否都存在
            if (rainFile.exists() && rhFile.exists()) {
                System.out.println("处理时次: " + fileName);
                
                // 提取时间信息
                String timeStr = extractTimeFromFilePath(plotFile.getAbsolutePath());
                
                // 如果这个时次已经处理过，跳过
                if (processedTimes.contains(timeStr)) {
                    System.out.println("时次 " + timeStr + " 已处理过，跳过");
                    continue;
                }
                
                try {
                    // 读取各文件数据
                    String plotData = FileUtils.readFileToString(plotFile, StandardCharsets.UTF_8);
                    String rainData = FileUtils.readFileToString(rainFile, StandardCharsets.UTF_8);
                    String rhData = FileUtils.readFileToString(rhFile, StandardCharsets.UTF_8);
                    
                    // 解析数据
                    List<String[]> plotRecords = parsePlotData(plotData);
                    Map<String, String[]> rainRecords = parseRainData(rainData);
                    Map<String, String[]> rhRecords = parseRhData(rhData);
                    
                    // 合并当前时次的数据
                    List<String[]> mergedData = mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                    
                    // 将合并后的数据添加到总数据集
                    allMergedData.addAll(mergedData);
                    
                    // 记录已处理的时次
                    processedTimes.add(timeStr);
                    
                    System.out.println("时次 " + timeStr + " 处理完成，添加 " + mergedData.size() + " 条记录");
                } catch (Exception e) {
                    System.err.println("处理文件 " + fileName + " 时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("跳过文件 " + fileName + "，因为缺少对应的降水或湿度文件");
            }
        }
        
        System.out.println("所有时次处理完成，共有 " + allMergedData.size() + " 条记录");
        
        // 生成CSV文件
        File outputFile = new File(outputFilePath);
        
        // 确保输出目录存在
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile), 
                                            ',',              // 分隔符使用逗号
                                            '\0',             // 不使用引号字符
                                            '\\',             // 转义字符
                                            "\n")) {          // 行结束符
            
            // 写入标题行
            writer.writeNext(header);
            
            // 处理数据行：确保空值为空字符串而不是逗号
            for (String[] record : allMergedData) {
                if (record == null) continue; // 跳过null记录
                // 处理数据中的空值，将","替换为空字符串
                for (int i = 0; i < record.length; i++) {
                    if (record[i] != null && record[i].equals(",")) {
                        record[i] = ""; // 使用空字符串代替","
                    }
                }
                writer.writeNext(record);
            }
        }
        
        System.out.println("多时次数据合并完成，输出文件: " + outputFilePath);
    }
} 