package sagex.plugin.resolver;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static sagex.plugin.resolver.Utils.*;

public class PluginInstaller {
    public IOutput out;
    public PluginManager pm;

    public String pluginsURL = "http://download.sagetv.com/SageTVPlugins.xml";
    public String pluginsMD5 = "http://download.sagetv.com/SageTVPlugins.md5.txt";

    public File target;
    public File targetCache;
    public Properties properties;
    public File propFile;
    public File pluginsFile;
    public File libsDir;

    private FilenameFilter jarFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return (name!=null && name.toLowerCase().endsWith(".jar"));
        }
    };

    public PluginInstaller(IOutput out) throws Exception {
        this.out = out;

        // set http follow redirects

        target = newDir("target");
        targetCache = newDir("target/cache/");
        pluginsFile = newFile("target/cache/SageTVPlugins.xml");
        propFile = newFile("target/cache/libs.properties");
        libsDir = newDir("target/libs/");
        properties = loadProperties(propFile);

        // determine if we need to reload
        String remotemd5 = url2string(pluginsMD5);
        String localmd5 = properties.getProperty("md5");
        if (localmd5==null || localmd5.isEmpty() || !localmd5.equals(remotemd5)) {
            out.msg("Downloading " + pluginsURL);
            properties.setProperty("md5", remotemd5);
            url2file(pluginsURL, pluginsFile);
            saveProperties(properties, propFile);
        }

        SageTVPluginModelImpl model = new SageTVPluginModelImpl(pluginsFile.toURL());
        DefaultPackageResolver resolver = new DefaultPackageResolver();
        pm = new PluginManager(model, resolver);
        pm.loadPlugins();
    }

    public List<Plugin.Package> extractJarPackages(String plugin, File jarDir) {
        try {
            if (jarDir==null) jarDir = libsDir;
            List<Plugin.Package> packages = new ArrayList<>();
            List<Plugin> toInstall = pm.resolvePlugin(plugin);
            out.msg("Calculating Dependencies for " + plugin);
            pm.dumpPlugins(toInstall, out);
            for (Plugin p: toInstall) {
                for (Plugin.Package pack : p.packages) {
                    packages.add(pack);
                }
            }
            for (Plugin.Package p: packages) {
                // download the packages
                downloadJars(p.location, jarDir);
            }
            out.msg("");
            out.msg("Done");
        } catch (Throwable t) {
            out.msg(t, true);
        }
        return null;
    }

    public void downloadJars(String url, File jarDir) throws Exception {
        if (url.toLowerCase().endsWith(".jar")) {
            out.msg("DOWNLOADING JAR: " + url);
            url2file(url, new File(jarDir, new File(url).getName()));
        } else {
            // Assume a .zip and download and extract jars
            File dest = new File(targetCache, makeZipFileName(url));
            if (!dest.exists()) {
                url2file(url, dest);
                out.msg("DOWNLOADING: " + url);
            }
            // extract jars to libs area
            pm.unzip(dest, jarDir, jarFilter, false, out);
        }
    }

    public PluginManager getPluginManager() {
        return pm;
    }

    public void addDevPluginsXml(String devPluginsXml) throws Exception {
        File f = new File(devPluginsXml);
        if (f.exists()) {
            SageTVPluginModelImpl sageTVPluginModel = new SageTVPluginModelImpl(f.toURL());
            sageTVPluginModel.loadPlugins(pm);
        } else {
            URL url = new URL(devPluginsXml);
            SageTVPluginModelImpl sageTVPluginModel = new SageTVPluginModelImpl(url);
            sageTVPluginModel.loadPlugins(pm);
        }
    }
}
