package unpackaged;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/** Saves a quest guide page and the images/styles it needs into a local folder so it can be read offline. */
final class GuideDownloader {
    private final String base;
    private final String host;
    private final File dir;
    private final Set<String> done = new HashSet<>();
    private static final Pattern ATTR = Pattern.compile("(?i)\\b(src|href|data-src|poster)\\s*=\\s*([\"'])(.*?)\\2", Pattern.DOTALL);
    private static final Pattern CSSURL = Pattern.compile("(?i)url\\(\\s*(['\"]?)([^)'\"]+)\\1\\s*\\)");
    private static final Pattern QUEST = Pattern.compile("(?i)(?:^|[?&])p=questguides&(?:amp;)?quest=([a-z0-9_]+)");

    GuideDownloader(String base, File dir) throws MalformedURLException {
        this.base = base; this.dir = dir; this.host = new URL(base).getHost();
    }

    private static byte[] get(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000); c.setReadTimeout(30000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (offline guide saver)");
        int code = c.getResponseCode();
        if (code != 200) throw new IOException("HTTP " + code + " for " + url);
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192]; int n;
            while ((n = in.read(b)) > 0) out.write(b, 0, n);
            return out.toByteArray();
        }
    }

    private static boolean isAsset(String path) {
        return path.toLowerCase().matches(".*\\.(png|jpe?g|gif|webp|svg|ico|css|js|woff2?|ttf|otf)$");
    }

    /** Returns the replacement reference (relative to the guides folder), or null to leave it unchanged. */
    private String rewrite(String ref, URL page, boolean fromCss) {
        String r = ref.trim().replace("&amp;", "&");
        if (r.isEmpty() || r.startsWith("#") || r.startsWith("data:") || r.startsWith("javascript:") || r.startsWith("mailto:")) return null;
        URL u;
        try { u = new URL(page, r); } catch (MalformedURLException e) { return null; }
        if (!u.getHost().equalsIgnoreCase(host)) return null;
        if (!fromCss) {
            Matcher q = QUEST.matcher(u.getQuery() == null ? "" : u.getQuery());
            if (q.find()) return q.group(1) + ".html";
        }
        String lp = u.getPath();
        if (lp.startsWith("/")) lp = lp.substring(1);
        if (lp.isEmpty() || lp.contains("..") || !isAsset(lp)) return fromCss ? null : u.toString();
        if (done.add(lp)) {
            try {
                byte[] data = get(u.toString());
                if (lp.toLowerCase().endsWith(".css")) data = rewriteCss(new String(data, StandardCharsets.UTF_8), u, lp).getBytes(StandardCharsets.UTF_8);
                File f = new File(dir, lp);
                f.getParentFile().mkdirs();
                Files.write(f.toPath(), data);
            } catch (IOException e) { done.remove(lp); return u.toString(); }
        }
        return lp;
    }

    private String rewriteCss(String css, URL cssUrl, String cssLocal) {
        Matcher m = CSSURL.matcher(css);
        StringBuffer sb = new StringBuffer();
        StringBuilder up = new StringBuilder();
        for (char c : cssLocal.toCharArray()) if (c == '/') up.append("../");
        while (m.find()) {
            String lp = rewrite(m.group(2), cssUrl, true);
            String rep = lp == null ? m.group(0) : "url(\"" + up + lp + "\")";
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Downloads one guide as {@code id}.html with its assets mirrored under the same site-relative paths. */
    void fetchGuide(String id) throws IOException {
        URL page = new URL(base + "?p=questguides&quest=" + id);
        String html = new String(get(page.toString()), StandardCharsets.UTF_8);
        Matcher m = ATTR.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String rep = rewrite(m.group(3), page, false);
            String out = rep == null ? m.group(0) : m.group(1) + "=" + m.group(2) + rep + m.group(2);
            m.appendReplacement(sb, Matcher.quoteReplacement(out));
        }
        m.appendTail(sb);
        dir.mkdirs();
        Files.write(new File(dir, id + ".html").toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
