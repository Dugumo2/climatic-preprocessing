package com.epoch.climaticpreprocessing.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息DTO类，用于存储气象文件和相关信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private String filePath;  // 文件路径
    private String timeStr;   // 时间字符串
    
    // 其他可能需要的字段
    private String dateStr;   // 日期字符串
} 