package com.epoch.climaticpreprocessing.service;

import com.epoch.climaticpreprocessing.domain.dto.MergeRequestDTO;
import com.epoch.climaticpreprocessing.domain.enums.TimeScale;
import com.epoch.climaticpreprocessing.domain.po.MeteorologyData;
import com.epoch.climaticpreprocessing.domain.po.PlotData;
import com.epoch.climaticpreprocessing.domain.po.RainData;
import com.epoch.climaticpreprocessing.domain.po.RhData;
import com.epoch.climaticpreprocessing.util.*;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 气候数据处理服务
 */
@Service
public class ClimateDataService {

    /**
     * 合并气象数据
     *
     * @param plotFilePath 地面填图数据文件路径
     * @param rainFilePath 降水数据文件路径
     * @param rhFilePath 相对湿度数据文件路径
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void mergeMeteorologicalData(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath) throws IOException {
        // 使用MeteorologyDataMerger工具类处理数据合并
        MeteorologyDataMerger.mergeAndProcessData(plotFilePath, rainFilePath, rhFilePath, outputFilePath);
    }

    /**
     * 处理合并请求
     *
     * @param request 合并请求DTO
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void processMergeRequest(MergeRequestDTO request) throws IOException {
        if (request.getTimeScale() != null) {
            // 判断是目录路径处理还是文件路径处理
            if (request.getPlotDirPath() != null && !request.getPlotDirPath().isEmpty()) {
                // 按时间尺度处理数据（目录路径）
                processDataByTimeScale(
                    new File(request.getPlotDirPath()),
                    new File(request.getRainDirPath()),
                    new File(request.getRhDirPath()),
                    new File(request.getOutputDirPath()),
                    request.getTimeScale()
                );
            } else if (request.getPlotFilePath() != null && !request.getPlotFilePath().isEmpty()) {
                // 按时间尺度处理数据（文件路径，输出到目录）
                File outputDir = new File(request.getOutputFilePath()).getParentFile();
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                
                // 直接处理单个数据集
                mergeMeteorologicalData(
                    request.getPlotFilePath(),
                    request.getRainFilePath(),
                    request.getRhFilePath(),
                    request.getOutputFilePath()
                );
            } else {
                throw new IOException("必须提供文件路径或目录路径");
            }
        } else {
            // 直接处理单个数据集
            mergeMeteorologicalData(
                request.getPlotFilePath(),
                request.getRainFilePath(),
                request.getRhFilePath(),
                request.getOutputFilePath()
            );
        }
    }

    /**
     * 直接使用底层工具类进行数据处理的示例
     *
     * @param plotFilePath 地面填图数据文件路径
     * @param rainFilePath 降水数据文件路径
     * @param rhFilePath 相对湿度数据文件路径
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void processDataDirectly(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath) throws IOException {
        // 读取文件内容
        String plotData = FileUtils.readFileToString(new File(plotFilePath), StandardCharsets.UTF_8);
        String rainData = FileUtils.readFileToString(new File(rainFilePath), StandardCharsets.UTF_8);
        String rhData = FileUtils.readFileToString(new File(rhFilePath), StandardCharsets.UTF_8);

        // 提取时间信息
        String timeStr = FileUtil.extractTimeFromFilePath(plotFilePath);
        
        // 使用DataParser解析数据
        List<PlotData> plotRecords = DataParser.parsePlotData(plotData);
        Map<String, RainData> rainRecords = DataParser.parseRainData(rainData);
        Map<String, RhData> rhRecords = DataParser.parseRhData(rhData);
        
        // 使用DataMerger合并数据
        List<MeteorologyData> mergedData = DataMerger.mergeData(plotRecords, rainRecords, rhRecords, timeStr);
        
        // 使用CsvExporter导出CSV
        CsvExporter.exportMeteorologyDataToCsv(mergedData, outputFilePath);
    }

    /**
     * 按照指定时间尺度处理气象数据
     *
     * @param plotDir 地面填图数据目录
     * @param rainDir 降水数据目录
     * @param rhDir 相对湿度数据目录
     * @param outputDir 输出目录
     * @param timeScale 时间尺度
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void processDataByTimeScale(File plotDir, File rainDir, File rhDir, File outputDir, TimeScale timeScale) throws IOException {
        MeteorologyDataMerger.processMeteorologyByTimeScale(plotDir, rainDir, rhDir, outputDir, timeScale);
    }

    /**
     * 处理所有时间尺度的数据
     *
     * @param plotDir 地面填图数据目录
     * @param rainDir 降水数据目录
     * @param rhDir 相对湿度数据目录
     * @param outputDir 输出目录
     * @throws IOException 如果文件处理过程中发生错误
     */
    public void processAllTimeScales(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        // 遍历所有时间尺度并处理
        for (TimeScale timeScale : TimeScale.values()) {
            File timeScaleOutputDir = new File(outputDir, timeScale.getDescription());
            if (!timeScaleOutputDir.exists()) {
                timeScaleOutputDir.mkdirs();
            }
            
            processDataByTimeScale(plotDir, rainDir, rhDir, timeScaleOutputDir, timeScale);
        }
    }
    
    /**
     * 获取所有时间尺度目录列表
     * 
     * @param baseDir 基础目录
     * @return 包含所有时间尺度子目录的Map，键为时间尺度，值为对应的目录File对象
     */
    public Map<TimeScale, File> getAllTimeScaleDirectories(File baseDir) {
        Map<TimeScale, File> timeScaleDirs = new HashMap<>();
        
        for (TimeScale timeScale : TimeScale.values()) {
            File timeScaleDir = new File(baseDir, timeScale.getDescription());
            timeScaleDirs.put(timeScale, timeScaleDir);
        }
        
        return timeScaleDirs;
    }
} 