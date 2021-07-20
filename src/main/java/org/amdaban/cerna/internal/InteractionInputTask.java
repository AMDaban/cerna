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

public class InteractionInputTask extends AbstractTask {
    @Tunable(description = "mRNA - miRNA Interaction File", required = true, params = "input=true")
    public File mRNAInteractionFile;

    @Tunable(description = "lncRNA - miRNA Interaction File", required = true, params = "input=true")
    public File lncRNAInteractionFile;

    @Tunable(description = "circRNA - miRNA Interaction File", required = true, params = "input=true")
    public File circRNAInteractionFile;

    public ExpressionProfileDB mRNAExpressionProfileDB;
    public ExpressionProfileDB miRNAExpressionProfileDB;
    public ExpressionProfileDB lncRNAExpressionProfileDB;
    public ExpressionProfileDB circRNAExpressionProfileDB;

    public RNAInteractionDB mRNAInteractionDB;
    public RNAInteractionDB lncRNAInteractionDB;
    public RNAInteractionDB circRNAInteractionDB;

    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkNaming networkNaming;

    public InteractionInputTask(ExpressionProfileDB mRNAExpProfDB, ExpressionProfileDB miRNAExpProfDB,
            ExpressionProfileDB lncRNAExpProfDB, ExpressionProfileDB circRNAExpProfDB, CyNetworkManager networkManager,
            CyNetworkFactory networkFactory, CyNetworkNaming networkNaming) {
        this.mRNAExpressionProfileDB = mRNAExpProfDB;
        this.miRNAExpressionProfileDB = miRNAExpProfDB;
        this.lncRNAExpressionProfileDB = lncRNAExpProfDB;
        this.circRNAExpressionProfileDB = circRNAExpProfDB;

        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
        this.networkNaming = networkNaming;
    }

    @Override
    public void run(TaskMonitor monitor) throws BadDataException, IOException, CsvException {
        try {
            this.mRNAInteractionDB = new RNAInteractionDB(this.mRNAInteractionFile);
            this.lncRNAInteractionDB = new RNAInteractionDB(this.lncRNAInteractionFile);
            this.circRNAInteractionDB = new RNAInteractionDB(this.circRNAInteractionFile);

            super.insertTasksAfterCurrentTask(new ThresholdInputTask(this.mRNAExpressionProfileDB,
                    this.miRNAExpressionProfileDB, this.lncRNAExpressionProfileDB, this.circRNAExpressionProfileDB,
                    this.mRNAInteractionDB, this.lncRNAInteractionDB, this.circRNAInteractionDB, networkManager,
                    networkFactory, networkNaming));
        } catch (IOException e) {
            throw e;
        } catch (CsvException e) {
            throw e;
        } catch (BadDataException e) {
            throw e;
        }
    }
}
