package com.epoch.climaticpreprocessing.util;

import com.epoch.climaticpreprocessing.domain.po.PlotData;
import com.epoch.climaticpreprocessing.domain.po.RainData;
import com.epoch.climaticpreprocessing.domain.po.RhData;

import java.util.*;

/**
 * 数据解析器工具类
 * 负责解析各种气象数据文件
 */
public class DataParser {
    
    /**
     * 解析地面填图数据
     * 
     * @param data 原始数据字符串
     * @return 解析后的地面填图数据列表
     */
    public static List<PlotData> parsePlotData(String data) {
        List<PlotData> records = new ArrayList<>();
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
                    records.add(new PlotData(record));
                }
            }
        }

        // 检查是否有重复站点
        Set<String> uniqueStations = new HashSet<>();
        for (PlotData record : records) {
            String stationId = record.getStationId();
            if (uniqueStations.contains(stationId)) {
                System.out.println("发现重复站点ID: " + stationId);
            }
            uniqueStations.add(stationId);
        }
        System.out.println("地面填图数据中唯一站点数: " + uniqueStations.size());

        return records;
    }

    /**
     * 解析单条地面填图记录
     * 
     * @param line1 第一行数据（站号、经纬度等基本信息和部分气象参数）
     * @param line2 第二行数据（降水、云状等其他参数）
     * @return 解析后的记录数组
     */
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

    /**
     * 解析降水数据
     * 
     * @param data 原始数据字符串
     * @return 解析后的降水数据Map，以站点ID为键
     */
    public static Map<String, RainData> parseRainData(String data) {
        Map<String, RainData> records = new HashMap<>();
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

                records.put(stationId, new RainData(record));
            }
        }
        return records;
    }

    /**
     * 解析相对湿度数据
     * 
     * @param data 原始数据字符串
     * @return 解析后的相对湿度数据Map，以站点ID为键
     */
    public static Map<String, RhData> parseRhData(String data) {
        Map<String, RhData> records = new HashMap<>();
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

                records.put(stationId, new RhData(record));
            }
        }
        return records;
    }
} 