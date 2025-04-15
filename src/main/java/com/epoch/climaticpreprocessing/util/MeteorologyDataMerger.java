package com.epoch.climaticpreprocessing.util;

import org.apache.commons.io.FileUtils;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 从文件路径中提取时间字符串，格式为yyyyMMddHH
     * 支持多种文件名格式：
     * - plot_20111017_08.txt：从中提取20111017和08
     * - L88_20111017.txt：从中提取20111017，小时默认为00
     * - plot_202305210200.txt：从中提取20230521和02
     * @param filePath 文件路径，可以是完整路径或仅文件名
     * @return 提取的时间字符串，格式为yyyyMMddHH，如果未找到匹配则返回null
     */
    public static String extractTimeFromFilePath(String filePath) {
        // 使用正则表达式提取文件名中的日期时间部分
        // 首先尝试匹配包含年月日和小时的格式
        Pattern fullPattern = Pattern.compile("(\\d{8})(\\d{2})");
        Matcher fullMatcher = fullPattern.matcher(filePath);
        
        if (fullMatcher.find()) {
            // 直接找到完整的年月日时格式
            return fullMatcher.group(1) + fullMatcher.group(2);
        }
        
        // 如果没找到完整格式，尝试仅匹配年月日部分
        Pattern datePattern = Pattern.compile("(\\d{8})");
        Matcher dateMatcher = datePattern.matcher(filePath);
        
        if (dateMatcher.find()) {
            // 只找到年月日，小时默认为"00"
            return dateMatcher.group(1) + "00";
        }
        
        // 如果都未匹配成功，返回null
        return null;
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
     * 文件信息类，用于存储气象日中的三种文件和相关信息
     */
    private static class FileInfo {
        private final File plotFile;  // 地面填图文件
        private final File rainFile;  // 降水文件
        private final File rhFile;    // 相对湿度文件
        private final String date;    // 文件日期
        private final int hour;       // 文件小时

        public FileInfo(File plotFile, File rainFile, File rhFile, String date, int hour) {
            this.plotFile = plotFile;
            this.rainFile = rainFile;
            this.rhFile = rhFile;
            this.date = date;
            this.hour = hour;
        }

        public File getPlotFile() {
            return plotFile;
        }

        public File getRainFile() {
            return rainFile;
        }

        public File getRhFile() {
            return rhFile;
        }

        public String getDate() {
            return date;
        }

        public int getHour() {
            return hour;
        }
    }
    
    /**
     * 处理单个气象日数据并生成输出文件
     * 
     * @param meteorologicalDay 气象日日期（格式为yyyyMMdd）
     * @param fileInfos 该气象日所有时次的文件信息列表
     * @param outputDir 输出目录
     */
    public static void processMeteorologyDay(String meteorologicalDay, List<FileInfo> fileInfos, File outputDir) {
        if (fileInfos.isEmpty()) {
            System.out.println("警告: 气象日 " + meteorologicalDay + " 没有找到任何文件");
            return;
        }

        // 排序文件，按小时顺序
        fileInfos.sort(Comparator.comparingInt(FileInfo::getHour));

        // 收集所有当天的有效小时
        List<Integer> availableHours = fileInfos.stream()
                .map(FileInfo::getHour)
                .collect(Collectors.toList());
        
        System.out.println("\n------------------------------");
        System.out.println("处理气象日: " + meteorologicalDay);
        System.out.println("可用的小时数: " + availableHours.size());
        System.out.println("可用的小时: " + availableHours);

        // 检查是否有缺失的小时
        List<Integer> missingHours = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            if (!availableHours.contains(i)) {
                missingHours.add(i);
            }
        }

        // 打印缺失小时的信息
        if (!missingHours.isEmpty()) {
            StringBuilder message = new StringBuilder("【缺失时次】" + meteorologicalDay + "文件缺少时次: ");
            Collections.sort(missingHours);
            
            // 记录连续区间的起始和结束
            int start = missingHours.get(0);
            int end = start;
            
            for (int i = 1; i < missingHours.size(); i++) {
                int current = missingHours.get(i);
                if (current == end + 1) {
                    // 连续的时间段
                    end = current;
                } else {
                    // 不连续，需要输出前面的区间
                    if (start == end) {
                        message.append(start).append("h,");
                    } else {
                        message.append(start).append("h-").append(end).append("h,");
                    }
                    // 开始新的区间
                    start = current;
                    end = current;
                }
            }
            
            // 添加最后一个区间
            if (start == end) {
                message.append(start).append("h");
            } else {
                message.append(start).append("h-").append(end).append("h");
            }
            
            System.out.println(message.toString());
        } else {
            System.out.println("【完整数据】" + meteorologicalDay + "文件包含全部24小时数据");
        }
        System.out.println("------------------------------");

        try {
            // 合并该气象日的所有数据
            List<String[]> allMergedData = new ArrayList<>();
            boolean hasProcessedAnyFile = false;

            // 记录标题行
            String[] header = {"time", "sta", "lon", "lat", "ele", "stalev", "总云量", "风向", "风速", 
                              "海平面气压", "3小时变压", "过去天气1", "过去天气2", "6小时降水", "低云状", 
                              "低云量", "低云高", "露点", "能见度", "现在天气", "温度", "中云状", "高云状", 
                              "标志1", "标志2", "24小时变温", "24小时变压", "rain1", "rh", "p0"};

            for (FileInfo fileInfo : fileInfos) {
                if (fileInfo.getPlotFile() != null && fileInfo.getPlotFile().exists()) {
                    try {
                        // 读取各文件数据
                        String plotData = FileUtils.readFileToString(fileInfo.getPlotFile(), StandardCharsets.UTF_8);
                        
                        String rainData = "";
                        if (fileInfo.getRainFile() != null && fileInfo.getRainFile().exists()) {
                            rainData = FileUtils.readFileToString(fileInfo.getRainFile(), StandardCharsets.UTF_8);
                        }
                        
                        String rhData = "";
                        if (fileInfo.getRhFile() != null && fileInfo.getRhFile().exists()) {
                            rhData = FileUtils.readFileToString(fileInfo.getRhFile(), StandardCharsets.UTF_8);
                        }
                        
                        // 构建时间字符串
                        String fileName = fileInfo.getPlotFile().getName();
                        String timeStr = fileName;
                        if (fileName.contains(".")) {
                            timeStr = fileName.substring(0, fileName.lastIndexOf('.'));
                        }
                        if (timeStr.contains("_")) {
                            timeStr = timeStr.substring(0, timeStr.indexOf('_'));
                        }
                        timeStr = extractTimeFromFilePath(fileInfo.getPlotFile().getAbsolutePath());
                        
                        // 解析数据
                        List<String[]> plotRecords = parsePlotData(plotData);
                        Map<String, String[]> rainRecords = parseRainData(rainData);
                        Map<String, String[]> rhRecords = parseRhData(rhData);
                        
                        // 合并当前时次的数据
                        List<String[]> mergedData = mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                        
                        // 将合并后的数据添加到总数据集
                        allMergedData.addAll(mergedData);
                        hasProcessedAnyFile = true;
                        
                        System.out.println("  时次 " + timeStr + " 处理完成，添加 " + mergedData.size() + " 条记录");
                    } catch (Exception e) {
                        System.err.println("处理文件 " + fileInfo.getPlotFile().getName() + " 时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            if (!hasProcessedAnyFile) {
                System.out.println("警告: 气象日 " + meteorologicalDay + " 没有有效的地面填图文件");
                return;
            }

            System.out.println("气象日总共处理了 " + allMergedData.size() + " 条记录");

            // 生成输出文件名 - 不再在文件名中添加小时范围
            String outputFileName = meteorologicalDay + ".csv";

            // 写入合并后的数据到文件
            File outputFile = new File(outputDir, outputFileName);
            
            // 使用OpenCSV生成CSV文件
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
            
            System.out.println("成功合并气象日 " + meteorologicalDay + " 的数据到文件: " + outputFile.getName());

        } catch (Exception e) {
            System.err.println("处理气象日 " + meteorologicalDay + " 时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理多个气象日的数据
     * 
     * @param inputDir 输入目录（地面填图数据目录）
     * @param rainDir 降水数据目录
     * @param rhDir 相对湿度数据目录
     * @param outputDir 输出目录
     */
    public static void processMeteorologyDays(File inputDir, File rainDir, File rhDir, File outputDir) {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.println("输入目录不存在或不是一个目录: " + inputDir.getAbsolutePath());
            return;
        }
        
        if (!rainDir.exists() || !rainDir.isDirectory()) {
            System.err.println("降水数据目录不存在或不是一个目录: " + rainDir.getAbsolutePath());
            return;
        }
        
        if (!rhDir.exists() || !rhDir.isDirectory()) {
            System.err.println("相对湿度数据目录不存在或不是一个目录: " + rhDir.getAbsolutePath());
            return;
        }

        // 创建输出目录（如果不存在）
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("无法创建输出目录: " + outputDir.getAbsolutePath());
                return;
            }
        }

        // 获取输入目录中的所有文件
        File[] plotFiles = inputDir.listFiles();
        File[] rainFiles = rainDir.listFiles();
        File[] rhFiles = rhDir.listFiles();
        
        if (plotFiles == null || plotFiles.length == 0) {
            System.out.println("地面填图数据目录为空: " + inputDir.getAbsolutePath());
            return;
        }

        // 按文件名前缀（地面填图）分组，并分析气象日
        Map<String, List<File>> plotFileGroups = new HashMap<>();
        Map<String, List<File>> rainFileGroups = new HashMap<>();
        Map<String, List<File>> rhFileGroups = new HashMap<>();

        // 处理地面填图文件
        for (File file : plotFiles) {
            String fileName = file.getName();
            if (!fileName.endsWith(".000")) {
                continue;
            }

            if (fileName.length() >= 8) {
                String key = fileName.substring(0, 6); // 提取日期部分（YYMMDD）
                plotFileGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
            }
        }
        
        // 处理降水文件
        if (rainFiles != null) {
            for (File file : rainFiles) {
                String fileName = file.getName();
                if (!fileName.endsWith(".000")) {
                    continue;
                }
                
                if (fileName.length() >= 8) {
                    String key = fileName.substring(0, 6); // 提取日期部分（YYMMDD）
                    rainFileGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
                }
            }
        }
        
        // 处理相对湿度文件
        if (rhFiles != null) {
            for (File file : rhFiles) {
                String fileName = file.getName();
                if (!fileName.endsWith(".000")) {
                    continue;
                }
                
                if (fileName.length() >= 8) {
                    String key = fileName.substring(0, 6); // 提取日期部分（YYMMDD）
                    rhFileGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
                }
            }
        }

        // 根据气象日规则组织文件
        Map<String, List<FileInfo>> meteorologicalDayFiles = new HashMap<>();

        // 遍历所有地面填图文件
        for (String date : plotFileGroups.keySet()) {
            for (File plotFile : plotFileGroups.get(date)) {
                String fileName = plotFile.getName();
                if (fileName.length() < 10) continue;

                String fileDate = fileName.substring(0, 6);  // 年月日部分 (YYMMDD)
                int fileHour;
                try {
                    // 提取小时部分（第7-8位）
                    fileHour = Integer.parseInt(fileName.substring(6, 8));
                } catch (NumberFormatException e) {
                    System.out.println("警告: 无法解析小时部分: " + fileName);
                    continue;
                }

                // 确定文件属于哪个气象日
                // 根据规则：如果文件小时 ≥ 21，归到次日；如果 ≤ 20，归到当日
                String meteorologicalDay;
                int meteorologicalHour;

                if (fileHour >= 21) {
                    // 21:00-23:00 的数据属于下一个气象日
                    meteorologicalDay = getNextDay("20" + fileDate);  // 添加20前缀转为完整年份
                    meteorologicalHour = fileHour - 21; // 转换为气象日的小时（0-2）
                } else {
                    // 00:00-20:00 的数据属于当天的气象日
                    meteorologicalDay = "20" + fileDate;  // 添加20前缀转为完整年份
                    meteorologicalHour = fileHour + 3; // 转换为气象日的小时（3-23）
                }

                // 找到对应的降水和相对湿度文件
                File rainFile = null;
                File rhFile = null;

                if (rainFileGroups.containsKey(fileDate)) {
                    for (File file : rainFileGroups.get(fileDate)) {
                        if (file.getName().equals(fileName)) {
                            rainFile = file;
                            break;
                        }
                    }
                }

                if (rhFileGroups.containsKey(fileDate)) {
                    for (File file : rhFileGroups.get(fileDate)) {
                        if (file.getName().equals(fileName)) {
                            rhFile = file;
                            break;
                        }
                    }
                }

                // 创建FileInfo并添加到对应的气象日
                FileInfo fileInfo = new FileInfo(plotFile, rainFile, rhFile, fileDate, meteorologicalHour);
                meteorologicalDayFiles.computeIfAbsent(meteorologicalDay, k -> new ArrayList<>()).add(fileInfo);

                // 如果没有找到对应的降水或相对湿度文件，输出警告
                if (rainFile == null) {
                    System.out.println("警告: 无法找到匹配的降水文件: " + fileName);
                }

                if (rhFile == null) {
                    System.out.println("警告: 无法找到匹配的相对湿度文件: " + fileName);
                }
            }
        }

        // 处理每个气象日
        for (String meteorologicalDay : meteorologicalDayFiles.keySet()) {
            List<FileInfo> fileInfos = meteorologicalDayFiles.get(meteorologicalDay);
            processMeteorologyDay(meteorologicalDay, fileInfos, outputDir);
        }

        System.out.println("所有气象日处理完成，输出目录: " + outputDir.getAbsolutePath());
    }

    // 为了向下兼容，保留原有的方法签名，但内部重定向到新的方法
    public static void processMeteorologyDays(File inputDir, File outputDir) {
        // 假设降水和相对湿度数据与地面填图数据在同一目录
        processMeteorologyDays(inputDir, inputDir, inputDir, outputDir);
    }

    /**
     * 获取下一天的日期
     * @param dateStr 当前日期，格式为yyyyMMdd
     * @return 下一天的日期，格式为yyyyMMdd
     */
    private static String getNextDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(dateStr));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            System.err.println("日期格式错误: " + dateStr);
            return dateStr;
        }
    }
    
    /**
     * 获取前一天的日期
     * @param dateStr 当前日期，格式为yyyyMMdd
     * @return 前一天的日期，格式为yyyyMMdd
     */
    private static String getPreviousDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(dateStr));
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            System.err.println("日期格式错误: " + dateStr);
            return dateStr;
        }
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

    /**
     * 按照气象时间尺度处理数据
     * 
     * @param inputDir 输入目录（地面填图数据目录）
     * @param rainDir 降水数据目录
     * @param rhDir 相对湿度数据目录
     * @param outputDir 输出目录
     * @param timeScale 时间尺度，可选值：ALL, YEAR, SEASON, MONTH, PENTAD, DEKAD, DAY
     */
    public static void processMeteorologyByTimeScale(File inputDir, File rainDir, File rhDir, File outputDir, TimeScale timeScale) {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.println("输入目录不存在或不是一个目录: " + inputDir.getAbsolutePath());
            return;
        }
        
        if (!rainDir.exists() || !rainDir.isDirectory()) {
            System.err.println("降水数据目录不存在或不是一个目录: " + rainDir.getAbsolutePath());
            return;
        }
        
        if (!rhDir.exists() || !rhDir.isDirectory()) {
            System.err.println("相对湿度数据目录不存在或不是一个目录: " + rhDir.getAbsolutePath());
            return;
        }

        // 创建输出目录（如果不存在）
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("无法创建输出目录: " + outputDir.getAbsolutePath());
                return;
            }
        }
        
        // 按照不同的时间尺度进行处理
        switch (timeScale) {
            case ALL:
                processAllData(inputDir, rainDir, rhDir, outputDir);
                break;
            case YEAR:
                processYearlyData(inputDir, rainDir, rhDir, outputDir);
                break;
            case SEASON:
                processSeasonalData(inputDir, rainDir, rhDir, outputDir);
                break;
            case MONTH:
                processMonthlyData(inputDir, rainDir, rhDir, outputDir);
                break;
            case DEKAD:
                processDekadData(inputDir, rainDir, rhDir, outputDir);
                break;
            case PENTAD:
                processPentadData(inputDir, rainDir, rhDir, outputDir);
                break;
            case DAY:
                processMeteorologyDays(inputDir, rainDir, rhDir, outputDir);
                break;
            default:
                System.err.println("不支持的时间尺度: " + timeScale);
        }
    }
    
    /**
     * 处理所有数据并生成单个文件
     */
    private static void processAllData(File inputDir, File rainDir, File rhDir, File outputDir) {
        System.out.println("开始处理全部气象数据...");
        
        // 获取所有气象文件信息
        Map<String, List<FileInfo>> allFiles = collectAllMeteorologyFiles(inputDir, rainDir, rhDir);
        
        // 合并所有气象文件数据到一个文件
        mergeFilesToSingleOutput(allFiles, outputDir, "ALL_DATA.csv", "全部气象数据");
        
        System.out.println("全部气象数据处理完成");
    }
    
    /**
     * 处理年度数据
     */
    private static void processYearlyData(File inputDir, File rainDir, File rhDir, File outputDir) {
        System.out.println("开始处理年度气象数据...");
        
        // 获取所有气象文件信息
        Map<String, List<FileInfo>> allFiles = collectAllMeteorologyFiles(inputDir, rainDir, rhDir);
        
        // 按年分组
        Map<String, List<FileInfo>> yearlyFiles = new HashMap<>();
        for (String day : allFiles.keySet()) {
            String year = day.substring(0, 4);
            yearlyFiles.computeIfAbsent(year, k -> new ArrayList<>()).addAll(allFiles.get(day));
        }
        
        // 处理每一年的数据
        for (String year : yearlyFiles.keySet()) {
            mergeFilesToSingleOutput(Collections.singletonMap(year, yearlyFiles.get(year)), 
                    outputDir, year + ".csv", year + "年气象数据");
        }
        
        System.out.println("年度气象数据处理完成");
    }
    
    /**
     * 处理季度数据
     */
    private static void processSeasonalData(File inputDir, File rainDir, File rhDir, File outputDir) {
        System.out.println("开始处理季度气象数据...");
        
        // 获取所有气象文件信息
        Map<String, List<FileInfo>> allFiles = collectAllMeteorologyFiles(inputDir, rainDir, rhDir);
        
        // 按季度分组
        Map<String, List<FileInfo>> seasonalFiles = new HashMap<>();
        for (String day : allFiles.keySet()) {
            String year = day.substring(0, 4);
            int month = Integer.parseInt(day.substring(4, 6));
            String season = year + "_Q" + ((month - 1) / 3 + 1); // Q1, Q2, Q3, Q4
            seasonalFiles.computeIfAbsent(season, k -> new ArrayList<>()).addAll(allFiles.get(day));
        }
        
        // 处理每个季度的数据
        for (String season : seasonalFiles.keySet()) {
            String[] parts = season.split("_");
            String year = parts[0];
            String quarter = parts[1];
            
            mergeFilesToSingleOutput(Collections.singletonMap(season, seasonalFiles.get(season)), 
                    outputDir, season + ".csv", year + "年" + quarter + "季度气象数据");
        }
        
        System.out.println("季度气象数据处理完成");
    }
    
    /**
     * 处理月度数据
     */
    private static void processMonthlyData(File inputDir, File rainDir, File rhDir, File outputDir) {
        System.out.println("开始处理月度气象数据...");
        
        // 获取所有气象文件信息
        Map<String, List<FileInfo>> allFiles = collectAllMeteorologyFiles(inputDir, rainDir, rhDir);
        
        // 按月分组
        Map<String, List<FileInfo>> monthlyFiles = new HashMap<>();
        for (String day : allFiles.keySet()) {
            String yearMonth = day.substring(0, 6); // YYYYMM
            monthlyFiles.computeIfAbsent(yearMonth, k -> new ArrayList<>()).addAll(allFiles.get(day));
        }
        
        // 处理每个月的数据
        for (String yearMonth : monthlyFiles.keySet()) {
            String year = yearMonth.substring(0, 4);
            String month = yearMonth.substring(4, 6);
            
            mergeFilesToSingleOutput(Collections.singletonMap(yearMonth, monthlyFiles.get(yearMonth)), 
                    outputDir, yearMonth + ".csv", year + "年" + month + "月气象数据");
        }
        
        System.out.println("月度气象数据处理完成");
    }
    
    /**
     * 处理旬数据（10天一旬）
     */
    private static void processDekadData(File inputDir, File rainDir, File rhDir, File outputDir) {
        System.out.println("开始处理旬气象数据...");
        
        // 获取所有气象文件信息
        Map<String, List<FileInfo>> allFiles = collectAllMeteorologyFiles(inputDir, rainDir, rhDir);
        
        // 按旬分组 (1-10日为上旬, 11-20日为中旬, 21日及以后为下旬)
        Map<String, List<FileInfo>> dekadFiles = new HashMap<>();
        for (String day : allFiles.keySet()) {
            String yearMonth = day.substring(0, 6); // YYYYMM
            int dayOfMonth = Integer.parseInt(day.substring(6, 8));
            
            String dekad;
            if (dayOfMonth <= 10) {
                dekad = yearMonth + "_D1"; // 上旬
            } else if (dayOfMonth <= 20) {
                dekad = yearMonth + "_D2"; // 中旬
            } else {
                dekad = yearMonth + "_D3"; // 下旬
            }
            
            dekadFiles.computeIfAbsent(dekad, k -> new ArrayList<>()).addAll(allFiles.get(day));
        }
        
        // 处理每个旬的数据
        for (String dekad : dekadFiles.keySet()) {
            String[] parts = dekad.split("_");
            String yearMonth = parts[0];
            String year = yearMonth.substring(0, 4);
            String month = yearMonth.substring(4, 6);
            String dekadNum = parts[1];
            
            String dekadName;
            if ("D1".equals(dekadNum)) {
                dekadName = "上旬";
            } else if ("D2".equals(dekadNum)) {
                dekadName = "中旬";
            } else {
                dekadName = "下旬";
            }
            
            mergeFilesToSingleOutput(Collections.singletonMap(dekad, dekadFiles.get(dekad)), 
                    outputDir, dekad.replace("_", "") + ".csv", 
                    year + "年" + month + "月" + dekadName + "气象数据");
        }
        
        System.out.println("旬气象数据处理完成");
    }
    
    /**
     * 处理候数据（5天一候）
     */
    private static void processPentadData(File inputDir, File rainDir, File rhDir, File outputDir) {
        System.out.println("开始处理候气象数据...");
        
        // 获取所有气象文件信息
        Map<String, List<FileInfo>> allFiles = collectAllMeteorologyFiles(inputDir, rainDir, rhDir);
        
        // 按候分组 (一个月分6候，每5天一候）
        Map<String, List<FileInfo>> pentadFiles = new HashMap<>();
        for (String day : allFiles.keySet()) {
            String yearMonth = day.substring(0, 6); // YYYYMM
            int dayOfMonth = Integer.parseInt(day.substring(6, 8));
            
            // 计算候序号 (1-5, 6-10, 11-15, 16-20, 21-25, 26-31)
            int pentadNumber = (dayOfMonth - 1) / 5 + 1;
            String pentad = yearMonth + "_P" + pentadNumber;
            
            pentadFiles.computeIfAbsent(pentad, k -> new ArrayList<>()).addAll(allFiles.get(day));
        }
        
        // 处理每个候的数据
        for (String pentad : pentadFiles.keySet()) {
            String[] parts = pentad.split("_");
            String yearMonth = parts[0];
            String year = yearMonth.substring(0, 4);
            String month = yearMonth.substring(4, 6);
            String pentadNum = parts[1].substring(1);
            
            mergeFilesToSingleOutput(Collections.singletonMap(pentad, pentadFiles.get(pentad)), 
                    outputDir, pentad.replace("_", "") + ".csv", 
                    year + "年" + month + "月第" + pentadNum + "候气象数据");
        }
        
        System.out.println("候气象数据处理完成");
    }
    
    /**
     * 收集所有气象文件信息
     */
    private static Map<String, List<FileInfo>> collectAllMeteorologyFiles(File inputDir, File rainDir, File rhDir) {
        // 获取输入目录中的所有文件
        File[] plotFiles = inputDir.listFiles();
        File[] rainFiles = rainDir.listFiles();
        File[] rhFiles = rhDir.listFiles();
        
        if (plotFiles == null || plotFiles.length == 0) {
            System.out.println("地面填图数据目录为空: " + inputDir.getAbsolutePath());
            return Collections.emptyMap();
        }

        // 按文件名前缀（地面填图）分组，并分析气象日
        Map<String, List<File>> plotFileGroups = new HashMap<>();
        Map<String, List<File>> rainFileGroups = new HashMap<>();
        Map<String, List<File>> rhFileGroups = new HashMap<>();

        // 处理地面填图文件
        for (File file : plotFiles) {
            String fileName = file.getName();
            if (!fileName.endsWith(".000")) {
                continue;
            }

            if (fileName.length() >= 8) {
                String key = fileName.substring(0, 6); // 提取日期部分（YYMMDD）
                plotFileGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
            }
        }
        
        // 处理降水文件
        if (rainFiles != null) {
            for (File file : rainFiles) {
                String fileName = file.getName();
                if (!fileName.endsWith(".000")) {
                    continue;
                }
                
                if (fileName.length() >= 8) {
                    String key = fileName.substring(0, 6); // 提取日期部分（YYMMDD）
                    rainFileGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
                }
            }
        }
        
        // 处理相对湿度文件
        if (rhFiles != null) {
            for (File file : rhFiles) {
                String fileName = file.getName();
                if (!fileName.endsWith(".000")) {
                    continue;
                }
                
                if (fileName.length() >= 8) {
                    String key = fileName.substring(0, 6); // 提取日期部分（YYMMDD）
                    rhFileGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
                }
            }
        }

        // 根据气象日规则组织文件
        Map<String, List<FileInfo>> meteorologicalDayFiles = new HashMap<>();

        // 遍历所有地面填图文件
        for (String date : plotFileGroups.keySet()) {
            for (File plotFile : plotFileGroups.get(date)) {
                String fileName = plotFile.getName();
                if (fileName.length() < 10) continue;

                String fileDate = fileName.substring(0, 6);  // 年月日部分 (YYMMDD)
                int fileHour;
                try {
                    // 提取小时部分（第7-8位）
                    fileHour = Integer.parseInt(fileName.substring(6, 8));
                } catch (NumberFormatException e) {
                    System.out.println("警告: 无法解析小时部分: " + fileName);
                    continue;
                }

                // 根据最新气象日规则划分
                // 当日 20:00 至次日 20:00 的数据属于次日的日期
                // 如果文件小时 ≥ 21，归到次日
                // 如果文件小时 ≤ 20，归到当日
                // 注意：21时文件视为前一日20:00-21:00的数据（即文件名标记为结束时间）
                String meteorologicalDay;
                int meteorologicalHour;

                if (fileHour >= 21) {
                    // 21:00-23:59 的数据属于次日
                    meteorologicalDay = getNextDay("20" + fileDate);  // 添加20前缀转为完整年份
                    meteorologicalHour = fileHour - 21; // 转换为气象日的小时（0-2）
                } else {
                    // 00:00-20:00 的数据属于当日
                    meteorologicalDay = "20" + fileDate;  // 添加20前缀转为完整年份
                    meteorologicalHour = fileHour + 3; // 转换为气象日的小时（3-23）
                }

                // 找到对应的降水和相对湿度文件
                File rainFile = null;
                File rhFile = null;

                if (rainFileGroups.containsKey(fileDate)) {
                    for (File file : rainFileGroups.get(fileDate)) {
                        if (file.getName().equals(fileName)) {
                            rainFile = file;
                            break;
                        }
                    }
                }

                if (rhFileGroups.containsKey(fileDate)) {
                    for (File file : rhFileGroups.get(fileDate)) {
                        if (file.getName().equals(fileName)) {
                            rhFile = file;
                            break;
                        }
                    }
                }

                // 创建FileInfo并添加到对应的气象日
                FileInfo fileInfo = new FileInfo(plotFile, rainFile, rhFile, fileDate, meteorologicalHour);
                meteorologicalDayFiles.computeIfAbsent(meteorologicalDay, k -> new ArrayList<>()).add(fileInfo);
            }
        }
        
        return meteorologicalDayFiles;
    }
    
    /**
     * 将文件合并到单个输出文件
     */
    private static void mergeFilesToSingleOutput(Map<String, List<FileInfo>> filesMap, File outputDir, 
                                              String outputFileName, String description) {
        try {
            // 合并所有数据
            List<String[]> allMergedData = new ArrayList<>();
            boolean hasProcessedAnyFile = false;

            // 记录标题行
            String[] header = {"time", "sta", "lon", "lat", "ele", "stalev", "总云量", "风向", "风速", 
                             "海平面气压", "3小时变压", "过去天气1", "过去天气2", "6小时降水", "低云状", 
                             "低云量", "低云高", "露点", "能见度", "现在天气", "温度", "中云状", "高云状", 
                             "标志1", "标志2", "24小时变温", "24小时变压", "rain1", "rh", "p0"};

            System.out.println("开始处理" + description);
            
            // 处理每个时间单位的文件
            for (String timeUnit : filesMap.keySet()) {
                List<FileInfo> fileInfos = filesMap.get(timeUnit);
                
                if (fileInfos.isEmpty()) {
                    System.out.println("警告: " + timeUnit + " 没有找到任何文件");
                    continue;
                }
                
                // 排序文件，按小时顺序
                fileInfos.sort(Comparator.comparingInt(FileInfo::getHour));
                
                // 收集可用小时
                List<Integer> availableHours = fileInfos.stream()
                        .map(FileInfo::getHour)
                        .collect(Collectors.toList());
                
                // 检查是否有缺失的小时
                List<Integer> missingHours = new ArrayList<>();
                for (int i = 0; i <= 23; i++) {
                    if (!availableHours.contains(i)) {
                        missingHours.add(i);
                    }
                }

                // 打印缺失小时的信息
                if (!missingHours.isEmpty()) {
                    StringBuilder message = new StringBuilder("【缺失时次】" + timeUnit + "缺少时次: ");
                    Collections.sort(missingHours);
                    
                    // 记录连续区间的起始和结束
                    int start = missingHours.get(0);
                    int end = start;
                    
                    for (int i = 1; i < missingHours.size(); i++) {
                        int current = missingHours.get(i);
                        if (current == end + 1) {
                            // 连续的时间段
                            end = current;
                        } else {
                            // 不连续，需要输出前面的区间
                            if (start == end) {
                                message.append(start).append("h,");
                            } else {
                                message.append(start).append("h-").append(end).append("h,");
                            }
                            // 开始新的区间
                            start = current;
                            end = current;
                        }
                    }
                    
                    // 添加最后一个区间
                    if (start == end) {
                        message.append(start).append("h");
                    } else {
                        message.append(start).append("h-").append(end).append("h");
                    }
                    
                    System.out.println(message.toString());
                }
                
                // 处理每个文件
                for (FileInfo fileInfo : fileInfos) {
                    if (fileInfo.getPlotFile() != null && fileInfo.getPlotFile().exists()) {
                        try {
                            // 读取各文件数据
                            String plotData = FileUtils.readFileToString(fileInfo.getPlotFile(), StandardCharsets.UTF_8);
                            
                            String rainData = "";
                            if (fileInfo.getRainFile() != null && fileInfo.getRainFile().exists()) {
                                rainData = FileUtils.readFileToString(fileInfo.getRainFile(), StandardCharsets.UTF_8);
                            }
                            
                            String rhData = "";
                            if (fileInfo.getRhFile() != null && fileInfo.getRhFile().exists()) {
                                rhData = FileUtils.readFileToString(fileInfo.getRhFile(), StandardCharsets.UTF_8);
                            }
                            
                            // 构建时间字符串
                            String fileName = fileInfo.getPlotFile().getName();
                            String timeStr = extractTimeFromFilePath(fileInfo.getPlotFile().getAbsolutePath());
                            
                            // 解析数据
                            List<String[]> plotRecords = parsePlotData(plotData);
                            Map<String, String[]> rainRecords = parseRainData(rainData);
                            Map<String, String[]> rhRecords = parseRhData(rhData);
                            
                            // 合并当前时次的数据
                            List<String[]> mergedData = mergeData(plotRecords, rainRecords, rhRecords, timeStr);
                            
                            // 将合并后的数据添加到总数据集
                            allMergedData.addAll(mergedData);
                            hasProcessedAnyFile = true;
                        } catch (Exception e) {
                            System.err.println("处理文件 " + fileInfo.getPlotFile().getName() + " 时出错: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (!hasProcessedAnyFile) {
                System.out.println("警告: 没有找到有效的地面填图文件");
                return;
            }

            System.out.println(description + "总共处理了 " + allMergedData.size() + " 条记录");

            // 写入合并后的数据到文件
            File outputFile = new File(outputDir, outputFileName);
            
            // 使用OpenCSV生成CSV文件
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
            
            System.out.println("成功生成" + description + "文件: " + outputFile.getName());
            
        } catch (Exception e) {
            System.err.println("处理" + description + "时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定义时间尺度枚举
     */
    public enum TimeScale {
        ALL("全部数据"),
        YEAR("年度数据"),
        SEASON("季度数据"),
        MONTH("月度数据"),
        DEKAD("旬数据"),      // 10天一旬
        PENTAD("候数据"),     // 5天一候
        DAY("日数据");
        
        private final String description;
        
        TimeScale(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 