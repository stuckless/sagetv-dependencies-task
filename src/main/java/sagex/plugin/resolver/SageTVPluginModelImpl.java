package sagex.plugin.resolver;

import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import sagex.plugin.resolver.Plugin.Dependency;
import sagex.plugin.resolver.Plugin.Package;

import java.net.URL;

public class SageTVPluginModelImpl implements IPluginModel {
    private URL url = null;

    public SageTVPluginModelImpl(URL url) {
        this.url = url;
    }

    @Override
    public void loadPlugins(final PluginManager pluginManager) throws Exception {
        SAXReader reader = new SAXReader();
        reader.addHandler("/PluginRepository/SageTVPlugin", new ElementHandler() {
            @Override
            public void onStart(ElementPath arg0) {
            }

            @Override
            public void onEnd(ElementPath path) {
                Element row = path.getCurrent();
                Plugin plugin = new Plugin(row.elementTextTrim("Identifier"), row.elementText("Version"));
                for (Object o : row.elements("Dependency")) {
                    Element e = (Element) o;
                    Dependency d = new Dependency(e.elementTextTrim("Plugin"), e.elementTextTrim("MinVersion"));
                    if (d.plugin != null) {
                        plugin.dependencies.add(d);
                    }
                }
                for (Object o : row.elements("Package")) {
                    Element e = (Element) o;
                    Package p = new Package(e.elementTextTrim("PackageType"), e.elementTextTrim("Location"), e.elementTextTrim("MD5"));
                    plugin.packages.add(p);
                }
                // remove the row from the dom, since we have it
                row.detach();
                pluginManager.addPlugin(plugin);
            }
        });
        // read document
        reader.read(url);
    }
}
