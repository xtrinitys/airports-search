package com.xtrinity;

import com.xtrinity.services.IOService;
import com.xtrinity.services.search.SearchService;

import java.io.File;

public class Main {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/main/resources/data.csv";

    public static void main(String[] args) {
        if (!dataExists()) {
            throw new RuntimeException("File not exists!");
        }

        IOService io = new IOService();
        SearchService searchService = new SearchService(FILE_PATH);

        new App(searchService, io).run();
    }

    private static boolean dataExists() {
        File f = new File(FILE_PATH);
        return f.exists() && !f.isDirectory();
    }
}
