package name.auh.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class GsonUtils {

    private static final Gson gson = new Gson();

    public static String
    toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T toObject(String src, Class<T> cls) {
        return gson.fromJson(src, cls);
    }

    /**
     * Convert string to given type
     * @return instance of type
     */
    public static final <V> V toObject(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public static Map<String, Object> toMap(String json) {
        Map<String, Object> retMap = gson.fromJson(json,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return retMap;
    }

    public static Map<String, String> toStrMap(String json) {
        Map<String, String> retMap = gson.fromJson(json,
                new TypeToken<Map<String, String>>() {
                }.getType());
        return retMap;
    }

    public static final String toJsonString(Object object) {
        return gson.toJson(object);
    }
}