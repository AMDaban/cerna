package org.amdaban.cerna.internal;

import java.io.File;
import java.io.IOException;

import com.opencsv.exceptions.CsvException;

import org.amdaban.cerna.internal.exceptions.BadDataException;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ExpressionInputTask extends AbstractTask {
    @Tunable(description = "mRNA Expression File", required = true, params = "input=true")
    public File mRNAExpressionFile;

    @Tunable(description = "miRNA Expression File", required = true, params = "input=true")
    public File miRNAExpressionFile;

    @Tunable(description = "lncRNA Expression File", required = true, params = "input=true")
    public File lncRNAExpressionFile;

    @Tunable(description = "circRNA Expression File", required = true, params = "input=true")
    public File circRNAExpressionFile;

    public ExpressionProfileDB mRNAExpressionProfileDB;
    public ExpressionProfileDB miRNAExpressionProfileDB;
    public ExpressionProfileDB lncRNAExpressionProfileDB;
    public ExpressionProfileDB circRNAExpressionProfileDB;

    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkNaming networkNaming;

    public ExpressionInputTask(CyNetworkManager networkManager, CyNetworkFactory networkFactory,
            CyNetworkNaming networkNaming) {
        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
        this.networkNaming = networkNaming;
    }

    @Override
    public void run(TaskMonitor monitor) throws BadDataException, IOException, CsvException {
        try {
            this.mRNAExpressionProfileDB = new ExpressionProfileDB(this.mRNAExpressionFile);
            this.miRNAExpressionProfileDB = new ExpressionProfileDB(this.miRNAExpressionFile);
            this.lncRNAExpressionProfileDB = new ExpressionProfileDB(this.lncRNAExpressionFile);
            this.circRNAExpressionProfileDB = new ExpressionProfileDB(this.circRNAExpressionFile);

            super.insertTasksAfterCurrentTask(new InteractionInputTask(this.mRNAExpressionProfileDB,
                    this.miRNAExpressionProfileDB, this.lncRNAExpressionProfileDB, this.circRNAExpressionProfileDB,
                    networkManager, networkFactory, networkNaming));
        } catch (IOException e) {
            throw e;
        } catch (CsvException e) {
            throw e;
        } catch (BadDataException e) {
            throw e;
        }
    }
}