package com.epoch.climaticpreprocessing.util;

import com.epoch.climaticpreprocessing.domain.dto.FileInfo;
import com.epoch.climaticpreprocessing.domain.enums.TimeScale;
import com.epoch.climaticpreprocessing.domain.po.MeteorologyData;
import com.epoch.climaticpreprocessing.domain.po.PlotData;
import com.epoch.climaticpreprocessing.domain.po.RainData;
import com.epoch.climaticpreprocessing.domain.po.RhData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 气象数据合并工具类
 * 用于合并不同来源的气象数据并输出为CSV格式
 */
public class MeteorologyDataMerger {

    /**
     * 合并并处理气象数据
     *
     * @param plotFilePath  地面填图数据文件路径
     * @param rainFilePath  降水数据文件路径
     * @param rhFilePath    相对湿度数据文件路径
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果文件操作出错
     */
    public static void mergeAndProcessData(String plotFilePath, String rainFilePath, String rhFilePath, String outputFilePath) throws IOException {
        // 读取文件内容
        String plotData = FileUtils.readFileToString(new File(plotFilePath), StandardCharsets.UTF_8);
        String rainData = FileUtils.readFileToString(new File(rainFilePath), StandardCharsets.UTF_8);
        String rhData = FileUtils.readFileToString(new File(rhFilePath), StandardCharsets.UTF_8);

        // 提取时间信息
        String timeStr = FileUtil.extractTimeFromFilePath(plotFilePath);
        System.out.println("提取的时间: " + timeStr);

        // 解析各类数据
        List<PlotData> plotRecords = DataParser.parsePlotData(plotData);
        Map<String, RainData> rainRecords = DataParser.parseRainData(rainData);
        Map<String, RhData> rhRecords = DataParser.parseRhData(rhData);

        // 打印数据记录数量用于调试
        System.out.println("地面填图数据记录数: " + plotRecords.size());
        System.out.println("降水数据记录数: " + rainRecords.size());
        System.out.println("相对湿度数据记录数: " + rhRecords.size());

        // 合并数据
        List<MeteorologyData> mergedData = DataMerger.mergeData(plotRecords, rainRecords, rhRecords, timeStr);
        System.out.println("合并后总记录数: " + mergedData.size());

        // 导出合并后的数据到CSV文件
        CsvExporter.exportMeteorologyDataToCsv(mergedData, outputFilePath);
        System.out.println("数据已成功合并并导出到: " + outputFilePath);
    }

    /**
     * 处理多时次的气象数据
     *
     * @param plotDirPath  地面填图数据目录路径
     * @param rainDirPath  降水数据目录路径
     * @param rhDirPath    相对湿度数据目录路径
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果文件操作出错
     */
    public static void processMultipleTimeData(String plotDirPath, String rainDirPath, String rhDirPath, String outputFilePath) throws IOException {
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);

        if (!plotDir.isDirectory() || !rainDir.isDirectory() || !rhDir.isDirectory()) {
            throw new IOException("提供的路径不是有效目录");
        }

        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 创建文件配对并处理
        List<MeteorologyData> allMergedData = new ArrayList<>();
        for (File plotFile : plotFiles) {
            String timeStr = FileUtil.extractTimeFromFilePath(plotFile.getAbsolutePath());
            
            // 根据时间匹配对应的降水和相对湿度文件
            File matchingRainFile = findMatchingFile(rainDir, timeStr);
            File matchingRhFile = findMatchingFile(rhDir, timeStr);
            
            if (matchingRainFile != null && matchingRhFile != null) {
                try {
                    // 读取文件内容
                    String plotData = FileUtils.readFileToString(plotFile, StandardCharsets.UTF_8);
                    String rainData = FileUtils.readFileToString(matchingRainFile, StandardCharsets.UTF_8);
                    String rhData = FileUtils.readFileToString(matchingRhFile, StandardCharsets.UTF_8);
                    
                    // 解析数据
                    List<PlotData> plotRecords = DataParser.parsePlotData(plotData);
                    Map<String, RainData> rainRecords = DataParser.parseRainData(rainData);
                    Map<String, RhData> rhRecords = DataParser.parseRhData(rhData);
                    
                    // 合并数据
                    List<MeteorologyData> timePointData = DataMerger.mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                    allMergedData.addAll(timePointData);
                    
                    System.out.println("成功处理时间点: " + timeStr + ", 记录数: " + timePointData.size());
                } catch (Exception e) {
                    System.err.println("处理时间点 " + timeStr + " 时出错: " + e.getMessage());
                }
            } else {
                System.out.println("跳过时间点 " + timeStr + ": 未找到匹配的降水或相对湿度文件");
            }
        }
        
        // 导出所有合并后的数据到CSV文件
        CsvExporter.exportMeteorologyDataToCsv(allMergedData, outputFilePath);
        System.out.println("所有时间点的数据已成功合并并导出到: " + outputFilePath + ", 总记录数: " + allMergedData.size());
    }

    /**
     * 根据时间戳查找匹配的文件
     *
     * @param directory 搜索的目录
     * @param timeStr   时间字符串
     * @return 匹配的文件，如果未找到则返回null
     */
    private static File findMatchingFile(File directory, String timeStr) {
        File[] files = directory.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".txt") && name.contains(timeStr));
        
        return (files != null && files.length > 0) ? files[0] : null;
    }

    /**
     * 按气象一天处理数据
     *
     * @param plotDir    地面填图数据目录
     * @param rainDir    降水数据目录
     * @param rhDir      相对湿度数据目录
     * @param outputDir  输出目录
     * @throws IOException 如果文件操作出错
     */
    public static void processMeteorologyDays(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("无法创建输出目录: " + outputDir.getAbsolutePath());
        }

        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 按日期分组文件
        Map<String, List<FileInfo>> filesByDate = new HashMap<>();
        for (File plotFile : plotFiles) {
            String filePath = plotFile.getAbsolutePath();
            String timeStr = FileUtil.extractTimeFromFilePath(filePath);
            
            // 从时间字符串中提取日期部分（前8位，例如20230101）
            String dateStr = timeStr.substring(0, 8);
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setTimeStr(timeStr);
            
            filesByDate.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(fileInfo);
        }

        // 处理每一天的数据
        for (Map.Entry<String, List<FileInfo>> entry : filesByDate.entrySet()) {
            String dateStr = entry.getKey();
            List<FileInfo> filesForDay = entry.getValue();
            
            // 创建该日期的输出文件
            String outputPath = outputDir.getAbsolutePath() + File.separator + "meteorology_" + dateStr + ".csv";
            
            // 处理当天的所有时间点
            List<MeteorologyData> allDayData = new ArrayList<>();
            for (FileInfo plotFileInfo : filesForDay) {
                String timeStr = plotFileInfo.getTimeStr();
                
                // 寻找匹配的降水和相对湿度文件
                File matchingRainFile = findMatchingFile(rainDir, timeStr);
                File matchingRhFile = findMatchingFile(rhDir, timeStr);
                
                if (matchingRainFile != null && matchingRhFile != null) {
                    try {
                        // 读取文件内容
                        String plotData = FileUtils.readFileToString(new File(plotFileInfo.getFilePath()), StandardCharsets.UTF_8);
                        String rainData = FileUtils.readFileToString(matchingRainFile, StandardCharsets.UTF_8);
                        String rhData = FileUtils.readFileToString(matchingRhFile, StandardCharsets.UTF_8);
                        
                        // 解析数据
                        List<PlotData> plotRecords = DataParser.parsePlotData(plotData);
                        Map<String, RainData> rainRecords = DataParser.parseRainData(rainData);
                        Map<String, RhData> rhRecords = DataParser.parseRhData(rhData);
                        
                        // 合并数据
                        List<MeteorologyData> timePointData = DataMerger.mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                        allDayData.addAll(timePointData);
                        
                        System.out.println("日期 " + dateStr + " 的时间点 " + timeStr + " 处理完成, 记录数: " + timePointData.size());
                    } catch (Exception e) {
                        System.err.println("处理日期 " + dateStr + " 的时间点 " + timeStr + " 时出错: " + e.getMessage());
                    }
                } else {
                    System.out.println("日期 " + dateStr + " 的时间点 " + timeStr + " 跳过: 未找到匹配的降水或相对湿度文件");
                }
            }
            
            // 导出当天的所有数据到CSV文件
            CsvExporter.exportMeteorologyDataToCsv(allDayData, outputPath);
            System.out.println("日期 " + dateStr + " 的所有数据已成功合并并导出到: " + outputPath + ", 总记录数: " + allDayData.size());
        }
    }

    /**
     * 按指定时间尺度处理气象数据
     *
     * @param plotDir    地面填图数据目录
     * @param rainDir    降水数据目录
     * @param rhDir      相对湿度数据目录
     * @param outputDir  输出目录
     * @param timeScale  时间尺度
     * @throws IOException 如果文件操作出错
     */
    public static void processMeteorologyByTimeScale(File plotDir, File rainDir, File rhDir, File outputDir, TimeScale timeScale) throws IOException {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("无法创建输出目录: " + outputDir.getAbsolutePath());
        }

        switch (timeScale) {
            case ALL:
                processAllData(plotDir, rainDir, rhDir, outputDir);
                break;
            case DAY:
                processMeteorologyDays(plotDir, rainDir, rhDir, outputDir);
                break;
            case PENTAD:
                processPentadData(plotDir, rainDir, rhDir, outputDir);
                break;
            case DEKAD:
                processDekadData(plotDir, rainDir, rhDir, outputDir);
                break;
            case MONTH:
                processMonthlyData(plotDir, rainDir, rhDir, outputDir);
                break;
            case SEASON:
                processSeasonalData(plotDir, rainDir, rhDir, outputDir);
                break;
            case YEAR:
                processYearlyData(plotDir, rainDir, rhDir, outputDir);
                break;
            default:
                throw new IllegalArgumentException("不支持的时间尺度: " + timeScale);
        }
    }

    /**
     * 处理所有数据（不按时间尺度分组）
     */
    private static void processAllData(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        String outputPath = outputDir.getAbsolutePath() + File.separator + "meteorology_all.csv";
        processMultipleTimeData(plotDir.getAbsolutePath(), rainDir.getAbsolutePath(), rhDir.getAbsolutePath(), outputPath);
    }

    /**
     * 处理五日数据
     */
    private static void processPentadData(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 按五日分组
        Map<String, List<FileInfo>> filesByPentad = new HashMap<>();
        for (File plotFile : plotFiles) {
            String filePath = plotFile.getAbsolutePath();
            String timeStr = FileUtil.extractTimeFromFilePath(filePath);
            
            // 从时间字符串中提取年月日
            LocalDate date = LocalDate.parse(timeStr.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 确定五日期（每月1-5, 6-10, 11-15, 16-20, 21-25, 26-30/31）
            int day = date.getDayOfMonth();
            int pentadIndex;
            if (day <= 5) pentadIndex = 1;
            else if (day <= 10) pentadIndex = 2;
            else if (day <= 15) pentadIndex = 3;
            else if (day <= 20) pentadIndex = 4;
            else if (day <= 25) pentadIndex = 5;
            else pentadIndex = 6;
            
            // 形成五日键: yyyyMM-p (p是五日索引)
            String pentadKey = date.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-" + pentadIndex;
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setTimeStr(timeStr);
            
            filesByPentad.computeIfAbsent(pentadKey, k -> new ArrayList<>()).add(fileInfo);
        }

        // 处理每个五日期的数据
        processByTimeGroup(filesByPentad, rainDir, rhDir, outputDir, "pentad");
    }

    /**
     * 处理旬数据（每月三旬）
     */
    private static void processDekadData(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 按旬分组
        Map<String, List<FileInfo>> filesByDekad = new HashMap<>();
        for (File plotFile : plotFiles) {
            String filePath = plotFile.getAbsolutePath();
            String timeStr = FileUtil.extractTimeFromFilePath(filePath);
            
            // 从时间字符串中提取年月日
            LocalDate date = LocalDate.parse(timeStr.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 确定旬（每月1-10, 11-20, 21-30/31）
            int day = date.getDayOfMonth();
            int dekadIndex;
            if (day <= 10) dekadIndex = 1;
            else if (day <= 20) dekadIndex = 2;
            else dekadIndex = 3;
            
            // 形成旬键: yyyyMM-d (d是旬索引)
            String dekadKey = date.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-" + dekadIndex;
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setTimeStr(timeStr);
            
            filesByDekad.computeIfAbsent(dekadKey, k -> new ArrayList<>()).add(fileInfo);
        }

        // 处理每个旬的数据
        processByTimeGroup(filesByDekad, rainDir, rhDir, outputDir, "dekad");
    }

    /**
     * 处理月数据
     */
    private static void processMonthlyData(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 按月分组
        Map<String, List<FileInfo>> filesByMonth = new HashMap<>();
        for (File plotFile : plotFiles) {
            String filePath = plotFile.getAbsolutePath();
            String timeStr = FileUtil.extractTimeFromFilePath(filePath);
            
            // 从时间字符串中提取年月
            String monthKey = timeStr.substring(0, 6); // yyyyMM
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setTimeStr(timeStr);
            
            filesByMonth.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(fileInfo);
        }

        // 处理每个月的数据
        processByTimeGroup(filesByMonth, rainDir, rhDir, outputDir, "month");
    }

    /**
     * 处理季节数据
     */
    private static void processSeasonalData(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 按季节分组
        Map<String, List<FileInfo>> filesBySeason = new HashMap<>();
        for (File plotFile : plotFiles) {
            String filePath = plotFile.getAbsolutePath();
            String timeStr = FileUtil.extractTimeFromFilePath(filePath);
            
            // 从时间字符串中提取年月
            LocalDate date = LocalDate.parse(timeStr.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 确定季节（春：3-5月，夏：6-8月，秋：9-11月，冬：12-2月）
            int month = date.getMonthValue();
            int seasonIndex;
            if (month >= 3 && month <= 5) seasonIndex = 1; // 春季
            else if (month >= 6 && month <= 8) seasonIndex = 2; // 夏季
            else if (month >= 9 && month <= 11) seasonIndex = 3; // 秋季
            else seasonIndex = 4; // 冬季 (12, 1, 2)
            
            // 形成季节键: yyyy-s (s是季节索引)
            String seasonKey = date.format(DateTimeFormatter.ofPattern("yyyy")) + "-" + seasonIndex;
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setTimeStr(timeStr);
            
            filesBySeason.computeIfAbsent(seasonKey, k -> new ArrayList<>()).add(fileInfo);
        }

        // 处理每个季节的数据
        processByTimeGroup(filesBySeason, rainDir, rhDir, outputDir, "season");
    }

    /**
     * 处理年数据
     */
    private static void processYearlyData(File plotDir, File rainDir, File rhDir, File outputDir) throws IOException {
        // 获取所有地面填图数据文件
        File[] plotFiles = plotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (plotFiles == null || plotFiles.length == 0) {
            throw new IOException("地面填图数据目录为空");
        }

        // 按年分组
        Map<String, List<FileInfo>> filesByYear = new HashMap<>();
        for (File plotFile : plotFiles) {
            String filePath = plotFile.getAbsolutePath();
            String timeStr = FileUtil.extractTimeFromFilePath(filePath);
            
            // 从时间字符串中提取年
            String yearKey = timeStr.substring(0, 4); // yyyy
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setTimeStr(timeStr);
            
            filesByYear.computeIfAbsent(yearKey, k -> new ArrayList<>()).add(fileInfo);
        }

        // 处理每年的数据
        processByTimeGroup(filesByYear, rainDir, rhDir, outputDir, "year");
    }

    /**
     * 根据时间分组处理数据的通用方法
     */
    private static void processByTimeGroup(Map<String, List<FileInfo>> filesByGroup, File rainDir, File rhDir, File outputDir, String groupType) throws IOException {
        for (Map.Entry<String, List<FileInfo>> entry : filesByGroup.entrySet()) {
            String groupKey = entry.getKey();
            List<FileInfo> filesInGroup = entry.getValue();
            
            // 创建该时间组的输出文件
            String outputPath = outputDir.getAbsolutePath() + File.separator + "meteorology_" + groupType + "_" + groupKey + ".csv";
            
            // 处理该时间组内的所有时间点
            List<MeteorologyData> allGroupData = new ArrayList<>();
            for (FileInfo plotFileInfo : filesInGroup) {
                String timeStr = plotFileInfo.getTimeStr();
                
                // 寻找匹配的降水和相对湿度文件
                File matchingRainFile = findMatchingFile(rainDir, timeStr);
                File matchingRhFile = findMatchingFile(rhDir, timeStr);
                
                if (matchingRainFile != null && matchingRhFile != null) {
                    try {
                        // 读取文件内容
                        String plotData = FileUtils.readFileToString(new File(plotFileInfo.getFilePath()), StandardCharsets.UTF_8);
                        String rainData = FileUtils.readFileToString(matchingRainFile, StandardCharsets.UTF_8);
                        String rhData = FileUtils.readFileToString(matchingRhFile, StandardCharsets.UTF_8);
                        
                        // 解析数据
                        List<PlotData> plotRecords = DataParser.parsePlotData(plotData);
                        Map<String, RainData> rainRecords = DataParser.parseRainData(rainData);
                        Map<String, RhData> rhRecords = DataParser.parseRhData(rhData);
                        
                        // 合并数据
                        List<MeteorologyData> timePointData = DataMerger.mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                        allGroupData.addAll(timePointData);
                        
                        System.out.println(groupType + " " + groupKey + " 的时间点 " + timeStr + " 处理完成, 记录数: " + timePointData.size());
                    } catch (Exception e) {
                        System.err.println("处理 " + groupType + " " + groupKey + " 的时间点 " + timeStr + " 时出错: " + e.getMessage());
                    }
                } else {
                    System.out.println(groupType + " " + groupKey + " 的时间点 " + timeStr + " 跳过: 未找到匹配的降水或相对湿度文件");
                }
            }
            
            // 导出该时间组的所有数据到CSV文件
            CsvExporter.exportMeteorologyDataToCsv(allGroupData, outputPath);
            System.out.println(groupType + " " + groupKey + " 的所有数据已成功合并并导出到: " + outputPath + ", 总记录数: " + allGroupData.size());
        }
    }
} 