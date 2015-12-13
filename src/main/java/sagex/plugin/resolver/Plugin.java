package sagex.plugin.resolver;

import java.util.Set;
import java.util.TreeSet;

public class Plugin implements Comparable<Plugin> {
    public static class Dependency implements Comparable<Dependency> {
        public Dependency(String plugin, String minVersion) {
            super();
            this.plugin = plugin;
            this.minVersion = minVersion;
        }

        public String plugin;
        public String minVersion;
        public String maxVersion;

        @Override
        public int compareTo(Dependency o) {
            return plugin.compareTo(o.plugin);
        }
    }

    public static class Package implements Comparable<Package> {
        public Package(String packageType, String location, String mD5) {
            super();
            this.packageType = packageType;
            this.location = location;
            MD5 = mD5;
        }

        public String packageType;
        public String location;
        public String MD5;

        @Override
        public int compareTo(Package o) {
            return location.compareTo(o.location);
        }
    }

    public String identifier;
    public String version;
    public Set<Dependency> dependencies = new TreeSet<Dependency>();
    public Set<Package> packages = new TreeSet<Package>();

    public Plugin(String identifier, String version) {
        super();
        this.identifier = identifier;
        this.version = version;
    }

    @Override
    public int compareTo(Plugin other) {
        return identifier.compareTo(other.identifier);
    }

    public boolean dependOn(Plugin p2) {
        for (Dependency d : dependencies) {
            if (p2.identifier.equalsIgnoreCase(d.plugin)) {
                return true;
            }

        }
        return false;
    }

    public boolean equals(Object o) {
        return (o instanceof Plugin) && ((Plugin) o).identifier.equals(identifier);
    }
}
