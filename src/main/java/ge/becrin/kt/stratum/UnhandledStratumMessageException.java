package ge.becrin.kt.stratum;

import ge.becrin.kt.stratum.message.NotificationMessage;
import ge.becrin.kt.stratum.message.RequestMessage;

/**
 * Exception thrown when there is no handler registered for a specific type of Stratum message.
 *
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class UnhandledStratumMessageException
    extends Exception {
    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = -5169028037089628215L;

    /**
     * The Stratum request that went unhandled.
     */
    private final RequestMessage unhandledRequest;

    private final NotificationMessage unhandledNotification;

    /**
     * Initializes a new exception that indicates that the specified Stratum request was not handled.
     *
     * @param request The Stratum request that went unhandled.
     */
    public UnhandledStratumMessageException(final RequestMessage request) {
        super(
            String.format(
                "No handler registered for method \"%s\", specified by Stratum JSON message: %s",
                request.getMethodName(),
                request.toJson().toString()
            )
        );

        this.unhandledRequest = request;
        this.unhandledNotification = null;
    }

    public UnhandledStratumMessageException(final NotificationMessage notification) {
        super(
            String.format(
                "No handler registered for method \"%s\", specified by Stratum JSON message: %s",
                notification.getMethodName(),
                notification.toJson().toString()
            )
        );

        this.unhandledNotification = notification;
        this.unhandledRequest = null;
    }

    /**
     * @return The Stratum request that went unhandled.
     */
    public RequestMessage getUnhandledRequest() {
        return this.unhandledRequest;
    }

    public NotificationMessage getUnhandledNotification() {
        return this.unhandledNotification;
    }
}
