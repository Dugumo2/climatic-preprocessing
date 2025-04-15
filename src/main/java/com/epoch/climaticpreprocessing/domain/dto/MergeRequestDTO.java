package com.epoch.climaticpreprocessing.domain.dto;

import com.epoch.climaticpreprocessing.domain.enums.TimeScale;

/**
 * 合并请求DTO类
 * 用于接收前端传入的合并请求参数
 */
public class MergeRequestDTO {
    private String plotFilePath;     // 地面填图数据文件路径
    private String rainFilePath;     // 降水数据文件路径
    private String rhFilePath;       // 相对湿度数据文件路径
    private String outputFilePath;   // 输出CSV文件路径
    private String plotDirPath;      // 地面填图数据目录路径
    private String rainDirPath;      // 降水数据目录路径
    private String rhDirPath;        // 相对湿度数据目录路径
    private String outputDirPath;    // 输出目录路径
    private TimeScale timeScale;     // 时间尺度（可选）
    
    // 构造函数
    public MergeRequestDTO() {
    }
    
    public MergeRequestDTO(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath) {
        this.plotFilePath = plotFilePath;
        this.rainFilePath = rainFilePath;
        this.rhFilePath = rhFilePath;
        this.outputFilePath = outputFilePath;
    }
    
    // 带时间尺度的文件路径构造函数
    public MergeRequestDTO(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath, TimeScale timeScale) {
        this(plotFilePath, rainFilePath, rhFilePath, outputFilePath);
        this.timeScale = timeScale;
    }
    
    // 带时间尺度的目录路径构造函数
    public MergeRequestDTO(String plotDirPath, String rainDirPath, String rhDirPath, String outputDirPath, TimeScale timeScale, boolean isDirectory) {
        this.plotDirPath = plotDirPath;
        this.rainDirPath = rainDirPath;
        this.rhDirPath = rhDirPath;
        this.outputDirPath = outputDirPath;
        this.timeScale = timeScale;
    }
    
    // Getters和Setters
    public String getPlotFilePath() {
        return plotFilePath;
    }
    
    public void setPlotFilePath(String plotFilePath) {
        this.plotFilePath = plotFilePath;
    }
    
    public String getRainFilePath() {
        return rainFilePath;
    }
    
    public void setRainFilePath(String rainFilePath) {
        this.rainFilePath = rainFilePath;
    }
    
    public String getRhFilePath() {
        return rhFilePath;
    }
    
    public void setRhFilePath(String rhFilePath) {
        this.rhFilePath = rhFilePath;
    }
    
    public String getOutputFilePath() {
        return outputFilePath;
    }
    
    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }
    
    public String getPlotDirPath() {
        return plotDirPath;
    }
    
    public void setPlotDirPath(String plotDirPath) {
        this.plotDirPath = plotDirPath;
    }
    
    public String getRainDirPath() {
        return rainDirPath;
    }
    
    public void setRainDirPath(String rainDirPath) {
        this.rainDirPath = rainDirPath;
    }
    
    public String getRhDirPath() {
        return rhDirPath;
    }
    
    public void setRhDirPath(String rhDirPath) {
        this.rhDirPath = rhDirPath;
    }
    
    public String getOutputDirPath() {
        return outputDirPath;
    }
    
    public void setOutputDirPath(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }
    
    public TimeScale getTimeScale() {
        return timeScale;
    }
    
    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }
} 