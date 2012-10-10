package nl.mpi.pluginloader;

import nl.mpi.kinnate.plugin.BasePlugin;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Document : PluginService
 * Created on : Dec 23, 2011, 10:20:52 AM
 * Author : Peter Withers
 */
public class PluginService {

    private static PluginService pluginService;
    private ServiceLoader<BasePlugin> serviceLoader;

    private PluginService() {
        serviceLoader = ServiceLoader.load(BasePlugin.class);
    }

    public static synchronized PluginService getInstance() {
        if (pluginService == null) {
            pluginService = new PluginService();
        }
        return pluginService;
    }

    public Iterator<BasePlugin> getPlugins() throws ServiceConfigurationError {
        return serviceLoader.iterator();
    }

    public void listPlugins() {
        try {
            Iterator<BasePlugin> pluginIterator = serviceLoader.iterator();
            while (pluginIterator.hasNext()) {
                BasePlugin d = pluginIterator.next();
                System.out.println("Name: " + d.getName());
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PluginService.getInstance().listPlugins();
    }
}
