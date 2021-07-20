package org.amdaban.cerna.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskFactory;

import org.osgi.framework.BundleContext;

import java.util.Properties;

public class CernaActivator extends AbstractCyActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        CyNetworkManager cyNetworkManagerServiceRef = getService(context, CyNetworkManager.class);
        CyNetworkNaming cyNetworkNamingServiceRef = getService(context, CyNetworkNaming.class);
        CyNetworkFactory cyNetworkFactoryServiceRef = getService(context, CyNetworkFactory.class);

        CernaTaskFactory myFactory = new CernaTaskFactory(cyNetworkManagerServiceRef, cyNetworkFactoryServiceRef,
                cyNetworkNamingServiceRef);

        Properties props = new Properties();
        props.setProperty("preferredMenu", "Apps.cerna");
        props.setProperty("title", "Generate a ceRNA Network");

        registerService(context, myFactory, TaskFactory.class, props);
    }
}