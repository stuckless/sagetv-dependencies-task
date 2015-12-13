package sagex.plugin.resolver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by seans on 12/12/15.
 */
public class Utils {
    public static File newDir(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    public static File newFile(String path) {
        File f = new File(path);
        if (!f.exists() && (f.getParentFile()!=null && !f.getParentFile().exists())) {
            f.getParentFile().mkdirs();
        }
        return f;
    }

    /**
     * return null if it fails, otherwise the string contents, fully trimmed.
     *
     * @param url
     * @return
     */
    public static String url2string(String url) {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (new URL(url).openStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (Throwable t) {
            return null;
        }
        return textBuilder.toString().trim();
    }

    public static void url2file(String url, File out) throws IOException {
        try (InputStream is = new URL(resolveURL(url)).openStream()) {
            Files.copy(is, Paths.get(out.toURI()));
        }
    }

    public static Properties loadProperties(File propFile) {
        Properties properties = new Properties();
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                properties.load(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static void saveProperties(Properties properties, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "properties");
        }
    }

    public static String makeFileName(String location) {
        if (location == null) return null;
        return location.replaceAll("[^A-Za-z0-9]", "_");
    }

    public static String makeZipFileName(String location) {
        return makeFileName(location) + ".zip";
    }

    public static String resolveURL(String url) throws IOException {
        URL resourceUrl, base, next;
        HttpURLConnection conn;
        String origUrl=url;
        String location;
        int redirects = 0;
        while (true)
        {
            resourceUrl = new URL(url);
            conn = (HttpURLConnection) resourceUrl.openConnection();

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            switch (conn.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    if (++redirects >10) throw new IOException("Max Redirected for " + origUrl);
                    location = conn.getHeaderField("Location");
                    base     = new URL(url);
                    next     = new URL(base, location);  // Deal with relative URLs
                    url      = next.toExternalForm();
                    continue;
            }

            break;
        }

//        System.out.println("ORIG: " + origUrl);
//        System.out.println("NEW: " + url);

        return url;
    }
}
