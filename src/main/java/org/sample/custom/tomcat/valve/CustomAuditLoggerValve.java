package org.sample.custom.tomcat.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sample.custom.audit.logger.CustomAuditLogger;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CustomAuditLoggerValve extends ValveBase {
    private static final Log log = LogFactory.getLog(CustomAuditLoggerValve.class);

    @Override
    public void invoke(final Request request, final Response response) throws ServletException, IOException {
        try {
            final String userAgent = request.getHeader(Constants.USER_AGENT);
            final String forwardedFor = request.getHeader(Constants.X_FORWARDED_FOR);
            final String referer = request.getHeader(Constants.REFERER);

            if (userAgent != null) {
                MDC.put(Constants.USER_AGENT, userAgent);
            }
            if (forwardedFor != null) {
                MDC.put(Constants.X_FORWARDED_FOR, forwardedFor);
            }
            if (referer != null) {
                MDC.put(Constants.REFERER, referer);
            }

            // Call the next valve in the pipeline. The response will be populated
            // after this call returns.
            getNext().invoke(request, response);

            // Now the response object should have the status code
            // This line is reached after the Identity Server executed (including CustomAuditLogger)
            // has likely completed for this specific HTTP request processing path.
            MDC.put(Constants.HTTP_STATUS_CODE, Integer.toString(response.getStatus()));
        } catch (Throwable e) {
            // Prevent any errors from propagating and failing healthy requests to the client
            log.debug("Error while extracting HTTP request/response data", e);
        } finally {
            try {
                // Trigger the logging on a finally block to ensure it is also executed if the request fails
                CustomAuditLogger.logFromMDCData();
            } catch (Throwable e) {
                // Prevent any errors from propagating and failing healthy requests to the client
                log.debug("Error while logging request data", e);
            }
            // Unset the MDC thread locals after use (request should be completed) to avoid memory leaks
            unsetMDCThreadLocals();
        }
    }

    private void unsetMDCThreadLocals() {
        Constants.REMOVAL_LIST.forEach(MDC::remove);
    }

    public static final class Constants {
        public static final String USER_AGENT = "User-Agent";
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String REFERER = "Referer";
        public static final String INSTANT = "Instant";
        public static final String USER_NAME = "UserName";
        public static final String AUTHENTICATED = "Authenticated";
        public static final String LOG_MESSAGE = "LogMessage";
        public static final String HTTP_STATUS_CODE = "HttpStatusCode";

        public static final List<String> REMOVAL_LIST = Arrays.asList(
                USER_AGENT,
                X_FORWARDED_FOR,
                REFERER,
                INSTANT,
                USER_NAME,
                AUTHENTICATED,
                LOG_MESSAGE,
                HTTP_STATUS_CODE
        );
    }
}
