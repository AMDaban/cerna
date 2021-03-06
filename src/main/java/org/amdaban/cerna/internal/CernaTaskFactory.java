package org.amdaban.cerna.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

class CernaTaskFactory extends AbstractTaskFactory {
    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkNaming networkNaming;
    private final CyNetworkViewFactory networkViewFactory;
    private final CyNetworkViewManager networkViewManager;
    private final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory;

    public CernaTaskFactory(CyNetworkManager networkManager, CyNetworkFactory networkFactory,
            CyNetworkNaming networkNaming, CyNetworkViewFactory networkViewFactory,
            CyNetworkViewManager networkViewManager, ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory) {
        super();

        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
        this.networkNaming = networkNaming;
        this.networkViewManager = networkViewManager;
        this.networkViewFactory = networkViewFactory;
        this.applyPreferredLayoutTaskFactory = applyPreferredLayoutTaskFactory;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ExpressionInputTask(networkManager, networkFactory, networkNaming,
                networkViewFactory, networkViewManager, applyPreferredLayoutTaskFactory));
    }
}