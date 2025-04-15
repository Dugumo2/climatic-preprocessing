package com.epoch.climaticpreprocessing.domain.po;

/**
 * 合并后的气象数据实体类
 */
public class MeteorologyData {
    private String time;           // 时间
    private String stationId;      // 站点ID
    private String longitude;      // 经度
    private String latitude;       // 纬度
    private String elevation;      // 海拔高度
    private String stationLevel;   // 站点级别
    private String totalCloud;     // 总云量
    private String windDirection;  // 风向
    private String windSpeed;      // 风速
    private String seaLevelPressure; // 海平面气压
    private String pressureChange3h; // 3小时变压
    private String pastWeather1;   // 过去天气1
    private String pastWeather2;   // 过去天气2
    private String precipitation6h; // 6小时降水
    private String lowCloudForm;   // 低云状
    private String lowCloudAmount; // 低云量
    private String lowCloudHeight; // 低云高
    private String dewPoint;       // 露点
    private String visibility;     // 能见度
    private String presentWeather; // 现在天气
    private String temperature;    // 温度
    private String middleCloudForm; // 中云状
    private String highCloudForm;  // 高云状
    private String mark1;          // 标志1
    private String mark2;          // 标志2
    private String temperatureChange24h; // 24小时变温
    private String pressureChange24h;    // 24小时变压
    private String rainfall;       // 一小时降水
    private String relativeHumidity; // 相对湿度
    private String p0;             // 海平面气压+1000
    
    // 构造函数
    public MeteorologyData() {
    }
    
    // 从原始数据数组构造
    public MeteorologyData(String[] record) {
        if (record != null && record.length >= 30) {
            this.time = record[0];
            this.stationId = record[1];
            this.longitude = record[2];
            this.latitude = record[3];
            this.elevation = record[4];
            this.stationLevel = record[5];
            this.totalCloud = record[6];
            this.windDirection = record[7];
            this.windSpeed = record[8];
            this.seaLevelPressure = record[9];
            this.pressureChange3h = record[10];
            this.pastWeather1 = record[11];
            this.pastWeather2 = record[12];
            this.precipitation6h = record[13];
            this.lowCloudForm = record[14];
            this.lowCloudAmount = record[15];
            this.lowCloudHeight = record[16];
            this.dewPoint = record[17];
            this.visibility = record[18];
            this.presentWeather = record[19];
            this.temperature = record[20];
            this.middleCloudForm = record[21];
            this.highCloudForm = record[22];
            this.mark1 = record[23];
            this.mark2 = record[24];
            this.temperatureChange24h = record[25];
            this.pressureChange24h = record[26];
            this.rainfall = record[27];
            this.relativeHumidity = record[28];
            this.p0 = record[29];
        }
    }
    
    // 转换为数据数组
    public String[] toArray() {
        String[] record = new String[30];
        record[0] = this.time;
        record[1] = this.stationId;
        record[2] = this.longitude;
        record[3] = this.latitude;
        record[4] = this.elevation;
        record[5] = this.stationLevel;
        record[6] = this.totalCloud;
        record[7] = this.windDirection;
        record[8] = this.windSpeed;
        record[9] = this.seaLevelPressure;
        record[10] = this.pressureChange3h;
        record[11] = this.pastWeather1;
        record[12] = this.pastWeather2;
        record[13] = this.precipitation6h;
        record[14] = this.lowCloudForm;
        record[15] = this.lowCloudAmount;
        record[16] = this.lowCloudHeight;
        record[17] = this.dewPoint;
        record[18] = this.visibility;
        record[19] = this.presentWeather;
        record[20] = this.temperature;
        record[21] = this.middleCloudForm;
        record[22] = this.highCloudForm;
        record[23] = this.mark1;
        record[24] = this.mark2;
        record[25] = this.temperatureChange24h;
        record[26] = this.pressureChange24h;
        record[27] = this.rainfall;
        record[28] = this.relativeHumidity;
        record[29] = this.p0;
        return record;
    }
    
    // 从PlotData创建
    public static MeteorologyData fromPlotData(PlotData plotData, String timeStr) {
        MeteorologyData meteorologyData = new MeteorologyData();
        meteorologyData.time = timeStr;
        meteorologyData.stationId = plotData.getStationId();
        meteorologyData.longitude = plotData.getLongitude();
        meteorologyData.latitude = plotData.getLatitude();
        meteorologyData.elevation = plotData.getElevation();
        meteorologyData.stationLevel = plotData.getStationLevel();
        meteorologyData.totalCloud = plotData.getTotalCloud();
        meteorologyData.windDirection = plotData.getWindDirection();
        meteorologyData.windSpeed = plotData.getWindSpeed();
        meteorologyData.seaLevelPressure = plotData.getSeaLevelPressure();
        meteorologyData.pressureChange3h = plotData.getPressureChange3h();
        meteorologyData.pastWeather1 = plotData.getPastWeather1();
        meteorologyData.pastWeather2 = plotData.getPastWeather2();
        meteorologyData.precipitation6h = plotData.getPrecipitation6h();
        meteorologyData.lowCloudForm = plotData.getLowCloudForm();
        meteorologyData.lowCloudAmount = plotData.getLowCloudAmount();
        meteorologyData.lowCloudHeight = plotData.getLowCloudHeight();
        meteorologyData.dewPoint = plotData.getDewPoint();
        meteorologyData.visibility = plotData.getVisibility();
        meteorologyData.presentWeather = plotData.getPresentWeather();
        meteorologyData.temperature = plotData.getTemperature();
        meteorologyData.middleCloudForm = plotData.getMiddleCloudForm();
        meteorologyData.highCloudForm = plotData.getHighCloudForm();
        meteorologyData.mark1 = plotData.getMark1();
        meteorologyData.mark2 = plotData.getMark2();
        meteorologyData.temperatureChange24h = plotData.getTemperatureChange24h();
        meteorologyData.pressureChange24h = plotData.getPressureChange24h();
        
        // 计算p0值
        try {
            double slp = Double.parseDouble(plotData.getSeaLevelPressure());
            int p0Value = (int)(slp + 1000);
            meteorologyData.p0 = String.valueOf(p0Value);
        } catch (NumberFormatException e) {
            meteorologyData.p0 = "";
        }
        
        return meteorologyData;
    }
    
    // Getters和Setters
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
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
    
    public String getRainfall() {
        return rainfall;
    }
    
    public void setRainfall(String rainfall) {
        this.rainfall = rainfall;
    }
    
    public String getRelativeHumidity() {
        return relativeHumidity;
    }
    
    public void setRelativeHumidity(String relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }
    
    public String getP0() {
        return p0;
    }
    
    public void setP0(String p0) {
        this.p0 = p0;
    }
} 