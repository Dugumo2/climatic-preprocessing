package com.epoch.climaticpreprocessing.domain.po;

/**
 * 地面填图数据实体类
 */
public class PlotData {
    private String stationId;     // 站点ID
    private String longitude;     // 经度
    private String latitude;      // 纬度
    private String elevation;     // 海拔高度
    private String stationLevel;  // 站点级别
    private String totalCloud;    // 总云量
    private String windDirection; // 风向
    private String windSpeed;     // 风速
    private String seaLevelPressure; // 海平面气压
    private String pressureChange3h; // 3小时变压
    private String pastWeather1;  // 过去天气1
    private String pastWeather2;  // 过去天气2
    private String precipitation6h; // 6小时降水
    private String lowCloudForm;   // 低云状
    private String lowCloudAmount; // 低云量
    private String lowCloudHeight; // 低云高
    private String dewPoint;      // 露点
    private String visibility;    // 能见度
    private String presentWeather; // 现在天气
    private String temperature;   // 温度
    private String middleCloudForm; // 中云状
    private String highCloudForm;   // 高云状
    private String mark1;         // 标志1
    private String mark2;         // 标志2
    private String temperatureChange24h; // 24小时变温
    private String pressureChange24h;    // 24小时变压
    
    // 构造函数
    public PlotData() {
    }
    
    // 从原始数据数组构造
    public PlotData(String[] record) {
        if (record != null && record.length >= 26) {
            this.stationId = record[0];
            this.longitude = record[1];
            this.latitude = record[2];
            this.elevation = record[3];
            this.stationLevel = record[4];
            this.totalCloud = record[5];
            this.windDirection = record[6];
            this.windSpeed = record[7];
            this.seaLevelPressure = record[8];
            this.pressureChange3h = record[9];
            this.pastWeather1 = record[10];
            this.pastWeather2 = record[11];
            this.precipitation6h = record[12];
            this.lowCloudForm = record[13];
            this.lowCloudAmount = record[14];
            this.lowCloudHeight = record[15];
            this.dewPoint = record[16];
            this.visibility = record[17];
            this.presentWeather = record[18];
            this.temperature = record[19];
            this.middleCloudForm = record[20];
            this.highCloudForm = record[21];
            this.mark1 = record[22];
            this.mark2 = record[23];
            this.temperatureChange24h = record[24];
            this.pressureChange24h = record[25];
        }
    }
    
    // 转换为数据数组
    public String[] toArray() {
        String[] record = new String[26];
        record[0] = this.stationId;
        record[1] = this.longitude;
        record[2] = this.latitude;
        record[3] = this.elevation;
        record[4] = this.stationLevel;
        record[5] = this.totalCloud;
        record[6] = this.windDirection;
        record[7] = this.windSpeed;
        record[8] = this.seaLevelPressure;
        record[9] = this.pressureChange3h;
        record[10] = this.pastWeather1;
        record[11] = this.pastWeather2;
        record[12] = this.precipitation6h;
        record[13] = this.lowCloudForm;
        record[14] = this.lowCloudAmount;
        record[15] = this.lowCloudHeight;
        record[16] = this.dewPoint;
        record[17] = this.visibility;
        record[18] = this.presentWeather;
        record[19] = this.temperature;
        record[20] = this.middleCloudForm;
        record[21] = this.highCloudForm;
        record[22] = this.mark1;
        record[23] = this.mark2;
        record[24] = this.temperatureChange24h;
        record[25] = this.pressureChange24h;
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
    
    public String getStationLevel() {
        return stationLevel;
    }
    
    public void setStationLevel(String stationLevel) {
        this.stationLevel = stationLevel;
    }
    
    public String getTotalCloud() {
        return totalCloud;
    }
    
    public void setTotalCloud(String totalCloud) {
        this.totalCloud = totalCloud;
    }
    
    public String getWindDirection() {
        return windDirection;
    }
    
    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }
    
    public String getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public String getSeaLevelPressure() {
        return seaLevelPressure;
    }
    
    public void setSeaLevelPressure(String seaLevelPressure) {
        this.seaLevelPressure = seaLevelPressure;
    }
    
    public String getPressureChange3h() {
        return pressureChange3h;
    }
    
    public void setPressureChange3h(String pressureChange3h) {
        this.pressureChange3h = pressureChange3h;
    }
    
    public String getPastWeather1() {
        return pastWeather1;
    }
    
    public void setPastWeather1(String pastWeather1) {
        this.pastWeather1 = pastWeather1;
    }
    
    public String getPastWeather2() {
        return pastWeather2;
    }
    
    public void setPastWeather2(String pastWeather2) {
        this.pastWeather2 = pastWeather2;
    }
    
    public String getPrecipitation6h() {
        return precipitation6h;
    }
    
    public void setPrecipitation6h(String precipitation6h) {
        this.precipitation6h = precipitation6h;
    }
    
    public String getLowCloudForm() {
        return lowCloudForm;
    }
    
    public void setLowCloudForm(String lowCloudForm) {
        this.lowCloudForm = lowCloudForm;
    }
    
    public String getLowCloudAmount() {
        return lowCloudAmount;
    }
    
    public void setLowCloudAmount(String lowCloudAmount) {
        this.lowCloudAmount = lowCloudAmount;
    }
    
    public String getLowCloudHeight() {
        return lowCloudHeight;
    }
    
    public void setLowCloudHeight(String lowCloudHeight) {
        this.lowCloudHeight = lowCloudHeight;
    }
    
    public String getDewPoint() {
        return dewPoint;
    }
    
    public void setDewPoint(String dewPoint) {
        this.dewPoint = dewPoint;
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    
    public String getPresentWeather() {
        return presentWeather;
    }
    
    public void setPresentWeather(String presentWeather) {
        this.presentWeather = presentWeather;
    }
    
    public String getTemperature() {
        return temperature;
    }
    
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    
    public String getMiddleCloudForm() {
        return middleCloudForm;
    }
    
    public void setMiddleCloudForm(String middleCloudForm) {
        this.middleCloudForm = middleCloudForm;
    }
    
    public String getHighCloudForm() {
        return highCloudForm;
    }
    
    public void setHighCloudForm(String highCloudForm) {
        this.highCloudForm = highCloudForm;
    }
    
    public String getMark1() {
        return mark1;
    }
    
    public void setMark1(String mark1) {
        this.mark1 = mark1;
    }
    
    public String getMark2() {
        return mark2;
    }
    
    public void setMark2(String mark2) {
        this.mark2 = mark2;
    }
    
    public String getTemperatureChange24h() {
        return temperatureChange24h;
    }
    
    public void setTemperatureChange24h(String temperatureChange24h) {
        this.temperatureChange24h = temperatureChange24h;
    }
    
    public String getPressureChange24h() {
        return pressureChange24h;
    }
    
    public void setPressureChange24h(String pressureChange24h) {
        this.pressureChange24h = pressureChange24h;
    }
} 