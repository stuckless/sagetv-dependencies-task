package sagex.plugin.resolver.ant;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by seans on 13/12/15.
 */
public class SageDependenciesTest {

    @Test
    public void testExecute() throws Exception {
        File cacheDir=new File("build/tmp/cache/libs");
        if (cacheDir!=null) {
            try {
                for (File f : cacheDir.listFiles()) {
                    f.delete();
                }
            } catch (Throwable t) {
                // already deleted
            }
        }
        // lastFM comes from the SageTVpluginDev.xml override for phoenix-core
        SageDependencies sageDependencies = new SageDependencies();
        sageDependencies.setJarDir(cacheDir);

        // note phoenix-sample comes from sample-plugin.xml file
        sageDependencies.setPluginName("phoenix-core, phoenix-sample");

        // comma separated list of extra plugins
        // use override sagetvplugins dev, and a sample plugin manifest
        sageDependencies.setDevPluginsXml(SageDependencies.class.getResource("SageTVPluginsDev.xml").toExternalForm()+","+SageDependencies.class.getResource("sample-plugin.xml").toExternalForm());
        sageDependencies.setExtraJars("http://central.maven.org/maven2/com/squareup/retrofit/retrofit/1.9.0/retrofit-1.9.0.jar, http://central.maven.org/maven2/com/squareup/okhttp/okhttp/2.2.0/okhttp-2.2.0.jar");
        sageDependencies.execute();
        for (String jar: new String[] {"retrofit-1.9.0.jar", "okhttp-2.2.0.jar", "commons-io-2.5.jar", "telnet.jar"}) {
            File f = new File(cacheDir, jar);
            assertTrue("FILE Should Exist, but does not: " + f,f.exists());
        }
    }
}