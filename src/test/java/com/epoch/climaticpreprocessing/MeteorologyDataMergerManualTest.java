package com.epoch.climaticpreprocessing;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * MeteorologyDataMerger手动测试类
 * 直接从文件系统读取实际数据文件
 */
public class MeteorologyDataMergerManualTest {

    /**
     * 主方法，可以选择运行单时次测试或多时次测试
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "single":
                    testSingleTimeDataMerging();
                    break;
                case "multiple":
                    testMultipleTimeDataMerging();
                    break;
                case "meteorology":
                    testMeteorologyDayProcessing();
                    break;
                default:
                    System.out.println("未知的测试类型: " + args[0]);
                    System.out.println("可用选项: single, multiple, meteorology");
            }
        } else {
            // 默认运行气象一天测试
            testMeteorologyDayProcessing();
        }
    }
    
    /**
     * 单时次数据合并测试
     */
    public static void testSingleTimeDataMerging() throws IOException {
        System.out.println("=== 开始气象数据合并测试 ===");

        // 指定实际文件路径
        String plotFilePath = "F:\\2024大创\\202406\\SURF\\plot\\24060502.000";  // 替换为您实际的文件路径
        String rainFilePath = "F:\\2024大创\\202406\\SURF\\rain1-p\\24060502.000";
        String rhFilePath = "F:\\2024大创\\202406\\SURF\\rh-p\\24060502.000";
        String outputFilePath = "F:\\2024大创\\202406\\test\\merged_data_24060502.csv";

        // 确认文件存在
        File plotFile = new File(plotFilePath);
        File rainFile = new File(rainFilePath);
        File rhFile = new File(rhFilePath);

        if (!plotFile.exists() || !rainFile.exists() || !rhFile.exists()) {
            System.err.println("错误：输入文件不存在!");
            System.err.println("地面填图数据文件: " + plotFilePath + (plotFile.exists() ? " [存在]" : " [不存在]"));
            System.err.println("降水数据文件: " + rainFilePath + (rainFile.exists() ? " [存在]" : " [不存在]"));
            System.err.println("相对湿度数据文件: " + rhFilePath + (rhFile.exists() ? " [存在]" : " [不存在]"));
            return;
        }

        System.out.println("使用以下文件进行处理:");
        System.out.println("地面填图数据文件: " + plotFilePath);
        System.out.println("降水数据文件: " + rainFilePath);
        System.out.println("相对湿度数据文件: " + rhFilePath);
        System.out.println("输出CSV文件: " + outputFilePath);

        System.out.println("开始调用数据合并处理方法...");

        // 调用数据合并方法
        try {
            MeteorologyDataMerger.mergeAndProcessData(
                    plotFilePath,
                    rainFilePath,
                    rhFilePath,
                    outputFilePath
            );

            System.out.println("数据合并处理成功！");

            // 验证输出文件是否存在
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                System.out.println("测试通过: 已生成输出文件");

                // 使用OpenCSV读取并显示输出文件内容
                try (CSVReader reader = new CSVReader(new FileReader(outputFile)
                )) {           // 跳过的行数
                    List<String[]> allRows = reader.readAll();

                    if (allRows.size() > 1) {
                        System.out.println("测试通过: 输出文件包含标题行和数据行");

                        System.out.println("\n=== 输出CSV文件内容预览 ===");
                        for (int i = 0; i < Math.min(5, allRows.size()); i++) {
                            // 过滤掉可能的null值，避免输出显示问题
                            String[] row = allRows.get(i);
                            String[] displayRow = new String[row.length];
                            for (int j = 0; j < row.length; j++) {
                                displayRow[j] = (row[j] == null || row[j].isEmpty()) ? "[空]" : row[j];
                            }
                            System.out.println(String.join(", ", displayRow));
                        }

                        // 检查第一个数据行的字段数量
                        if (allRows.size() > 1) {
                            String[] dataRow = allRows.get(1);
                            int fieldsCount = dataRow.length;

                            System.out.println("\n数据行字段数量: " + fieldsCount);
                            if (fieldsCount == 30) {
                                System.out.println("测试通过: 数据行字段数量正确");
                            } else {
                                System.out.println("测试不通过: 数据行字段数量应为30，实际为" + fieldsCount);
                            }
                        }
                    } else {
                        System.out.println("测试不通过: 输出文件不包含足够的数据行");
                    }
                }
            } else {
                System.out.println("测试不通过: 未生成输出文件");
            }
        } catch (Exception e) {
            System.out.println("测试失败: 处理过程中发生错误");
            e.printStackTrace();
        }

        System.out.println("\n=== 测试完成 ===");
        System.out.println("输出文件位置: " + outputFilePath);
    }

    /**
     * 测试多时次数据合并功能
     * 从指定目录读取多个时次的数据文件，合并处理后生成单一CSV文件
     */
    public static void testMultipleTimeDataMerging() throws IOException {
        System.out.println("=== 开始多时次气象数据合并测试 ===");

        // 指定实际目录路径
        String plotDirPath = "F:\\2024大创\\202406\\test-multiplefile\\plot";  // 替换为您实际的目录路径
        String rainDirPath = "F:\\2024大创\\202406\\test-multiplefile\\rain1-p";
        String rhDirPath = "F:\\2024大创\\202406\\SURF\\rh-p";
        String outputFilePath = "F:\\2024大创\\202406\\test\\merged_multiple_time_data.csv";

        // 确认目录存在
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);

        if (!plotDir.exists() || !plotDir.isDirectory() ||
            !rainDir.exists() || !rainDir.isDirectory() ||
            !rhDir.exists() || !rhDir.isDirectory()) {
            System.err.println("错误：输入目录不存在或不是有效目录!");
            System.err.println("地面填图数据目录: " + plotDirPath + (plotDir.exists() && plotDir.isDirectory() ? " [有效]" : " [无效]"));
            System.err.println("降水数据目录: " + rainDirPath + (rainDir.exists() && rainDir.isDirectory() ? " [有效]" : " [无效]"));
            System.err.println("相对湿度数据目录: " + rhDirPath + (rhDir.exists() && rhDir.isDirectory() ? " [有效]" : " [无效]"));
            return;
        }

        System.out.println("使用以下目录进行处理:");
        System.out.println("地面填图数据目录: " + plotDirPath);
        System.out.println("降水数据目录: " + rainDirPath);
        System.out.println("相对湿度数据目录: " + rhDirPath);
        System.out.println("输出CSV文件: " + outputFilePath);

        System.out.println("开始调用多时次数据合并处理方法...");

        // 调用多时次数据合并方法
        try {
            MeteorologyDataMerger.processMultipleTimeData(
                    plotDirPath,
                    rainDirPath,
                    rhDirPath,
                    outputFilePath
            );

            System.out.println("多时次数据合并处理成功！");

            // 验证输出文件是否存在
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                System.out.println("测试通过: 已生成输出文件");

                // 使用OpenCSV读取并显示输出文件内容
                try (CSVReader reader = new CSVReader(new FileReader(outputFile))) {
                    List<String[]> allRows = reader.readAll();

                    if (allRows.size() > 1) {
                        System.out.println("测试通过: 输出文件包含标题行和数据行");
                        System.out.println("总行数: " + allRows.size());

                        System.out.println("\n=== 输出CSV文件内容预览 ===");
                        for (int i = 0; i < Math.min(5, allRows.size()); i++) {
                            // 过滤掉可能的null值，避免输出显示问题
                            String[] row = allRows.get(i);
                            String[] displayRow = new String[row.length];
                            for (int j = 0; j < row.length; j++) {
                                displayRow[j] = (row[j] == null || row[j].isEmpty()) ? "[空]" : row[j];
                            }
                            System.out.println(String.join(", ", displayRow));
                        }

                        // 检查各个时次
                        Set<String> timeValues = new HashSet<>();
                        for (int i = 1; i < allRows.size(); i++) {
                            if (allRows.get(i).length > 0) {
                                String timeValue = allRows.get(i)[0];
                                timeValues.add(timeValue);
                            }
                        }
                        
                        System.out.println("\n发现的不同时次数量: " + timeValues.size());
                        System.out.println("时次列表: " + String.join(", ", timeValues));
                        
                        if (timeValues.size() > 1) {
                            System.out.println("测试通过: 输出文件包含多个时次的数据");
                        } else {
                            System.out.println("测试不通过: 输出文件只包含一个时次的数据");
                        }
                    } else {
                        System.out.println("测试不通过: 输出文件不包含足够的数据行");
                    }
                }
            } else {
                System.out.println("测试不通过: 未生成输出文件");
            }
        } catch (Exception e) {
            System.out.println("测试失败: 处理过程中发生错误");
            e.printStackTrace();
        }

        System.out.println("\n=== 多时次测试完成 ===");
        System.out.println("输出文件位置: " + outputFilePath);
    }

    /**
     * 测试气象一天数据处理功能
     * 从指定目录读取多个时次的数据文件，按照气象一天合并处理后生成多个CSV文件
     */
    public static void testMeteorologyDayProcessing() throws IOException {
        System.out.println("=== 开始气象一天数据合并测试 ===");

        // 指定实际目录路径
        String plotDirPath = "F:\\2024大创\\202406\\SURF\\plot";  // 替换为您实际的目录路径
        String rainDirPath = "F:\\2024大创\\202406\\SURF\\rain1-p";
        String rhDirPath = "F:\\2024大创\\202406\\SURF\\rh-p";
        String outputDirPath = "F:\\2024大创\\202406\\test\\meteorology_days";

        // 确认目录存在
        File plotDir = new File(plotDirPath);
        File rainDir = new File(rainDirPath);
        File rhDir = new File(rhDirPath);
        File outputDir = new File(outputDirPath);

        if (!plotDir.exists() || !plotDir.isDirectory() ||
            !rainDir.exists() || !rainDir.isDirectory() ||
            !rhDir.exists() || !rhDir.isDirectory()) {
            System.err.println("错误：输入目录不存在或不是有效目录!");
            System.err.println("地面填图数据目录: " + plotDirPath + (plotDir.exists() && plotDir.isDirectory() ? " [有效]" : " [无效]"));
            System.err.println("降水数据目录: " + rainDirPath + (rainDir.exists() && rainDir.isDirectory() ? " [有效]" : " [无效]"));
            System.err.println("相对湿度数据目录: " + rhDirPath + (rhDir.exists() && rhDir.isDirectory() ? " [有效]" : " [无效]"));
            return;
        }

        // 确保输出目录存在
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        System.out.println("使用以下目录进行处理:");
        System.out.println("地面填图数据目录: " + plotDirPath);
        System.out.println("降水数据目录: " + rainDirPath);
        System.out.println("相对湿度数据目录: " + rhDirPath);
        System.out.println("输出目录: " + outputDirPath);

        System.out.println("开始调用气象一天数据合并处理方法...");

        // 调用气象一天数据合并方法
        try {
            MeteorologyDataMerger.processMeteorologyDays(
                    plotDirPath,
                    rainDirPath,
                    rhDirPath,
                    outputDirPath
            );

            System.out.println("气象一天数据合并处理成功！");

            // 验证输出目录中是否有文件生成
            File[] outputFiles = outputDir.listFiles((dir, name) -> name.endsWith(".csv"));
            
            if (outputFiles != null && outputFiles.length > 0) {
                System.out.println("测试通过: 已生成 " + outputFiles.length + " 个气象一天数据文件");

                // 显示生成的文件信息
                System.out.println("\n=== 生成的气象一天数据文件 ===");
                for (File file : outputFiles) {
                    System.out.println(file.getName() + " (" + (file.length() / 1024) + " KB)");
                    
                    // 读取并显示文件内容预览
                    try (CSVReader reader = new CSVReader(new FileReader(file))) {
                        List<String[]> allRows = reader.readAll();
                        
                        if (allRows.size() > 1) {
                            System.out.println("  - 包含 " + allRows.size() + " 行数据");
                            
                            // 获取该文件中的时次信息
                            Set<String> timeValues = new HashSet<>();
                            for (int i = 1; i < Math.min(allRows.size(), 1000); i++) {
                                if (allRows.get(i).length > 0) {
                                    String timeValue = allRows.get(i)[0];
                                    timeValues.add(timeValue);
                                }
                            }
                            
                            System.out.println("  - 包含的时次数量: " + timeValues.size());
                            System.out.println("  - 部分时次示例: " + String.join(", ", timeValues.stream().limit(5).toArray(String[]::new)));
                        } else {
                            System.out.println("  - 文件格式可能有问题，数据行数不足");
                        }
                    }
                }
            } else {
                System.out.println("测试不通过: 未生成任何气象一天数据文件");
            }
        } catch (Exception e) {
            System.out.println("测试失败: 处理过程中发生错误");
            e.printStackTrace();
        }

        System.out.println("\n=== 气象一天测试完成 ===");
        System.out.println("输出目录位置: " + outputDirPath);
    }
}