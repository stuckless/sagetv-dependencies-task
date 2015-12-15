package sagex.plugin.resolver;

import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import sagex.plugin.resolver.Plugin.Dependency;
import sagex.plugin.resolver.Plugin.Package;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SageTVPluginModelImpl implements IPluginModel {
    private URL url = null;

    public SageTVPluginModelImpl(URL url) {
        this.url = url;
    }

    @Override
    public void loadPlugins(final PluginManager pluginManager) throws Exception {
        SAXReader reader = new SAXReader();
        ElementHandler handler = new ElementHandler() {
            @Override
            public void onStart(ElementPath arg0) {
            }

            @Override
            public void onEnd(ElementPath path) {
                Element row = path.getCurrent();
                Plugin plugin = new Plugin(row.elementTextTrim("Identifier"), version(row.elementText("Version")));
                for (Object o : row.elements("Dependency")) {
                    Element e = (Element) o;
                    Dependency d = new Dependency(e.elementTextTrim("Plugin"), version(e.elementTextTrim("MinVersion")));
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
        };
        // handle resolving plugins from a SageTV Plugins XML
        reader.addHandler("/PluginRepository/SageTVPlugin", handler);

        // hanlde resolving plugins form a simply plugin manifest
        reader.addHandler("/SageTVPlugin", handler);
        // read document
        reader.read(url);
    }

    private String version(String version) {
        // handle a case where the version coming in is in the format "@@version@@"
        // if the date is in the format @@, then we'll just use a Year.Month.Day version
        if (version==null || version.trim().startsWith("@@")) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
            return df.format(new Date());
        }
        return version;
    }
}
