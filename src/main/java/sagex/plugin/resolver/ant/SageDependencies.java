package sagex.plugin.resolver.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import sagex.plugin.resolver.IOutput;
import sagex.plugin.resolver.PluginInstaller;

import java.io.File;

/**
 * Created by seans on 13/12/15.
 */
public class SageDependencies extends Task {
    private Project project;

    private String pluginName;
    private File jarDir = new File("target/cache/libs/");
    private String devPluginsXml;
    private String extraJars;
    private String sageJar = "https://dl.bintray.com/opensagetv/sagetv/sagetv/9.0.3.178/SageJar-9.0.3.178.zip";
    private boolean downloadSageJar=true;

    private IOutput antOuput = new IOutput() {
        @Override
        public void msg(String msg) {
            log(msg);
        }

        @Override
        public void msg(Throwable t, boolean fatal) {
            log(t, 0);
        }
    };

    public SageDependencies() {
    }

    @Override
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * The parent plugin name to use when resolving dependencies
     * @param pluginName
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Location of where jar files will be placed
     * @param jarDir
     */
    public void setJarDir(File jarDir) {
        this.jarDir = jarDir;
    }

    /**
     * Location of extra SageTV plugins xml to use when resolving dependencies
     * @param devPluginsXml
     */
    public void setDevPluginsXml(String devPluginsXml) {
        this.devPluginsXml = devPluginsXml;
    }

    /**
     * Comma separted list of extra libraries to include
     * @param extraJars
     */
    public void setExtraJars(String extraJars) {
        this.extraJars = extraJars;
    }

    /**
     * Download the Sage.jar and add to jar folder
     * @param downloadSageJar
     */
    public void setDownloadSageJar(boolean downloadSageJar) {
        this.downloadSageJar = downloadSageJar;
    }

    /**
     * Alternate URL override for Sage.jar
     * @param sageJar
     */
    public void setSageJar(String sageJar) {
        this.sageJar = sageJar;
    }


    @Override
    public void execute() throws BuildException {
        if (pluginName==null) {
            throw new BuildException("Missing pluginName, ie, phoenix-core, or some other SageTV Plugin ID");
        }
        PluginInstaller pluginInstaller;
        try {
            pluginInstaller = new PluginInstaller(antOuput);
            if (devPluginsXml!=null) {
                pluginInstaller.addDevPluginsXml(devPluginsXml);
            }
        } catch (Exception e) {
            antOuput.msg(e, true);
            throw new BuildException("Unable to load the Plugin Manager");
        }
        pluginInstaller.extractJarPackages(pluginName, jarDir);

        if (extraJars!=null) {
            antOuput.msg("Processing extra jars");
            String jars[] = extraJars.split(",");
            for (String jar : jars) {
                try {
                    pluginInstaller.downloadJars(jar.trim(), jarDir);
                } catch (Exception e) {
                    throw new BuildException("Failed to download " + jar, e);
                }
            }
        }

        if (downloadSageJar) {
            try {
                pluginInstaller.downloadJars(sageJar, jarDir);
            } catch (Exception e) {
                throw new BuildException("Failed to download Sage.jar: " + sageJar, e);
            }
        }
    }
}
