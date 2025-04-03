package org.sample.custom.audit.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.user.core.UserStoreManager;
import org.sample.custom.tomcat.valve.RequestDataExtractorValve.Constants;

import java.time.Instant;

public class CustomAuditLogger extends AbstractIdentityUserOperationEventListener {
    private static final Log log = LogFactory.getLog(CustomAuditLogger.class);
    private static final int DEFAULT_EXECUTION_ORDER = 99;

    @Override
    public int getExecutionOrderId() {
        int result = super.getOrderId();  // use the value from the event listener configuration, if any
        return result <= 0 ? DEFAULT_EXECUTION_ORDER : result;  // otherwise use the default value
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential, UserStoreManager userStoreManager) {
        if (isEnable()) {  // if event listener is enabled in the IS deployment configuration
            log.info(String.format(LogMessage.PRE_AUTHENTICATE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF,
                    userName,
                    Instant.now().toString(),  // UTC timestamp string
                    MDC.get(Constants.USER_AGENT),
                    MDC.get(Constants.REFERER),
                    MDC.get(Constants.X_FORWARDED_FOR)
            ));
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated, UserStoreManager userStoreManager) {
        if (isEnable()) {  // if event listener is enabled in the IS deployment configuration
            log.info(String.format(authenticated ?
                            LogMessage.POST_AUTHENTICATE_SUCCESS_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF :
                            LogMessage.POST_AUTHENTICATE_FAILURE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF,
                    userName,
                    Instant.now().toString(),  // UTC timestamp string
                    MDC.get(Constants.USER_AGENT),
                    MDC.get(Constants.REFERER),
                    MDC.get(Constants.X_FORWARDED_FOR)
            ));
        }

        return true;
    }

    private static final class LogMessage {
        private static final String PRE_AUTHENTICATE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF =
                "Login attempt for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s";

        private static final String POST_AUTHENTICATE_SUCCESS_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF =
                "Login successful for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s";

        private static final String POST_AUTHENTICATE_FAILURE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF =
                "Login failed for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s";
    }
}
