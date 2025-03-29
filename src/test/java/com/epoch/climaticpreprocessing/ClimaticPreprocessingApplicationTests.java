package com.epoch.climaticpreprocessing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
class ClimaticPreprocessingApplicationTests {


    public class MeteorologyDataMerger {
        @Test
        public static void main(String[] args) {
            // 硬编码三个独立的文件数据，实际使用时可替换为文件读取代码
            String plotData = "diamond 1 24年06月05日00时地面填图 \n" +
                    "24 06 05 00 2502 \n" +
                    "56479  102.85   28.00 2132   32    9  110    0  138   17    0    0\n" +
                    "    3.0 9999 9999 2500   12.9   11.0   60   13.5 9999 9999    1    2    1    3\n" +
                    "56186  104.20   31.33  589   32    9  180    0  134   16    0    0\n" +
                    "      0 9999 9999 2500   17.6   15.0    0   21.6 9999 9999    1    2    3    1\n" +
                    // ... 其余数据
                    "56399  104.98   29.18  306   32    9   70    0  142   13    0    0\n" +
                    "      0 9999 9999 2500   18.3    7.0   10   19.8 9999 9999    1    2   -1    3";

            String rainData = "diamond 3 2024年06月05日00时1小时降水\n" +
                    "24 06 05 00 0 0 0 0 0 1 3219\n" +
                    "56478  102.41  28.31  1829.7  0.5\n" +
                    "56671  102.32  26.46  1811.4  1.2\n" +
                    // ... 其余数据
                    "59094  114.11  24.35  177.6  0.1";

            String rhData = "diamond 3 2024年06月05日00时相对湿度\n" +
                    "24 06 05 00 0 0 0 0 0 1 3219\n" +
                    "56186  104.19  31.33  588.9  78\n" +
                    "56478  102.41  28.31  1829.7  98\n" +
                    // ... 其余数据
                    "56381  103.49  30.07  629.9  79";

            // 解析各文件数据
            List<String[]> plotRecords = parsePlotData(plotData);
            Map<String, String[]> rainRecords = parseRainData(rainData);
            Map<String, String[]> rhRecords = parseRhData(rhData);

            // 合并数据
            List<String[]> mergedData = mergeData(plotRecords, rainRecords, rhRecords);

            // 输出合并结果
            System.out.println("time,sta,lon,lat,ele,stalev,总云量,风向,风速,海平面气压,3小时变压,过去天气1,过去天气2,6小时降水,低云状,低云量,低云高,露点,能见度,现在天气,温度,中云状,高云状,标志1,标志2,24小时变温,24小时变压,rain1,rh,p0");

            for (String[] record : mergedData) {
                System.out.println(String.join(",", record));
            }
        }

        // 解析地面填图数据
        private static List<String[]> parsePlotData(String data) {
            List<String[]> records = new ArrayList<>();
            String[] lines = data.split("\n");

            // 每条记录占两行，从第三行开始解析
            for (int i = 2; i < lines.length; i += 2) {
                if (i + 1 < lines.length) {
                    String line1 = lines[i].trim();
                    String line2 = lines[i + 1].trim();

                    if (!line1.isEmpty() && !line2.isEmpty()) {
                        String[] record = parsePlotRecord(line1, line2);
                        records.add(record);
                    }
                }
            }
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

            // 从第三行开始解析数据
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String stationId = parts[0];
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

            // 从第三行开始解析数据
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String stationId = parts[0];
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
        private static List<String[]> mergeData(List<String[]> plotRecords, Map<String, String[]> rainRecords, Map<String, String[]> rhRecords) {
            // 用于存储合并后的数据
            Map<String, String[]> mergedMap = new HashMap<>();
            Set<String> allStations = new HashSet<>();

            // 首先处理地面填图数据中的站点
            for (String[] plotRecord : plotRecords) {
                String stationId = plotRecord[0];
                allStations.add(stationId);

                String[] mergedRecord = new String[30];

                // 设置时间格式
                mergedRecord[0] = "20240605000000";

                // 复制地面填图数据的所有字段
                for (int i = 0; i < 26; i++) {
                    mergedRecord[i + 1] = plotRecord[i];
                }

                // 添加1小时降水数据
                if (rainRecords.containsKey(stationId)) {
                    mergedRecord[27] = rainRecords.get(stationId)[4];
                } else {
                    mergedRecord[27] = ",";
                }

                // 添加相对湿度数据
                if (rhRecords.containsKey(stationId)) {
                    mergedRecord[28] = rhRecords.get(stationId)[4];
                } else {
                    mergedRecord[28] = ",";
                }

                // 计算p0值(海平面气压+1000)
                try {
                    double slp = Double.parseDouble(plotRecord[8]);
                    int p0 = (int)(slp + 1000);
                    mergedRecord[29] = String.valueOf(p0);
                } catch (NumberFormatException e) {
                    mergedRecord[29] = ",";
                }

                mergedMap.put(stationId, mergedRecord);
            }

            // 处理仅存在于降水数据中的站点
            for (Map.Entry<String, String[]> entry : rainRecords.entrySet()) {
                String stationId = entry.getKey();
                if (!allStations.contains(stationId)) {
                    allStations.add(stationId);

                    String[] rainRecord = entry.getValue();
                    String[] mergedRecord = new String[30];

                    // 设置时间
                    mergedRecord[0] = "20240605000000";

                    // 添加站点基本信息
                    mergedRecord[1] = rainRecord[0]; // sta
                    mergedRecord[2] = rainRecord[1]; // lon
                    mergedRecord[3] = rainRecord[2]; // lat
                    mergedRecord[4] = rainRecord[3]; // ele

                    // 地面填图数据字段用逗号占位
                    for (int i = 5; i < 27; i++) {
                        mergedRecord[i] = ",";
                    }

                    // 添加降水数据
                    mergedRecord[27] = rainRecord[4];

                    // 添加湿度数据(如有)
                    if (rhRecords.containsKey(stationId)) {
                        mergedRecord[28] = rhRecords.get(stationId)[4];
                    } else {
                        mergedRecord[28] = ",";
                    }

                    // p0字段占位
                    mergedRecord[29] = ",";

                    mergedMap.put(stationId, mergedRecord);
                }
            }

            // 处理仅存在于湿度数据中的站点
            for (Map.Entry<String, String[]> entry : rhRecords.entrySet()) {
                String stationId = entry.getKey();
                if (!allStations.contains(stationId)) {
                    String[] rhRecord = entry.getValue();
                    String[] mergedRecord = new String[30];

                    // 设置时间
                    mergedRecord[0] = "20240605000000";

                    // 添加站点基本信息
                    mergedRecord[1] = rhRecord[0]; // sta
                    mergedRecord[2] = rhRecord[1]; // lon
                    mergedRecord[3] = rhRecord[2]; // lat
                    mergedRecord[4] = rhRecord[3]; // ele

                    // 地面填图数据字段用逗号占位
                    for (int i = 5; i < 27; i++) {
                        mergedRecord[i] = ",";
                    }

                    // 降水数据字段占位
                    mergedRecord[27] = ",";

                    // 添加湿度数据
                    mergedRecord[28] = rhRecord[4];

                    // p0字段占位
                    mergedRecord[29] = ",";

                    mergedMap.put(stationId, mergedRecord);
                }
            }

            // 对合并数据进行排序：首先是地面填图数据中的站点，保持原顺序
            List<String[]> sortedData = new ArrayList<>();

            // 按地面填图数据的原始顺序添加站点
            for (String[] plotRecord : plotRecords) {
                String stationId = plotRecord[0];
                sortedData.add(mergedMap.get(stationId));
                mergedMap.remove(stationId);
            }

            // 添加其余站点(仅存在于降水或湿度数据中的站点)
            for (String stationId : new ArrayList<>(mergedMap.keySet())) {
                sortedData.add(mergedMap.get(stationId));
            }

            return sortedData;
        }
    }

}
