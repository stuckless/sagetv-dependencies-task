package sagex.plugin.resolver;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static sagex.plugin.resolver.Utils.*;

public class PluginInstaller {
    public IOutput out;
    public PluginManager pm;

    public String pluginsURL = "http://download.sagetv.com/SageTVPlugins.xml";
    public String pluginsURLV9 = "https://raw.githubusercontent.com/OpenSageTV/sagetv-plugin-repo/master/SageTVPluginsV9.xml";
    // public String pluginsMD5 = "http://download.sagetv.com/SageTVPlugins.md5.txt";

    public File target;
    public File targetCache;
    public Properties properties;
    public File propFile;
    public File pluginsFile;
    public File pluginsFileV9;
    public File libsDir;
    public File baseDir;

    private FilenameFilter jarFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return (name!=null && name.toLowerCase().endsWith(".jar"));
        }
    };

    public PluginInstaller(IOutput out, File baseDir) throws Exception {
        this(out, baseDir, null);
    }
    public PluginInstaller(IOutput out, File baseDir, String tmpTargetRelativeToBase) throws Exception {
        this.out = out;
        this.baseDir=baseDir;

        // set http follow redirects

        target = newDir(baseDir, (tmpTargetRelativeToBase==null)?"build/tmp/":tmpTargetRelativeToBase);
        targetCache = newDir(target, "cache/");
        pluginsFile = newFile(target, "cache/SageTVPlugins.xml");
        pluginsFileV9 = newFile(target, "cache/SageTVPluginsV9.xml");
        propFile = newFile(target, "cache/libs.properties");
        libsDir = newDir(target, "cache/libs/");
        properties = loadProperties(propFile);

        // determine if we need to reload
        out.msg("Downloading " + pluginsURL);
        url2file(pluginsURL, pluginsFile);
        out.msg("Using " + pluginsFile.getAbsolutePath());

        out.msg("Downloading " + pluginsURLV9);
        url2file(pluginsURLV9, pluginsFileV9);
        out.msg("Using " + pluginsFileV9.getAbsolutePath());


        SageTVPluginModelImpl model = new SageTVPluginModelImpl(pluginsFile.toURL());
        DefaultPackageResolver resolver = new DefaultPackageResolver();
        pm = new PluginManager(model, resolver);
        pm.loadPlugins();

        // load V9
        SageTVPluginModelImpl sageTVPluginModel = new SageTVPluginModelImpl(pluginsFileV9.toURL());
        sageTVPluginModel.loadPlugins(pm);
    }

    public void extractJarPackages(String plugin, File jarDir) throws Exception {
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
    }

    public void downloadJars(String url, File jarDir) throws Exception {
        // special case for urls that require preprocessing, such as urls that are in a plugin manifest
        // we ignore these since these are likely just referencing our local build files
        if (url!=null && url.contains("@@")) {
            out.msg("Skipping Local Resource: " + url);
            return;
        }

        if (url.toLowerCase().endsWith(".jar")) {
            out.msg("DOWNLOADING JAR: " + url);
            url2file(url, new File(jarDir, new File(url).getName()));
        } else {
            // Assume a .zip and download and extract jars
            File dest = new File(targetCache, makeZipFileName(url));
            if (!dest.exists() || dest.length()==0) {
                url2file(url, dest);
                out.msg("DOWNLOADING: " + url + " to " + dest.getAbsolutePath());
            } else {
                out.msg("DOWNLOADED: " + dest.getAbsolutePath());
            }
            // special case, it didn't fail, but nothing was download, so resolve the url, and try again
            if (Utils.isHTML(dest)) {
                String newURL = Utils.parseDownloadUrl(dest);
                out.msg("Downloaded file for " + url + " was HTML will try to resolve redirect URL: " + newURL);
                url2file(newURL, dest);
            }

            if (dest.length()==0 || Utils.isHTML(dest)) {
                out.msg("== FILE IS HTML ==");
                out.msg(Utils.fileToString(dest));
                dest.delete();
                throw new IOException("File is invalid for " + url);
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
            f = new File(baseDir, devPluginsXml);
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
}
