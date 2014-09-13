package de.stephanlindauer.criticalmass.utils;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONUtils {

    private static final String TAG = "JSONUtils";

    // static
    private JSONUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Catches {@link org.json.JSONException} while putting key/value pair into json.
     *
     * @param obj Container.
     * @param key Key.
     * @param val Value.
     */
    public static void safePut(final JSONObject obj, final String key, final Object val) {
        try {
            obj.put(key, objectForJSON(val));
        } catch (final JSONException e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * Appends if key exists. Otherwise @see #safePut(org.json.JSONObject, String, Object)
     */
    public static void safeAppendToJsonArray(final JSONObject obj, final String key, final JSONArray val) {
        try {
            if (!obj.has(key))
                obj.put(key, objectForJSON(val));
            else
                for (int i = 0; i < (val != null ? val.length() : 0); ++i)
                    obj.getJSONArray(key).put(val.get(i));
        } catch (final JSONException e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    public static void safePutAppendString(final JSONObject obj, final String key, final String val) {
        try {
            if (!obj.has(key))
                obj.put(key, objectForJSON(val));
            else
                obj.put(key, obj.getString(key).concat(" ").concat(val));
        } catch (final JSONException e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * Makes sure there won't be put a null object to a key.
     *
     * @see #safePut(org.json.JSONObject, String, Object)
     */
    public static void safePutOpt(final JSONObject obj, final String key, final Object val) {
        if (val != null && !val.equals("")) {
            JSONUtils.safePut(obj, key, val);
        } else {
            obj.remove(key);
        }
    }

    /**
     * Deep clones a json object.
     *
     * @param source Source json.
     * @return Cloned json.
     */
    public static JSONObject deepClone(final JSONObject source) {
        final JSONObject dest = new JSONObject();
        final Iterator keys = source.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            Object value = source.opt(key);
            if (value != null) {
                if (value instanceof JSONObject) {
                    value = deepClone((JSONObject) value);
                }
                safePut(dest, key, value);
            }
        }
        return dest;
    }

    /**
     * Merges source json into destination json.
     *
     * @param dest   Destination json.
     * @param source Source json.
     */
    public static void merge(final JSONObject dest, final JSONObject source) {
        if (dest == null || source == null) return;
        final Iterator keys = source.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final Object sourceValue = source.opt(key);
            if (sourceValue != null) {
                final Object destValue = dest.opt(key);
                if (destValue != null && sourceValue instanceof JSONObject && destValue instanceof JSONObject) {
                    merge((JSONObject) destValue, (JSONObject) sourceValue);
                } else {
                    safePut(dest, key, sourceValue);
                }
            }
        }
    }

    /**
     * Serializes json to object.
     *
     * @param value json.
     * @return Object
     */
    public static Object objectForJSON(final Object value) {
        if (value == null) return null;

        if (value instanceof Map) {
            JSONObject dest = new JSONObject();

            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) value;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                safePut(dest, entry.getKey(), objectForJSON(entry.getValue()));
            }
            return dest;
        } else if (value instanceof Object[]) {
            final JSONArray dest = new JSONArray();

            final Object[] array = (Object[]) value;
            for (Object val : array) {
                dest.put(objectForJSON(val));
            }
            return dest;
        } else if (value instanceof List) {
            final JSONArray dest = new JSONArray();

            final List list = (List) value;
            for (Object val : list) {
                dest.put(objectForJSON(val));
            }
            return dest;
        } else {
            return value;
        }
    }

    /**
     * Json string to JSONObject.
     *
     * @param json Json String.
     * @return JSONObject.
     */
    public static JSONObject saveStringToJSONObject(final String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (final JSONException e) {
            Log.e(TAG, "" + e.getMessage());
        }
        return jsonObject;
    }

    /**
     * Json string to JSONArray.
     *
     * @param json Json String.
     * @return JSONArray.
     */
    public static JSONArray saveStringToJsonArray(final String json) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(json);
        } catch (final JSONException e) {
            Log.e(TAG, "" + e.getMessage());
        }
        return jsonArray;
    }

    public static JSONObject loadJson(final String filePath) {
        JSONObject json = null;

        try {
            final StringBuffer buffer = new StringBuffer();
            final BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            json = new JSONObject(new JSONTokener(buffer.toString()));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    public static JSONObject loadJsonFromAssets(final Context context, final String file) {
        JSONObject json = new JSONObject();
        try {
            StringBuffer buffer = new StringBuffer();
            BufferedReader in = new BufferedReader(new InputStreamReader(context.getAssets().open(file), "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buffer.append(str);
            }
            in.close();

            json = new JSONObject(new JSONTokener(buffer.toString()));

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Concatenates multiple json arrays.
     *
     * @param arguments Multiple JSon Arrays.
     * @return Single Json Array.
     * @throws org.json.JSONException
     */
    public JSONArray concatArray(final JSONArray... arguments) throws JSONException {
        final JSONArray result = new JSONArray();
        for (final JSONArray arr : arguments) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }
}
