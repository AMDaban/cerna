package org.amdaban.cerna.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;

import org.osgi.framework.BundleContext;

import java.util.Properties;

public class CernaActivator extends AbstractCyActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        CyNetworkManager cyNetworkManagerServiceRef = getService(context, CyNetworkManager.class);
        CyNetworkNaming cyNetworkNamingServiceRef = getService(context, CyNetworkNaming.class);
        CyNetworkFactory cyNetworkFactoryServiceRef = getService(context, CyNetworkFactory.class);
        CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(context, CyNetworkViewFactory.class);
        CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(context, CyNetworkViewManager.class);
        ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = getService(context, ApplyPreferredLayoutTaskFactory.class);

        CernaTaskFactory myFactory = new CernaTaskFactory(cyNetworkManagerServiceRef, cyNetworkFactoryServiceRef,
                cyNetworkNamingServiceRef, cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, applyPreferredLayoutTaskFactory);

        Properties props = new Properties();
        props.setProperty("preferredMenu", "Apps.cerna");
        props.setProperty("title", "Generate a ceRNA Network");

        registerService(context, myFactory, TaskFactory.class, props);
    }
}