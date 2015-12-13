package sagex.plugin.resolver;

import org.junit.Test;

/**
 * Created by seans on 12/12/15.
 */
public class PluginInstallerTest {

    @Test
    public void testPlugins() throws Exception {
        PluginInstaller pi = new PluginInstaller(new ConsoleOutput());
        pi.extractJarPackages("phoenix-core", null);
    }

}