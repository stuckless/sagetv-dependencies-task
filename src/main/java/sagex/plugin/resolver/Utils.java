package sagex.plugin.resolver;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seans on 12/12/15.
 */
public class Utils {
    public static File newDir(File base, String path) {
        File f = new File(base, path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    public static File newFile(File base, String path) {
        File f = new File(base, path);
        if (!f.exists() && (f.getParentFile()!=null && !f.getParentFile().exists())) {
            f.getParentFile().mkdirs();
        }
        return f;
    }

    static final int MAX_RETRIES=5;
    /**
     * return null if it fails, otherwise the string contents, fully trimmed.
     *
     * @param url
     * @return
     */
    public static String url2string(String url) {
        String val = null;
        int tries=0;
        while (tries++<MAX_RETRIES) {
            try {
                val = _url2string(url);
                if (val!=null) break;

                System.out.println("url2string(): RETRY["+tries+"]: " + url);
                Thread.sleep(200);
            } catch (Throwable t) {
                System.out.println("url2string(): RETRY["+tries+"]: " + url);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
        return val;
    }

    private static String _url2string(String url) {
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
        int tries=0;
        while (tries++<MAX_RETRIES) {
            try {
                _url2file(url, out);
                break;
            } catch (Throwable t) {
                System.out.println("url2file(): RETRY["+tries+"]: " + url);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
    }

    private static void _url2file(String url, File out) throws IOException {
        URL resourceUrl, base, next, prev=null;
        HttpURLConnection conn=null;
        String origUrl=url;
        String location;
        int redirects = 0;
        while (true)
        {
            resourceUrl = new URL(url);
            conn = (HttpURLConnection) resourceUrl.openConnection();

            //conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            if (prev!=null) {
                conn.setRequestProperty("Referer", prev.toString());
            }

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections

            switch (conn.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    if (++redirects >10) throw new IOException("Max Redirected for " + origUrl);
                    location = conn.getHeaderField("Location");
                    prev     = resourceUrl;
                    base     = new URL(url);
                    next     = new URL(base, location);  // Deal with relative URLs
                    url      = next.toExternalForm();
                    System.out.printf("URL REDIRECT: %s -> %s\n", prev, location);
                    continue;
            }

            break;
        }

        System.out.printf("HTTP RESPONSE: %s: %s\n", conn.getResponseCode(), conn.getResponseMessage());

        try (InputStream is = conn.getInputStream(); FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buffer = new byte[1024*100];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                System.out.print(".");
            }
            System.out.println();
            //Files.copy(is, Paths.get(out.toURI()));
        }

        try {
            conn.disconnect();
        } catch (Throwable t) {}
    }

    private static void _url2fileold(String url, File out) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
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


    public static boolean isHTML(File dest) {
        try (BufferedReader fr = new BufferedReader(new FileReader(dest))) {
            String line = fr.readLine();
            if (line!=null && (line.trim().contains("doctype html") || line.contains("<html"))) {
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }

    public static String parseDownloadUrl(File dest) {
        try {
            // <meta http-equiv="refresh" content="5; url=http://downloads.sourceforge.net/project/sagetv-addons/3rd_party_libs/jackson-annotations-2.5.1.zip?r=&amp;ts=1450037350&amp;use_mirror=iweb">
            String buf = fileToString(dest);
            Pattern p = Pattern.compile("<meta .*url=([^ \">]+)");
            Matcher m = p.matcher(buf);
            if (m.find()) {
                return fixURL(m.group(1));
            } else {
                p = Pattern.compile("href=\"([^ \">]+)");
                m = p.matcher(buf);
                if (m.find()) {
                    return fixURL(m.group(1));
                }
            }
            System.out.println("NO REDIRECT URL IN");
            System.out.print(buf);
        } catch (Throwable t) {
            return null;
        }
        return null;
    }

    private static String fixURL(String url) {
        if (url==null) return null;
        url = url.replaceAll(Pattern.quote("&amp;"),"&");
        return url;
    }

    public static String fileToString(File dest) throws IOException {
        return new String(Files.readAllBytes(Paths.get(dest.toURI())), "UTF-8");
    }
}
