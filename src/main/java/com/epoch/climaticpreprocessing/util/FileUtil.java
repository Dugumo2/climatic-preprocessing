package com.epoch.climaticpreprocessing.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具类
 * 负责处理文件相关的操作
 */
public class FileUtil {
    
    /**
     * 从文件路径中提取时间字符串，格式为yyyyMMddHH
     * 支持多种文件名格式：
     * - plot_20111017_08.txt：从中提取20111017和08
     * - L88_20111017.txt：从中提取20111017，小时默认为00
     * - plot_202305210200.txt：从中提取20230521和02
     * 
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
    
    /**
     * 获取下一天的日期
     * 
     * @param dateStr 当前日期，格式为yyyyMMdd
     * @return 下一天的日期，格式为yyyyMMdd
     */
    public static String getNextDay(String dateStr) {
        try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            
            // 简单处理日期加1
            day++;
            
            // 处理月末情况
            int daysInMonth;
            switch (month) {
                case 2:
                    // 判断闰年
                    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                        daysInMonth = 29;
                    } else {
                        daysInMonth = 28;
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    daysInMonth = 30;
                    break;
                default:
                    daysInMonth = 31;
            }
            
            if (day > daysInMonth) {
                day = 1;
                month++;
                
                if (month > 12) {
                    month = 1;
                    year++;
                }
            }
            
            // 格式化输出
            return String.format("%04d%02d%02d", year, month, day);
        } catch (Exception e) {
            System.err.println("日期格式错误: " + dateStr);
            return dateStr;
        }
    }
    
    /**
     * 获取前一天的日期
     * 
     * @param dateStr 当前日期，格式为yyyyMMdd
     * @return 前一天的日期，格式为yyyyMMdd
     */
    public static String getPreviousDay(String dateStr) {
        try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            
            // 日期减1
            day--;
            
            // 处理月初情况
            if (day < 1) {
                month--;
                
                if (month < 1) {
                    month = 12;
                    year--;
                }
                
                // 确定上个月的天数
                int daysInPrevMonth;
                switch (month) {
                    case 2:
                        // 判断闰年
                        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                            daysInPrevMonth = 29;
                        } else {
                            daysInPrevMonth = 28;
                        }
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        daysInPrevMonth = 30;
                        break;
                    default:
                        daysInPrevMonth = 31;
                }
                
                day = daysInPrevMonth;
            }
            
            // 格式化输出
            return String.format("%04d%02d%02d", year, month, day);
        } catch (Exception e) {
            System.err.println("日期格式错误: " + dateStr);
            return dateStr;
        }
    }
} 