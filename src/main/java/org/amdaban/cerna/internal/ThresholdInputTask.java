package org.amdaban.cerna.internal;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;

public class ThresholdInputTask extends AbstractTask {
    @Tunable(description = "Correlation Threshold")
    public BoundedDouble correlationThreshold = new BoundedDouble(0.0, 0.5, 1.0, false, false);

    @Tunable(description = "Confidence Threshold")
    public BoundedDouble confidenceThreshold = new BoundedDouble(0.0, 0.5, 1.0, false, false);

    public ExpressionProfileDB mRNAExpressionProfileDB;
    public ExpressionProfileDB miRNAExpressionProfileDB;
    public ExpressionProfileDB lncRNAExpressionProfileDB;
    public ExpressionProfileDB circRNAExpressionProfileDB;

    public RNAInteractionDB mRNAInteractionDB;
    public RNAInteractionDB lncRNAInteractionDB;
    public RNAInteractionDB circRNAInteractionDB;

    public ThresholdInputTask(ExpressionProfileDB mRNAExpProfDB, ExpressionProfileDB miRNAExpProfDB,
            ExpressionProfileDB lncRNAExpProfDB, ExpressionProfileDB circRNAExpProfDB, RNAInteractionDB mRNAIntDB,
            RNAInteractionDB lncRNAIntDB, RNAInteractionDB circRNAIntDB) {

        this.mRNAExpressionProfileDB = mRNAExpProfDB;
        this.miRNAExpressionProfileDB = miRNAExpProfDB;
        this.lncRNAExpressionProfileDB = lncRNAExpProfDB;
        this.circRNAExpressionProfileDB = circRNAExpProfDB;

        this.mRNAInteractionDB = mRNAIntDB;
        this.lncRNAInteractionDB = lncRNAIntDB;
        this.circRNAInteractionDB = circRNAIntDB;
    }

    @Override
    public void run(TaskMonitor monitor) {
        super.insertTasksAfterCurrentTask(new GenerateNetworkTask(mRNAExpressionProfileDB, miRNAExpressionProfileDB,
                lncRNAExpressionProfileDB, circRNAExpressionProfileDB, mRNAInteractionDB, lncRNAInteractionDB,
                circRNAInteractionDB, this.correlationThreshold.getValue(), this.confidenceThreshold.getValue()));
    }
}
