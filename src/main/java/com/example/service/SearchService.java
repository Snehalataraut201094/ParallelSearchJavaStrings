package com.example.service;

import java.util.List;

public interface SearchService {

        /**
        * Search for strings that contain the given query.
        *
        * @param query the search string to look for in the data
        * @return a list of strings that contain the query
        */
        List<String> search(String query);
}
