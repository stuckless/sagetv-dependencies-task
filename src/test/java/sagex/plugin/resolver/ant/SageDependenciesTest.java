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
        File cacheDir=new File("target/cache/libs");
        for (File f: cacheDir.listFiles()) {
            f.delete();
        }
        File lastFM = new File(cacheDir, "last.fm-bindings.jar");
        SageDependencies sageDependencies = new SageDependencies();
        sageDependencies.setJarDir(cacheDir);
        sageDependencies.setPluginName("phoenix-core");
        sageDependencies.setDevPluginsXml(SageDependencies.class.getResource("SageTVPluginsDev.xml").toExternalForm());
        sageDependencies.setExtraJars("http://central.maven.org/maven2/com/squareup/retrofit/retrofit/1.9.0/retrofit-1.9.0.jar, http://central.maven.org/maven2/com/squareup/okhttp/okhttp/2.2.0/okhttp-2.2.0.jar");
        sageDependencies.execute();
        for (String jar: new String[] {"last.fm-bindings.jar", "retrofit-1.9.0.jar", "okhttp-2.2.0.jar"}) {
            File f = new File(cacheDir, jar);
            assertTrue("FILE Should Exist, but does not: " + f,f.exists());
        }
    }
}