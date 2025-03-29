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
        record[0] = parts1[0]; // sta
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

        // 解析第二行（降水、云状等其他参数）
        String[] parts2 = line2.trim().split("\\s+");
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
                String stationId = parts[0];
                // 跳过站点ID为99999的记录
                if ("99999".equals(stationId)) {
                    System.out.println("跳过降水数据中的99999站点数据");
                    continue;
                }
                
                String[] record = new String[5];
                record[0] = parts[0]; // sta
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
                String stationId = parts[0];
                // 跳过站点ID为99999的记录
                if ("99999".equals(stationId)) {
                    System.out.println("跳过相对湿度数据中的99999站点数据");
                    continue;
                }
                
                String[] record = new String[5];
                record[0] = parts[0]; // sta
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
        
        // 首先处理地面填图数据中的站点
        for (String[] plotRecord : plotRecords) {
            String stationId = plotRecord[0];
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
                allStations.add(stationId);
                rainOnlyStationsCount++;

                String[] rainRecord = entry.getValue();
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
                allStations.add(stationId);
                rhOnlyStationsCount++;
                
                String[] rhRecord = entry.getValue();
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
} 