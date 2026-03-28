package ge.becrin.kt.stratum.transport.tcp;

import ge.becrin.kt.stratum.UnhandledStratumMessageException;
import ge.becrin.kt.stratum.message.Message;
import ge.becrin.kt.stratum.message.MessageMarshaller;
import ge.becrin.kt.stratum.transport.ConnectionState;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

/**
 * <p>A generic Stratum TCP socket input thread.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class TcpSocketInputThread
extends Thread {
  /**
   * The logger for this thread.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketInputThread.class);

  /**
   * The TCP message transport.
   */
  private final AbstractTcpMessageTransport transport;

  /**
   * Constructor for {@link TcpSocketInputThread} that initializes the new input thread for the
   * specified Stratum TCP message transport.
   *
   * @param transport
   *   The transport to which the input thread pertains.
   */
  public TcpSocketInputThread(final AbstractTcpMessageTransport transport) {
    this.setName(this.getClass().getSimpleName());
    this.setDaemon(true);

    this.transport = transport;
  }

  /**
   * Runs the input thread, continuously dispatching incoming messages until the TCP connection is
   * closed.
   */
  @Override
  public void run() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.transport.getSocket().getInputStream()))) {
      while (this.transport.isOpen()) {
        final String line = reader.readLine();

        if (line == null) {
            throw new IllegalStateException("Connection closed.");
        }

        final String jsonLine = line.trim();

        if (!jsonLine.isEmpty()) {
          synchronized (this.transport) {
            final ConnectionState   currentState = this.transport.getConnectionState();
            final MessageMarshaller marshaller   = currentState.getMarshaller();
            final List<Message>     messages;

            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("Stratum [in]: " + jsonLine);
            }

            try {
              messages = marshaller.marshalMessages(jsonLine);

              this.transport.receiveMessages(messages);
            }
            catch (UnhandledStratumMessageException ex) {
              this.transport.handleUnsupportedRequest(ex.getUnhandledRequest());
            }
          }
        }
      }
    } catch (final Exception ex) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(
          String.format("Error on connection: %s", ex.getMessage()),
          ex
        );
      }
    } finally {
      this.transport.close();
    }

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Input thread exiting.");
    }
  }
}
