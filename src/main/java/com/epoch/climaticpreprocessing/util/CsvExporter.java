package com.epoch.climaticpreprocessing.util;

import com.epoch.climaticpreprocessing.domain.po.MeteorologyData;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * CSV导出工具类
 * 负责将气象数据导出为CSV文件
 */
public class CsvExporter {
    
    /**
     * CSV文件列标题
     */
    private static final String[] CSV_HEADER = {
        "time", "sta", "lon", "lat", "ele", "stalev", "总云量", "风向", "风速", 
        "海平面气压", "3小时变压", "过去天气1", "过去天气2", "6小时降水", "低云状", 
        "低云量", "低云高", "露点", "能见度", "现在天气", "温度", "中云状", "高云状", 
        "标志1", "标志2", "24小时变温", "24小时变压", "rain1", "rh", "p0"
    };
    
    /**
     * 导出气象数据到CSV文件
     * 
     * @param meteorologyDataList 气象数据列表
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果文件写入过程中发生错误
     */
    public static void exportMeteorologyDataToCsv(List<MeteorologyData> meteorologyDataList, String outputFilePath) throws IOException {
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
            writer.writeNext(CSV_HEADER);
            
            // 处理数据行
            for (MeteorologyData data : meteorologyDataList) {
                if (data == null) continue; // 跳过null记录
                
                String[] record = data.toArray();
                
                // 处理数据中的空值，将","替换为空字符串
                for (int i = 0; i < record.length; i++) {
                    if (record[i] == null || record[i].equals(",")) {
                        record[i] = ""; // 使用空字符串代替","或null
                    }
                }
                
                writer.writeNext(record);
            }
        }
    }
    
    /**
     * 导出原始字符串数组数据到CSV文件
     * 用于兼容旧版本代码
     * 
     * @param mergedData 合并后的数据（字符串数组列表）
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果文件写入过程中发生错误
     */
    public static void exportStringArrayToCsv(List<String[]> mergedData, String outputFilePath) throws IOException {
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
            writer.writeNext(CSV_HEADER);
            
            // 处理数据行
            for (String[] record : mergedData) {
                if (record == null) continue; // 跳过null记录
                
                // 处理数据中的空值，将","替换为空字符串
                for (int i = 0; i < record.length; i++) {
                    if (record[i] == null || record[i].equals(",")) {
                        record[i] = ""; // 使用空字符串代替","或null
                    }
                }
                
                writer.writeNext(record);
            }
        }
    }
} 