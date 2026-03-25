package com.example.data;

import com.example.controller.SearchController;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    public static final int VALID_STRING_LENGTH = 4;
    public static final String INVALID_FILE_PATH_MESSAGE = "File path cannot be null or empty";
    public static final String INVALID_FILE_MESSAGE = "Either file does not exist at given path or Provided path is not a file.";

    private DataLoader(){}

    /**
     * Load all strings from a CSV file using CSVParser.
     * Each line must not be null or empty and contain a single 4-letter uppercase string.
     *
     * @param filePath path to CSV file
     * @return List of strings in memory, shuffled
     * @throws IOException if file cannot be read
     */
    public static List<String> loadStringsFromCSV(String filePath) throws IOException {

        Path path = validateAndGetPath(filePath);
        List<String> data = new ArrayList<>();

        try (CSVParser csvParser = new CSVParser(Files.newBufferedReader(path), buildCSVFormat())) {
            for (CSVRecord record : csvParser) {
                String value = validateRecordAndGetValue(record);
                if (value != null) {
                    data.add(value.toUpperCase());
                }
            }
        }
        Collections.shuffle(data);
        return data;
    }

    private static String validateRecordAndGetValue(CSVRecord record) {
        if (record == null || record.size() == 0) {
            return null;
        }
        String value = record.get(0).trim();
        if (value.length() != VALID_STRING_LENGTH) {
            logger.error("Skipping invalid string:{} ", value);
            return null;
        }
        return value;
    }

    private static Path validateAndGetPath(String filePath) {
        if(filePath == null || filePath.isBlank()){
            throw new IllegalArgumentException(INVALID_FILE_PATH_MESSAGE);
        }
        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(INVALID_FILE_MESSAGE);
        }
        return path;
    }

    private static CSVFormat buildCSVFormat() {
        return CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).build();
    }
}
