package org.amdaban.cerna.internal;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

public class GenerateNetworkTask extends AbstractTask {
    public ExpressionProfileDB mRNAExpressionProfileDB;
    public ExpressionProfileDB miRNAExpressionProfileDB;
    public ExpressionProfileDB lncRNAExpressionProfileDB;
    public ExpressionProfileDB circRNAExpressionProfileDB;

    public RNAInteractionDB mRNAInteractionDB;
    public RNAInteractionDB lncRNAInteractionDB;
    public RNAInteractionDB circRNAInteractionDB;

    private double corrThres;
    private double confThres;

    public GenerateNetworkTask(ExpressionProfileDB mRNAExpProfDB, ExpressionProfileDB miRNAExpProfDB,
            ExpressionProfileDB lncRNAExpProfDB, ExpressionProfileDB circRNAExpProfDB, RNAInteractionDB mRNAIntDB,
            RNAInteractionDB lncRNAIntDB, RNAInteractionDB circRNAIntDB, double corrThres, double confThres) {

        this.mRNAExpressionProfileDB = mRNAExpProfDB;
        this.miRNAExpressionProfileDB = miRNAExpProfDB;
        this.lncRNAExpressionProfileDB = lncRNAExpProfDB;
        this.circRNAExpressionProfileDB = circRNAExpProfDB;

        this.mRNAInteractionDB = mRNAIntDB;
        this.lncRNAInteractionDB = lncRNAIntDB;
        this.circRNAInteractionDB = circRNAIntDB;

        this.corrThres = corrThres;
        this.confThres = confThres;
    }

    @Override
    public void run(TaskMonitor monitor) {
        monitor.showMessage(Level.ERROR, Double.toString(this.corrThres));
    }
}
