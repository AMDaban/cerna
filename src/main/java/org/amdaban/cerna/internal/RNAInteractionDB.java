package org.amdaban.cerna.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.amdaban.cerna.internal.exceptions.BadDataException;

public class RNAInteractionDB {
    private Map<Set<String>, Double> interactionDB = new HashMap<>();
    private int fileColumnCount = 3;
    private String firstColumnTitle = "miRNA";

    public RNAInteractionDB(File file) throws IOException, CsvException, BadDataException {
        FileReader fileReader = this.createFileReader(file);
        List<String[]> fileContent = this.readContent(fileReader);
        fileReader.close();

        this.interactionDB = this.extractInteractionDB(fileContent);
    }

    public Double getScore(String miRNA, String rna) {
        Set<String> key = new HashSet<>();
        key.add(miRNA);
        key.add(rna);

        return this.interactionDB.getOrDefault(key, null);
    }

    private FileReader createFileReader(File file) throws FileNotFoundException {
        return new FileReader(file);
    }

    private List<String[]> readContent(Reader reader) throws CsvException, IOException {
        CSVReader csvReader = new CSVReader(reader);
        List<String[]> content = new ArrayList<>();
        content = csvReader.readAll();
        csvReader.close();
        return content;
    }

    private Map<Set<String>, Double> extractInteractionDB(List<String[]> content) throws BadDataException {
        if (content.size() == 0) {
            throw new BadDataException("content does not have any rows");
        }
        String[] header = content.get(0);
        if (header.length != fileColumnCount) {
            throw new BadDataException("row dimention mismatch");
        }
        String firstColumn = header[0];
        if (!firstColumn.equals(firstColumnTitle)) {
            throw new BadDataException("first column must be " + firstColumnTitle);
        }

        Map<Set<String>, Double> interactionDB = new HashMap<>();
        for (int i = 1; i < content.size(); i++) {
            String[] row = content.get(i);
            if (row.length != fileColumnCount) {
                throw new BadDataException("row dimention mismatch");
            }

            String c0 = row[0];
            String c1 = row[1];
            Double score = Double.valueOf(0);
            try {
                score = Double.valueOf(row[2]);
            } catch (NumberFormatException e) {
                throw new BadDataException("bad profile data");
            }

            Set<String> key = new HashSet<>();
            key.add(c0);
            key.add(c1);

            interactionDB.put(key, score);
        }
        return interactionDB;
    }
}
