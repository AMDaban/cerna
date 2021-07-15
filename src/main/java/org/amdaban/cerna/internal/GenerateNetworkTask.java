package org.amdaban.cerna.internal;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

public class GenerateNetworkTask extends AbstractTask {
    private double corrThres;
    private double confThres;

    public GenerateNetworkTask(double corrThres, double confThres) {
        this.corrThres = corrThres;
        this.confThres = confThres;
    }

    @Override
    public void run(TaskMonitor monitor) {
        monitor.showMessage(Level.ERROR, Double.toString(this.corrThres));
    }
}
