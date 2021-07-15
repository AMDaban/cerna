package org.amdaban.cerna.internal;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class InteractionInputTask extends AbstractTask {
    @Tunable(description = "mRNA - miRNA Interaction File", required = true, params = "input=true")
    public File mRNAExpressionFile;

    @Tunable(description = "lncRNA - miRNA Interaction File", required = true, params = "input=true")
    public File lncRNAExpressionFile;

    @Tunable(description = "circRNA - miRNA Interaction File", required = true, params = "input=true")
    public File circRNAExpressionFile;

    public InteractionInputTask() {}

    @Override
    public void run(TaskMonitor monitor) {
        super.insertTasksAfterCurrentTask(new ThresholdInputTask());
    }
}
