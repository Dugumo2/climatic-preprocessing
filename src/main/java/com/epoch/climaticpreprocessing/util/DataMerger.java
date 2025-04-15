package com.epoch.climaticpreprocessing.util;

import com.epoch.climaticpreprocessing.domain.po.MeteorologyData;
import com.epoch.climaticpreprocessing.domain.po.PlotData;
import com.epoch.climaticpreprocessing.domain.po.RainData;
import com.epoch.climaticpreprocessing.domain.po.RhData;

import java.util.*;

/**
 * 数据合并器工具类
 * 负责合并各种气象数据
 */
public class DataMerger {
    
    /**
     * 合并三种数据
     * 
     * @param plotDataList 地面填图数据列表
     * @param rainDataMap 降水数据Map
     * @param rhDataMap 相对湿度数据Map
     * @param timeStr 时间字符串
     * @return 合并后的气象数据列表
     */
    public static List<MeteorologyData> mergeData(List<PlotData> plotDataList, 
                                                 Map<String, RainData> rainDataMap, 
                                                 Map<String, RhData> rhDataMap, 
                                                 String timeStr) {
        // 用于存储合并后的数据
        Map<String, MeteorologyData> mergedMap = new HashMap<>();
        Set<String> allStations = new HashSet<>();
        
        int plotStationsCount = 0;
        int rainOnlyStationsCount = 0;
        int rhOnlyStationsCount = 0;
        int negativeCoordinateStationsCount = 0;
        
        // 首先处理地面填图数据中的站点
        for (PlotData plotData : plotDataList) {
            String stationId = plotData.getStationId();
            
            // 检查站点ID是否异常
            if (stationId == null || stationId.isEmpty() || "0000".equals(stationId)) {
                System.out.println("警告：跳过无效站点ID：" + (stationId == null ? "null" : stationId));
                continue;
            }
            
            // 检查经纬度是否为负值
            try {
                double lon = Double.parseDouble(plotData.getLongitude());
                double lat = Double.parseDouble(plotData.getLatitude());
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

            // 从PlotData创建MeteorologyData
            MeteorologyData meteorologyData = MeteorologyData.fromPlotData(plotData, timeStr);

            // 添加1小时降水数据
            if (rainDataMap.containsKey(stationId)) {
                meteorologyData.setRainfall(rainDataMap.get(stationId).getRainfall());
            } else {
                meteorologyData.setRainfall("");
            }

            // 添加相对湿度数据
            if (rhDataMap.containsKey(stationId)) {
                meteorologyData.setRelativeHumidity(rhDataMap.get(stationId).getRelativeHumidity());
            } else {
                meteorologyData.setRelativeHumidity("");
            }

            mergedMap.put(stationId, meteorologyData);
        }

        // 处理仅存在于降水数据中的站点
        for (Map.Entry<String, RainData> entry : rainDataMap.entrySet()) {
            String stationId = entry.getKey();
            if (!allStations.contains(stationId)) {
                RainData rainData = entry.getValue();
                
                // 检查经纬度是否为负值
                try {
                    double lon = Double.parseDouble(rainData.getLongitude());
                    double lat = Double.parseDouble(rainData.getLatitude());
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

                // 创建新的MeteorologyData
                MeteorologyData meteorologyData = new MeteorologyData();
                meteorologyData.setTime(timeStr);
                meteorologyData.setStationId(rainData.getStationId());
                meteorologyData.setLongitude(rainData.getLongitude());
                meteorologyData.setLatitude(rainData.getLatitude());
                meteorologyData.setElevation(rainData.getElevation());
                
                // 设置降水数据
                meteorologyData.setRainfall(rainData.getRainfall());
                
                // 设置相对湿度数据(如有)
                if (rhDataMap.containsKey(stationId)) {
                    meteorologyData.setRelativeHumidity(rhDataMap.get(stationId).getRelativeHumidity());
                }

                mergedMap.put(stationId, meteorologyData);
            }
        }

        // 处理仅存在于湿度数据中的站点
        for (Map.Entry<String, RhData> entry : rhDataMap.entrySet()) {
            String stationId = entry.getKey();
            if (!allStations.contains(stationId)) {
                RhData rhData = entry.getValue();
                
                // 检查经纬度是否为负值
                try {
                    double lon = Double.parseDouble(rhData.getLongitude());
                    double lat = Double.parseDouble(rhData.getLatitude());
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
                
                // 创建新的MeteorologyData
                MeteorologyData meteorologyData = new MeteorologyData();
                meteorologyData.setTime(timeStr);
                meteorologyData.setStationId(rhData.getStationId());
                meteorologyData.setLongitude(rhData.getLongitude());
                meteorologyData.setLatitude(rhData.getLatitude());
                meteorologyData.setElevation(rhData.getElevation());
                
                // 设置相对湿度数据
                meteorologyData.setRelativeHumidity(rhData.getRelativeHumidity());

                mergedMap.put(stationId, meteorologyData);
            }
        }

        System.out.println("地面填图站点数: " + plotStationsCount);
        System.out.println("仅降水数据中的站点数: " + rainOnlyStationsCount);
        System.out.println("仅相对湿度数据中的站点数: " + rhOnlyStationsCount);
        System.out.println("负经纬度站点数（已跳过）: " + negativeCoordinateStationsCount);
        System.out.println("总唯一站点数: " + allStations.size());

        // 对合并数据进行排序：首先是地面填图数据中的站点，保持原顺序
        List<MeteorologyData> sortedData = new ArrayList<>();

        // 按地面填图数据的原始顺序添加站点
        for (PlotData plotData : plotDataList) {
            String stationId = plotData.getStationId();
            MeteorologyData meteorologyData = mergedMap.get(stationId);
            if (meteorologyData != null) {
                sortedData.add(meteorologyData);
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
     * 合并多时次数据
     * 
     * @param timeDataMap 按时次分组的气象数据Map
     * @return 合并后的气象数据列表
     */
    public static List<MeteorologyData> mergeMultipleTimeData(Map<String, List<MeteorologyData>> timeDataMap) {
        List<MeteorologyData> allMergedData = new ArrayList<>();
        
        // 按时间顺序处理
        List<String> sortedTimes = new ArrayList<>(timeDataMap.keySet());
        Collections.sort(sortedTimes);
        
        for (String time : sortedTimes) {
            allMergedData.addAll(timeDataMap.get(time));
        }
        
        return allMergedData;
    }
} 