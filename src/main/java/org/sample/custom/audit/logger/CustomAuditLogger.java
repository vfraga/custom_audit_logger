package org.sample.custom.audit.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sample.custom.tomcat.valve.CustomAuditLoggerValve.Constants;
import org.slf4j.MDC;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.user.core.UserStoreManager;

import java.time.Instant;

public class CustomAuditLogger extends AbstractIdentityUserOperationEventListener {
    private static final Log log = LogFactory.getLog(CustomAuditLogger.class);
    private static final int DEFAULT_EXECUTION_ORDER = 99;

    public static void logFromMDCData() {
        final String logMessage = MDC.get(Constants.LOG_MESSAGE);

        if (logMessage != null) {
            final String userName = MDC.get(Constants.USER_NAME);
            final String instant = MDC.get(Constants.INSTANT);
            final String userAgent = MDC.get(Constants.USER_AGENT);
            final String referer = MDC.get(Constants.REFERER);
            final String xForwardedFor = MDC.get(Constants.X_FORWARDED_FOR);
            final String statusCode = MDC.get(Constants.HTTP_STATUS_CODE);

            log.info(String.format(logMessage, userName, instant, userAgent, referer, xForwardedFor, statusCode));
        } else {
            log.debug("Cannot log with a null message.");
        }
    }

    @Override
    public int getExecutionOrderId() {
        final int result = super.getOrderId();  // use the value from the event listener configuration, if any
        return result <= 0 ? DEFAULT_EXECUTION_ORDER : result;  // otherwise use the default value
    }

    @Override
    public boolean doPreAuthenticate(final String userName, final Object credential, final UserStoreManager userStoreManager) {
        if (isEnable()) {  // if event listener is enabled in the IS deployment configuration
            MDC.put(Constants.LOG_MESSAGE, LogMessage.PRE_AUTHENTICATE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC);
            MDC.put(Constants.AUTHENTICATED, String.valueOf(false));
            MDC.put(Constants.USER_NAME, userName);
            MDC.put(Constants.INSTANT, Instant.now().toString());  // UTC timestamp string
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticate(final String userName, final boolean authenticated, final UserStoreManager userStoreManager) {
        if (isEnable()) {  // if event listener is enabled in the IS deployment configuration
            final String message = authenticated ?
                    LogMessage.POST_AUTHENTICATE_SUCCESS_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC :
                    LogMessage.POST_AUTHENTICATE_FAILURE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC;

            MDC.put(Constants.AUTHENTICATED, String.valueOf(authenticated));
            MDC.put(Constants.LOG_MESSAGE, message);
            MDC.put(Constants.USER_NAME, userName);
            MDC.put(Constants.INSTANT, Instant.now().toString());  // UTC timestamp string
        }

        return true;
    }

    private static final class LogMessage {
        private static final String PRE_AUTHENTICATE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC =
                "Login attempt for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s";

        private static final String POST_AUTHENTICATE_SUCCESS_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC =
                "Login successful for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s";

        private static final String POST_AUTHENTICATE_FAILURE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC =
                "Login failed for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s";
    }
}
