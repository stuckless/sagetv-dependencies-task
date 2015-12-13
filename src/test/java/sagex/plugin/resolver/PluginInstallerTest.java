package sagex.plugin.resolver;

import org.junit.Test;

import java.io.File;

/**
 * Created by seans on 12/12/15.
 */
public class PluginInstallerTest {

    @Test
    public void testPlugins() throws Exception {
        PluginInstaller pi = new PluginInstaller(new ConsoleOutput(), new File("."));
        pi.extractJarPackages("phoenix-core", null);
    }

}