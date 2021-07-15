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

    public ThresholdInputTask() {}

    @Override
    public void run(TaskMonitor monitor) {
        super.insertTasksAfterCurrentTask(
            new GenerateNetworkTask(
                this.correlationThreshold.getValue(),
                this.confidenceThreshold.getValue()
            )
        );
    }
}
