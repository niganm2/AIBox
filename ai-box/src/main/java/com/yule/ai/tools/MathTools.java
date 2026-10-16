package com.yule.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class MathTools {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "获取当前日期和时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    @Tool(description = "计算两个日期之间相差的天数，日期格式为 yyyy-MM-dd")
    public String daysBetween(
            @ToolParam(description = "开始日期，格式 yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式 yyyy-MM-dd") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            long days = ChronoUnit.DAYS.between(start, end);
            return String.format("从 %s 到 %s 相差 %d 天", startDate, endDate, days);
        } catch (Exception e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式，例如 2024-01-01";
        }
    }

    @Tool(description = "在指定日期上加上指定天数，推算新日期，日期格式为 yyyy-MM-dd")
    public String addDays(
            @ToolParam(description = "基准日期，格式 yyyy-MM-dd") String baseDate,
            @ToolParam(description = "要增加的天数，可以为负数") long days) {
        try {
            LocalDate base = LocalDate.parse(baseDate, DATE_FORMATTER);
            LocalDate result = base.plusDays(days);
            return String.format("%s 加上 %d 天后是 %s", baseDate, days, result.format(DATE_FORMATTER));
        } catch (Exception e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式，例如 2024-01-01";
        }
    }

    @Tool(description = "执行基本数学运算，支持加减乘除、幂运算、取模")
    public String calculate(
            @ToolParam(description = "第一个操作数") double a,
            @ToolParam(description = "运算符，支持 +、-、*、/、^（幂）、%（取模）") String operator,
            @ToolParam(description = "第二个操作数") double b) {
        double result;
        String op;
        switch (operator) {
            case "+":
                result = a + b;
                op = "加";
                break;
            case "-":
                result = a - b;
                op = "减";
                break;
            case "*":
                result = a * b;
                op = "乘";
                break;
            case "/":
                if (b == 0) {
                    return "错误：除数不能为零";
                }
                result = a / b;
                op = "除以";
                break;
            case "^":
                result = Math.pow(a, b);
                op = "的";
                return String.format("%g %s %.0f 次方等于 %g", a, op, b, result);
            case "%":
                if (b == 0) {
                    return "错误：除数不能为零";
                }
                result = a % b;
                op = "对";
                return String.format("%g %s %.0f 取模等于 %g", a, op, b, result);
            default:
                return "不支持的运算符：" + operator + "，请使用 +、-、*、/、^、%";
        }
        return String.format("%g %s %g 等于 %g", a, op, b, result);
    }
}
