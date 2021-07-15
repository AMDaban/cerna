package org.amdaban.cerna.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.amdaban.cerna.internal.exceptions.BadDataException;

public class ExpressionProfileDB {
    public Map<String, double[]> profiles = new HashMap<>();
    public String[] samples;

    public ExpressionProfileDB(File file) throws IOException, CsvException, BadDataException {
        FileReader fileReader = this.createFileReader(file);
        List<String[]> fileContent = this.readContent(fileReader);
        fileReader.close();

        this.samples = this.extractSamples(fileContent);

        this.profiles = this.extractProfiles(fileContent, this.samples.length);
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

    private String[] extractSamples(List<String[]> content) throws BadDataException {
        if (content.size() == 0) {
            throw new BadDataException("content does not have any rows");
        }

        String[] headers = content.get(0);
        List<String> samples = new ArrayList<>();
        for (int i = 1; i < headers.length; i++) {
            samples.add(headers[i]);
        }
        return samples.toArray(new String[0]);
    }

    private Map<String, double[]> extractProfiles(List<String[]> content, int profileSize) throws BadDataException {
        Map<String, double[]> profiles = new HashMap<>();
        for (int i = 1; i < content.size(); i++) {
            String[] row = content.get(i);
            if (row.length != profileSize + 1) {
                throw new BadDataException("row dimention mismatch");
            }

            double[] profile = new double[profileSize];
            for (int j = 1; j < row.length; j++) {
                try {
                    profile[j - 1] = Double.parseDouble(row[j]);
                } catch (NumberFormatException e) {
                    throw new BadDataException("bad profile data");
                }
            }
            profiles.put(row[0], profile);
        }
        return profiles;
    }
}
