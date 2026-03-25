package com.example.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

public class ParallelSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(ParallelSearchService.class);

    private final List<String> data;

    private static final int CPU_LIMIT = Runtime.getRuntime().availableProcessors();

    public ParallelSearchService(List<String> data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        this.data = data;
    }

    public List<String> search(String stringToSearch) {

        if (stringToSearch == null || stringToSearch.isBlank() || data.isEmpty()) {
            return Collections.emptyList();
        }
        int size = data.size();
        int chunkSize = (int) Math.ceil(((double) size / CPU_LIMIT));

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<List<String>>> tasks = new ArrayList<>();

            for (int start = 0; start < size; start += chunkSize) {
                int chunkStart = start;
                int end = Math.min(start + chunkSize, size);

                tasks.add(scope.fork(() -> {
                    List<String> localResult = new ArrayList<>();
                    for (int i = chunkStart; i < end; i++) {
                        String value = data.get(i);
                        if (value != null && value.regionMatches(0, stringToSearch, 0, stringToSearch.length())) {
                            localResult.add(value);
                        }
                    }
                    return localResult;
                }));
            }
            scope.join();
            scope.throwIfFailed();

            List<String> finalResult = new ArrayList<>();
            for (var task : tasks) {
                finalResult.addAll(task.get());
            }
            return finalResult;
        } catch (Exception ex) {
            logger.error("Search failed:", ex);
            return Collections.emptyList();
        }
    }
}
