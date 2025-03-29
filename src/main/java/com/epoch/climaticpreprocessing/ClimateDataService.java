package com.epoch.climaticpreprocessing;

import java.io.IOException;

/**
 * 气象数据处理服务
 * 提供气象数据合并功能的服务类
 */
public class ClimateDataService {
    
    /**
     * 合并气象数据文件
     * 
     * @param plotFilePath 地面填图数据文件路径
     * @param rainFilePath 降水数据文件路径
     * @param rhFilePath 相对湿度数据文件路径
     * @param outputFilePath 输出CSV文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void mergeMeteorologicalData(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath) throws IOException {
        // 调用数据合并处理工具类
        MeteorologyDataMerger.mergeAndProcessData(plotFilePath, rainFilePath, rhFilePath, outputFilePath);
    }
    
    /**
     * 使用硬编码路径进行测试
     * 
     * @return 生成的CSV文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public String processSampleData() throws IOException {
        // 假设这些路径是实际存在的，真实使用时会被替换为前端传递的路径
        String plotFilePath = "E:/data/plot.txt";
        String rainFilePath = "E:/data/rain1-p.txt";
        String rhFilePath = "E:/data/rh-p.txt";
        String outputFilePath = "E:/data/output/merged_data.csv";
        
        // 调用数据合并处理方法
        mergeMeteorologicalData(plotFilePath, rainFilePath, rhFilePath, outputFilePath);
        
        return outputFilePath;
    }
} 