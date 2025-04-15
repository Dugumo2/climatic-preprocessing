package com.epoch.climaticpreprocessing.service;

import com.epoch.climaticpreprocessing.util.MeteorologyDataMerger;

import java.io.File;
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

    /**
     * 合并多个时次的气象数据
     * 
     * @param plotDirPath 地面填图数据文件夹路径
     * @param rainDirPath 降水数据文件夹路径
     * @param rhDirPath 相对湿度数据文件夹路径
     * @param outputFilePath 输出CSV文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void mergeMultipleTimeData(String plotDirPath, String rainDirPath, String rhDirPath, String outputFilePath) throws IOException {
        // 调用多时次数据合并处理方法
        MeteorologyDataMerger.processMultipleTimeData(plotDirPath, rainDirPath, rhDirPath, outputFilePath);
    }
    
    /**
     * 使用硬编码路径进行多时次数据合并测试
     * 
     * @return 生成的CSV文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public String processMultipleTimeSampleData() throws IOException {
        // 假设这些路径是实际存在的，真实使用时会被替换为前端传递的路径
        String plotDirPath = "E:/data/plot";
        String rainDirPath = "E:/data/rain1-p";
        String rhDirPath = "E:/data/rh-p";
        String outputFilePath = "E:/data/output/merged_multiple_time_data.csv";
        
        // 调用多时次数据合并处理方法
        mergeMultipleTimeData(plotDirPath, rainDirPath, rhDirPath, outputFilePath);
        
        return outputFilePath;
    }
    
    /**
     * 按照气象一天合并气象数据
     * 
     * @param plotDirPath 地面填图数据文件夹路径
     * @param rainDirPath 降水数据文件夹路径
     * @param rhDirPath 相对湿度数据文件夹路径
     * @param outputDirPath 输出目录路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void mergeMeteorologyDays(String plotDirPath, String rainDirPath, String rhDirPath, String outputDirPath) throws IOException {
        // 调用气象一天数据合并处理方法
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);
        File outputDir = new File(outputDirPath);
        
        // 使用更新后的processMeteorologyDays方法
        MeteorologyDataMerger.processMeteorologyDays(plotDir, rainDir, rhDir, outputDir);
    }
    
    /**
     * 使用硬编码路径进行气象一天数据合并测试
     * 
     * @return 生成的CSV文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public String processMeteorologyDaysSampleData() throws IOException {
        // 假设这些路径是实际存在的，真实使用时会被替换为前端传递的路径
        String plotDirPath = "E:/data/plot";
        String rainDirPath = "E:/data/rain1-p";
        String rhDirPath = "E:/data/rh-p";
        String outputDirPath = "E:/data/output/meteorology_days";
        
        // 调用气象一天数据合并处理方法
        mergeMeteorologyDays(plotDirPath, rainDirPath, rhDirPath, outputDirPath);
        
        return outputDirPath;
    }
    
    /**
     * 按照指定时间尺度处理气象数据
     * 
     * @param plotDirPath 地面填图数据文件夹路径
     * @param rainDirPath 降水数据文件夹路径
     * @param rhDirPath 相对湿度数据文件夹路径
     * @param outputDirPath 输出目录路径
     * @param timeScale 时间尺度
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void processMeteorologyByTimeScale(String plotDirPath, String rainDirPath, String rhDirPath, 
                                              String outputDirPath, MeteorologyDataMerger.TimeScale timeScale) throws IOException {
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);
        File outputDir = new File(outputDirPath);
        
        // 调用按时间尺度处理数据的方法
        MeteorologyDataMerger.processMeteorologyByTimeScale(plotDir, rainDir, rhDir, outputDir, timeScale);
    }
    
    /**
     * 处理所有时间尺度的气象数据
     * 
     * @param plotDirPath 地面填图数据文件夹路径
     * @param rainDirPath 降水数据文件夹路径
     * @param rhDirPath 相对湿度数据文件夹路径
     * @param outputDirPath 输出根目录路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void processAllTimeScales(String plotDirPath, String rainDirPath, String rhDirPath, String outputDirPath) throws IOException {
        // 处理所有时间尺度的数据
        for (MeteorologyDataMerger.TimeScale timeScale : MeteorologyDataMerger.TimeScale.values()) {
            // 为每个时间尺度创建单独的输出目录
            String scaleDirPath = outputDirPath + "/" + timeScale.name().toLowerCase();
            File scaleDir = new File(scaleDirPath);
            if (!scaleDir.exists()) {
                scaleDir.mkdirs();
            }
            
            // 处理该时间尺度的数据
            processMeteorologyByTimeScale(plotDirPath, rainDirPath, rhDirPath, scaleDirPath, timeScale);
        }
    }
    
    /**
     * 使用硬编码路径处理所有时间尺度的数据
     * 
     * @return 输出目录路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public String processAllTimeScalesSampleData() throws IOException {
        // 假设这些路径是实际存在的，真实使用时会被替换为前端传递的路径
        String plotDirPath = "E:/data/plot";
        String rainDirPath = "E:/data/rain1-p";
        String rhDirPath = "E:/data/rh-p";
        String outputDirPath = "E:/data/output/all_scales";
        
        // 处理所有时间尺度
        processAllTimeScales(plotDirPath, rainDirPath, rhDirPath, outputDirPath);
        
        return outputDirPath;
    }
} 