package com.crn.lgdms.common.util;

import org.springframework.stereotype.Component;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;

@Component
public class CsvExportUtil {

    public static <T> void export(PrintWriter writer,
                                  List<String> headers,
                                  List<T> data,
                                  Function<T, List<String>> rowMapper) {
        // Write headers
        writer.println(String.join(",", headers));

        // Write data rows
        for (T item : data) {
            List<String> row = rowMapper.apply(item);
            writer.println(String.join(",", row));
        }

        writer.flush();
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\"", "\"\"");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("\n")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
