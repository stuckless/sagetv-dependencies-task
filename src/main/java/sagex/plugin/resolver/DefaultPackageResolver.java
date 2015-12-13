package sagex.plugin.resolver;

import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class DefaultPackageResolver implements IPackageResolver {
    public Map<String, Plugin.Package> urls = new HashMap<String, Plugin.Package>();

    public DefaultPackageResolver() throws Exception {
        // http://mvnrepository.com/artifact
        // http://mirrors.ibiblio.org/maven2/
    }

    public void loadPackages(URL url) throws Exception {
        SAXReader reader = new SAXReader();
        reader.addHandler("/packages/Package", new ElementHandler() {
            @Override
            public void onStart(ElementPath arg0) {
            }

            @Override
            public void onEnd(ElementPath path) {
                Element row = path.getCurrent();
                Plugin.Package p = new Plugin.Package(value(row.elementTextTrim("PackageType"), "JAR"), row.elementTextTrim("Location"), value(row.elementTextTrim("MD5"), "--"));
                urls.put(row.attributeValue("url"), p);
                // remove the row from the dom, since we have it
                row.detach();
            }
        });
        // read document
        reader.read(url);
    }

    private String value(String v, String d) {
        if (v == null || v.isEmpty()) return d;
        return v;
    }

    @Override
    public Plugin.Package resolvePackage(Plugin.Package pkg) {
        if (pkg == null) return null;
        Plugin.Package newUrl = urls.get(pkg.location);
        if (newUrl != null) {
            return newUrl;
        }

        return pkg;
    }

}
