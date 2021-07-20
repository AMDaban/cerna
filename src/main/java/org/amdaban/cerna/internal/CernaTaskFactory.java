package org.amdaban.cerna.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

class CernaTaskFactory extends AbstractTaskFactory {
    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkNaming networkNaming;

    public CernaTaskFactory(CyNetworkManager networkManager, CyNetworkFactory networkFactory,
            CyNetworkNaming networkNaming) {
        super();

        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
        this.networkNaming = networkNaming;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ExpressionInputTask(networkManager, networkFactory, networkNaming));
    }
}