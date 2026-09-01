package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The About Us page stores block images as bare object keys ({@code about-us/…})
 * and never a signed URL, exactly like {@code BrandingAsset} stores an
 * {@code objectKey}. This resolver walks the opaque block tree at the transport
 * edge:
 * <ul>
 *   <li>{@link #toSignedUrls} — on read, every bare key becomes a fresh signed
 *       URL so the client can load the image.</li>
 *   <li>{@link #toStorageKeys} — on save, any signed URL that points at one of
 *       this service's {@code about-us/} objects is normalized back to its bare
 *       key before it is persisted (the editor round-trips the URL it was given
 *       on the previous read).</li>
 * </ul>
 * Any other string — external image URLs, block text — is left untouched.
 */
@Component
public class AboutBlockImageResolver {

    private static final String KEY_PREFIX = "about-us/";
    private static final Pattern BARE_KEY =
            Pattern.compile("^about-us/[A-Za-z0-9][A-Za-z0-9._/-]*\\.[A-Za-z0-9]{2,5}$");

    private final ObjectStoragePort objectStoragePort;

    public AboutBlockImageResolver(ObjectStoragePort objectStoragePort) {
        this.objectStoragePort = objectStoragePort;
    }

    public List<Map<String, Object>> toSignedUrls(List<Map<String, Object>> blocks) {
        return mapBlocks(blocks, this::signIfBareKey);
    }

    public List<Map<String, Object>> toStorageKeys(List<Map<String, Object>> blocks) {
        return mapBlocks(blocks, AboutBlockImageResolver::keyIfOwnedUrl);
    }

    private Object signIfBareKey(String value) {
        return BARE_KEY.matcher(value).matches() ? objectStoragePort.presignGetUrl(value) : value;
    }

    private static Object keyIfOwnedUrl(String value) {
        if (!value.contains("://")) {
            return value;
        }
        int at = value.indexOf("/" + KEY_PREFIX);
        if (at < 0) {
            return value;
        }
        String tail = value.substring(at + 1);
        int cut = indexOfAny(tail, '?', '#');
        String key = cut >= 0 ? tail.substring(0, cut) : tail;
        return BARE_KEY.matcher(key).matches() ? key : value;
    }

    private static int indexOfAny(String s, char a, char b) {
        int ia = s.indexOf(a);
        int ib = s.indexOf(b);
        if (ia < 0) return ib;
        if (ib < 0) return ia;
        return Math.min(ia, ib);
    }

    // ── generic deep transform over the Jackson-parsed block tree ──────────

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mapBlocks(List<Map<String, Object>> blocks, Function<String, Object> onString) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> block : (blocks == null ? List.<Map<String, Object>>of() : blocks)) {
            out.add((Map<String, Object>) mapValue(block, onString));
        }
        return out;
    }

    private static Object mapValue(Object value, Function<String, Object> onString) {
        if (value instanceof String s) {
            return onString.apply(s);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            map.forEach((k, v) -> out.put(String.valueOf(k), mapValue(v, onString)));
            return out;
        }
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object item : list) {
                out.add(mapValue(item, onString));
            }
            return out;
        }
        return value;
    }
}
