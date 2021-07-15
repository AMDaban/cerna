package org.amdaban.cerna.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor.Level;

public class ExpressionInputTask extends AbstractTask {
    @Tunable(description = "mRNA Expression File", required = true, params = "input=true")
    public File mRNAExpressionFile;

    @Tunable(description = "miRNA Expression File", required = true, params = "input=true")
    public File miRNAExpressionFile;

    @Tunable(description = "lncRNA Expression File", required = true, params = "input=true")
    public File lncRNAExpressionFile;

    @Tunable(description = "circRNA Expression File", required = true, params = "input=true")
    public File circRNAExpressionFile;

    public ExpressionInputTask() {}

    @Override
    public void run(TaskMonitor monitor) {
        FileReader reader;
        try {
            reader = new FileReader(this.mRNAExpressionFile);
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> list = new ArrayList<>();
            list = csvReader.readAll();
            reader.close();
            csvReader.close();

            monitor.showMessage(Level.ERROR, list.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }

        super.insertTasksAfterCurrentTask(new InteractionInputTask());
    }
}