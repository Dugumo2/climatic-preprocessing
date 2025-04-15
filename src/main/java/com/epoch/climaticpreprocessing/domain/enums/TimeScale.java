package com.epoch.climaticpreprocessing.domain.enums;

/**
 * 时间尺度枚举
 * 定义不同的气象数据时间尺度
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