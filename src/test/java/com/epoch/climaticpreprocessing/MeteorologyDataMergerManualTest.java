package com.epoch.climaticpreprocessing;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * MeteorologyDataMerger手动测试类
 * 直接从文件系统读取实际数据文件
 */
public class MeteorologyDataMergerManualTest {

    /**
     * 手动测试运行方法
     * 从指定文件路径读取数据文件，调用合并处理方法，验证结果
     */
    public static void main(String[] args) throws IOException {
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
}