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
    private String pluginName;
    private File jarDir = new File("build/tmp/cache/libs/");
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

    /**
     * The parent plugin name to use when resolving dependencies.  You can specify more than 1 sepated by comma
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
     * Location of extra SageTV plugins xml to use when resolving dependencies, can include more than one separated by comma
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
            Project project = getProject();
            pluginInstaller = new PluginInstaller(antOuput, (project==null)?new File("."):project.getBaseDir());
            if (devPluginsXml!=null) {
                String xmls[] = devPluginsXml.split("\\s*,\\s*");
                for (String xml: xmls) {
                    pluginInstaller.addDevPluginsXml(xml);
                }
            }
        } catch (Exception e) {
            antOuput.msg(e, true);
            throw new BuildException("Unable to load the Plugin Manager");
        }
        try {
            String names[] = pluginName.split("\\s*,\\s*");
            for (String name: names) {
                pluginInstaller.extractJarPackages(name, jarDir);
            }
        } catch (Exception e) {
            throw new BuildException("Failed to download SageTV Jars", e);
        }

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
