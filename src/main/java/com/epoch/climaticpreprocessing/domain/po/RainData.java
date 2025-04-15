package com.epoch.climaticpreprocessing.domain.po;

/**
 * 降水数据实体类
 */
public class RainData {
    private String stationId;     // 站点ID
    private String longitude;     // 经度
    private String latitude;      // 纬度
    private String elevation;     // 海拔高度
    private String rainfall;      // 降水量
    
    // 构造函数
    public RainData() {
    }
    
    // 从原始数据数组构造
    public RainData(String[] record) {
        if (record != null && record.length >= 5) {
            this.stationId = record[0];
            this.longitude = record[1];
            this.latitude = record[2];
            this.elevation = record[3];
            this.rainfall = record[4];
        }
    }
    
    // 转换为数据数组
    public String[] toArray() {
        String[] record = new String[5];
        record[0] = this.stationId;
        record[1] = this.longitude;
        record[2] = this.latitude;
        record[3] = this.elevation;
        record[4] = this.rainfall;
        return record;
    }
    
    // Getters和Setters
    public String getStationId() {
        return stationId;
    }
    
    public void setStationId(String stationId) {
        this.stationId = stationId;
    }
    
    public String getLongitude() {
        return longitude;
    }
    
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    
    public String getLatitude() {
        return latitude;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    public String getElevation() {
        return elevation;
    }
    
    public void setElevation(String elevation) {
        this.elevation = elevation;
    }
    
    public String getRainfall() {
        return rainfall;
    }
    
    public void setRainfall(String rainfall) {
        this.rainfall = rainfall;
    }
} 