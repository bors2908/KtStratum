package ge.becrin.kt.stratum.message;

import ge.becrin.kt.stratum.MalformedStratumMessageException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationMessage
    extends Message {

    protected static final String JSON_STRATUM_KEY_METHOD = "method";

    protected static final String JSON_STRATUM_KEY_PARAMS = "params";

    private String methodName;

    private List<Object> arrayParams;

    //TODO Override original with map behavior instead of this.
    private Map<String, Object> objectParams;

    public NotificationMessage(final JSONObject jsonMessage)
        throws MalformedStratumMessageException {
        super(jsonMessage);
    }

    public NotificationMessage(final String methodName, final Object... params) {
        super((Long)null);

        this.setMethodName(methodName);
        this.setArrayParams(Arrays.asList(params));
        this.setObjectParams(null);
    }

    public NotificationMessage(final String methodName, final Map<String, Object> params) {
        super((Long)null);

        this.setMethodName(methodName);
        this.setArrayParams(null);
        this.setObjectParams(params);
    }

    public String getMethodName() {
        return this.methodName;
    }

    public boolean hasArrayParams() {
        return this.arrayParams != null;
    }

    public boolean hasObjectParams() {
        return this.objectParams != null;
    }

    public List<Object> getArrayParams() {
        if (this.arrayParams == null) {
            return null;
        }
        return Collections.unmodifiableList(this.arrayParams);
    }

    public Map<String, Object> getObjectParams() {
        if (this.objectParams == null) {
            return null;
        }
        return Collections.unmodifiableMap(this.objectParams);
    }

    @Override
    public JSONObject toJson() {
        final JSONObject object = super.toJson();

        try {
            object.put(JSON_STRATUM_KEY_METHOD, this.getMethodName());

            if (this.hasObjectParams()) {
                object.put(JSON_STRATUM_KEY_PARAMS, new JSONObject(this.getObjectParams()));
            } else {
                final JSONArray jsonParams = new JSONArray();
                for (Object param : this.getArrayParams()) {
                    jsonParams.put(param);
                }
                object.put(JSON_STRATUM_KEY_PARAMS, jsonParams);
            }
        } catch (JSONException ex) {
            throw new RuntimeException(
                "Unexpected exception while constructing JSON object: " + ex.getMessage(),
                ex
            );
        }

        return object;
    }

    protected void setMethodName(String methodName) {
        if (methodName == null) {
            throw new IllegalArgumentException("methodName cannot be null.");
        }

        if (methodName.isEmpty()) {
            throw new IllegalArgumentException("methodName cannot be an empty string.");
        }

        this.methodName = methodName;
    }

    protected void setArrayParams(List<Object> params) {
        this.arrayParams = params == null ? null : new ArrayList<>(params);
    }

    protected void setObjectParams(Map<String, Object> params) {
        this.objectParams = params == null ? null : new LinkedHashMap<>(params);
    }

    @Override
    protected void parseMessage(JSONObject jsonMessage)
        throws MalformedStratumMessageException {
        this.parseMethodName(jsonMessage);
        this.parseParams(jsonMessage);

        // Call superclass last, since it calls validateParsedData()
        super.parseMessage(jsonMessage);
    }

    protected void parseMethodName(final JSONObject jsonMessage)
        throws MalformedStratumMessageException {
        final String methodName;

        if (!jsonMessage.has(JSON_STRATUM_KEY_METHOD)) {
            throw new MalformedStratumMessageException(
                jsonMessage,
                String.format("missing '%s'", JSON_STRATUM_KEY_METHOD)
            );
        }

        try {
            methodName = jsonMessage.getString(JSON_STRATUM_KEY_METHOD);
        } catch (JSONException ex) {
            throw new MalformedStratumMessageException(jsonMessage, ex);
        }

        if (methodName.isEmpty()) {
            throw new MalformedStratumMessageException(
                jsonMessage,
                String.format("empty '%s'", JSON_STRATUM_KEY_METHOD)
            );
        }

        this.setMethodName(methodName);
    }

    protected void parseParams(JSONObject jsonMessage) throws MalformedStratumMessageException {
        if (!jsonMessage.has(JSON_STRATUM_KEY_PARAMS)) {
            throw new MalformedStratumMessageException(
                jsonMessage,
                String.format("missing '%s'", JSON_STRATUM_KEY_PARAMS)
            );
        }

        try {
            final Object rawParams = jsonMessage.get(JSON_STRATUM_KEY_PARAMS);

            if (rawParams instanceof JSONArray) {
                final JSONArray jsonParams = (JSONArray)rawParams;
                final List<Object> params = new ArrayList<>();

                for (int i = 0; i < jsonParams.length(); ++i) {
                    params.add(jsonParams.get(i));
                }

                this.setArrayParams(params);
                this.setObjectParams(null);
                return;
            }

            if (rawParams instanceof JSONObject) {
                final JSONObject jsonParams = (JSONObject)rawParams;
                final Map<String, Object> params = new LinkedHashMap<>();

                for (String key : jsonParams.keySet()) {
                    params.put(key, jsonParams.get(key));
                }

                this.setObjectParams(params);
                this.setArrayParams(null);
                return;
            }

            throw new MalformedStratumMessageException(
                jsonMessage,
                String.format("'%s' must be a JSON array or object", JSON_STRATUM_KEY_PARAMS)
            );
        } catch (JSONException ex) {
            throw new MalformedStratumMessageException(jsonMessage, ex);
        }
    }
}
