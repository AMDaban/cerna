package org.amdaban.cerna.internal;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;

import org.osgi.framework.BundleContext;

import java.util.Properties;

public class CernaActivator extends AbstractCyActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        CernaTaskFactory myFactory = new CernaTaskFactory();

        Properties props = new Properties();
        props.setProperty("preferredMenu", "Apps.cerna");
        props.setProperty("title", "Generate a ceRNA Network");

        registerService(context, myFactory, TaskFactory.class, props);
    }
}