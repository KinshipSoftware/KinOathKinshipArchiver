package nl.mpi.kinnate.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 *  Document   : PluginLoader
 *  Created on : Dec 20, 2011, 3:20:27 PM
 *  Author     : Peter Withers
 */
@Deprecated
public class PluginLoader {

    private static final Class[] parameters = new Class[]{URL.class};
    private List<KinOathPlugin> pluginCollection = new ArrayList<KinOathPlugin>();
    private SessionStorage sessionStorage;

    public PluginLoader(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void scanPluginsDirectory() throws IOException {
        File pluginsDirectory = new File(sessionStorage.getStorageDirectory(), "Plugins");
        File[] jarFiles = pluginsDirectory.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return (name.endsWith(".jar"));
            }
        });
        for (File pluginJar : jarFiles) {
            loadPluginFromJar(pluginJar);
        }
    }

    public void loadPluginFromJar(File pluginJar) throws IOException {
        List<String> classNames = getClassNames(pluginJar.getAbsolutePath());
        for (String className : classNames) {
            try {
                // Remove the “.class” at the back
                String name = className.substring(0, className.length() - 6);
                Class pluginClass = getPluginClass(pluginJar, name);
                Class[] interfaces = pluginClass.getInterfaces();
                for (Class c : interfaces) {
                    // Implement the KinOathPlugin interface
                    if (c.getName().equals("base.KinOathPlugin")) {
                        pluginCollection.add((KinOathPlugin) pluginClass.newInstance());
                    }
                }
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IOException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            } catch (InstantiationException exception) {
                exception.printStackTrace();
            }
        }
    }

    protected List<String> getClassNames(String jarName) throws IOException {
        ArrayList<String> classes = new ArrayList<String>();
        JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
        JarEntry jarEntry;
        while (true) {
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            if (jarEntry.getName().endsWith(".class")) {
                classes.add(jarEntry.getName().replaceAll("/", "\\."));
            }
        }

        return classes;
    }

    public Class getPluginClass(File file, String name) throws ClassNotFoundException, IOException {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{file.toURI()});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Failed to add the jar to the classloader");
        }
        URLClassLoader classLoader;
        Class pluginClass;
        String filePath = file.getAbsolutePath();
        filePath = "jar:file://" + filePath + "!/";
        URL url = new File(filePath).toURL();
        classLoader = new URLClassLoader(new URL[]{url});
        pluginClass = classLoader.loadClass(name);
        return pluginClass;

    }

    public List<KinOathPlugin> getPluginCollection() {
        return pluginCollection;
    }

    public void setPluginCollection(List<KinOathPlugin> pluginCollection) {
        this.pluginCollection = pluginCollection;
    }

    public static void main(String[] args) {
        try {
            new PluginLoader(new KinSessionStorage()).loadPluginFromJar(new File("/Users/petwit/Downloads/kinoath-testing 4.app/Contents/MacOS/kinoath-testing-0-7-28196.jar"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
