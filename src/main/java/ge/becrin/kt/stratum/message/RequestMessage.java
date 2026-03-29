package ge.becrin.kt.stratum.message;

import ge.becrin.kt.stratum.MalformedStratumMessageException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>Java representation of a Stratum request message.</p>
 *
 * <p>Request messages must include the following:</p>
 *
 * <ul>
 *  <li>an {@code id} field, but it may be {@code null} if a response to the
 *      request is not expected or required.</li>
 *
 *  <li>a {@code method} field, which must not be {@code null} and must specify
 *      the name of the method being invoked.</li>
 *
 *  <li>a {@code params} field, which can be an empty array if the method being
 *      invoked takes no parameters.</li>
 * </ul>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class RequestMessage
    extends Message {
    /**
     * Constant for the name of the {@code method} field in the JSON object for this message.
     */
    protected static final String JSON_STRATUM_KEY_METHOD = "method";

    /**
     * Constant for the name of the {@code params} field in the JSON object for this message.
     */
    protected static final String JSON_STRATUM_KEY_PARAMS = "params";

    /**
     * Static counter for generating unique Stratum request IDs.
     */
    private static final AtomicLong nextRequestId;

    /**
     * The name of the method being invoked.
     */
    private String methodName;

    /**
     * The list of parameters being supplied to the method.
     */
    private List<Object> arrayParams;

    //TODO Override original with map behavior instead of this.
    private Map<String, Object> objectParams;

    static {
        nextRequestId = new AtomicLong(1);
    }

    /**
     * Gets a unique identifier than can be used to identify the next Stratum request.
     *
     * @return A unique identifier for the next Stratum request.
     */
    public static Long getNextRequestId() {
        return nextRequestId.getAndIncrement();
    }

    /**
     * Constructor for {@link RequestMessage} that initializes a new instance from information in the
     * included JSON message.
     *
     * @param jsonMessage The JSON message object.
     *
     * @throws MalformedStratumMessageException If the provided JSON message object is not a properly-formed Stratum message or cannot be
     * understood.
     */
    public RequestMessage(final JSONObject jsonMessage)
        throws MalformedStratumMessageException {
        super(jsonMessage);
    }

    /**
     * Constructor for {@link RequestMessage} that initializes a new instance having the specified ID,
     * method, and parameters.
     *
     * @param id The unique identifier for the message. This may be {@code null}.
     * @param methodName The name of the method being invoked on the remote side. This cannot be {@code null}.
     * @param params The parameters being passed to the method.
     *
     * @throws IllegalArgumentException If {@code methodName} is {@code null}.
     */
    public RequestMessage(final Long id, final String methodName, final Object... params) {
        super(id);

        this.setMethodName(methodName);
        this.setArrayParams(Arrays.asList(params));
        this.setObjectParams(null);
    }

    public RequestMessage(final Long id, final String methodName, final Map<String, Object> params) {
        super(id);

        this.setMethodName(methodName);
        this.setArrayParams(null);
        this.setObjectParams(params);
    }

    /**
     * Gets the name of the method being invoked.
     *
     * @return The name of the method.
     */
    public String getMethodName() {
        return this.methodName;
    }

    public boolean hasArrayParams() {
        return this.arrayParams != null;
    }

    public boolean hasObjectParams() {
        return this.objectParams != null;
    }

    /**
     * Gets the list of parameters being passed to the remote method.
     *
     * @return An unmodifiable copy of the parameters being passed to the remote method.
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * Sets the name of the method being invoked.
     *
     * @param methodName The name of the method.
     */
    protected void setMethodName(String methodName) {
        if (methodName == null) {
            throw new IllegalArgumentException("methodName cannot be null.");
        }

        if (methodName.isEmpty()) {
            throw new IllegalArgumentException("methodName cannot be an empty string.");
        }

        this.methodName = methodName;
    }

    /**
     * Sets the list of parameters being passed to the remote method.
     *
     * @param params The list of parameters. The list is used live, in-place.
     */
    protected void setArrayParams(List<Object> params) {
        this.arrayParams = params == null ? null : new ArrayList<>(params);
    }

    protected void setObjectParams(Map<String, Object> params) {
        this.objectParams = params == null ? null : new LinkedHashMap<>(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseMessage(JSONObject jsonMessage)
        throws MalformedStratumMessageException {
        this.parseMethodName(jsonMessage);
        this.parseParams(jsonMessage);

        // Call superclass last, since it calls validateParsedData()
        super.parseMessage(jsonMessage);
    }

    /**
     * Parses-out the {@code method} field from the message.
     *
     * @param jsonMessage The message to parse.
     *
     * @throws MalformedStratumMessageException If the provided JSON message object is not a properly-formed Stratum message or cannot be
     * understood.
     */
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

    /**
     * Parses-out the {@code params} field from the message.
     *
     * @param jsonMessage The message to parse.
     *
     * @throws MalformedStratumMessageException If the provided JSON message object is not a properly-formed Stratum message or cannot be
     * understood.
     */
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
