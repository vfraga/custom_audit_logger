package org.sample.custom.audit.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sample.custom.audit.logger.internal.ServiceHolder;
import org.sample.custom.audit.logger.utils.Utils;
import org.sample.custom.tomcat.valve.CustomAuditLoggerValve.Constants;
import org.slf4j.MDC;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.core.UserStoreManager;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomAuditLogger extends AbstractEventHandler {
    private static final Log log = LogFactory.getLog(CustomAuditLogger.class);
    private static final String HANDLER_NAME = "CustomAuditLogger";
    private static final int PRIORITY = 50;

    private static final Set<String> SUBSCRIPTIONS = new HashSet<>(Arrays.asList(
            IdentityEventConstants.EventName.AUTHENTICATION_SUCCESS.name(),
            IdentityEventConstants.EventName.AUTHENTICATION_FAILURE.name()
    ));

    public static void logFromMDCData() {
        final String logMessage = MDC.get(Constants.LOG_MESSAGE);

        if (logMessage != null) {
            final String userName = MDC.get(Constants.USER_NAME);
            final String instant = MDC.get(Constants.INSTANT);
            final String userAgent = MDC.get(Constants.USER_AGENT);
            final String referer = MDC.get(Constants.REFERER);
            final String xForwardedFor = MDC.get(Constants.X_FORWARDED_FOR);
            final String statusCode = MDC.get(Constants.HTTP_STATUS_CODE);
            final String roleList = MDC.get(Constants.ROLE_LIST);

            log.info(String.format(logMessage, userName, instant, userAgent, referer, xForwardedFor, statusCode, roleList));
        } else {
            log.debug("Cannot log with a null message.");
        }
    }

    @Override
    public void handleEvent(final Event event) throws IdentityEventException {
        final String eventName = event.getEventName();

        log.debug(eventName + " event received to " + getName() + ".");

        if (!SUBSCRIPTIONS.contains(eventName)) return;

        try {
            final Map<String, Object> eventProperties = event.getEventProperties();
            final AuthenticationContext context = Utils.getAuthenticationContextFromProperties(eventProperties);
            final Map<String, Object> params = Utils.getParamsFromProperties(eventProperties);
            final AuthenticatorStatus status = Utils.getAuthenticatorStatusFromProperties(eventProperties);

            final boolean authenticated = status == AuthenticatorStatus.PASS;
            final AuthenticatedUser user = context.getLastAuthenticatedUser();

            final UserStoreManager userStoreManager;

            if (user.getUserStoreDomain() != null) {
                userStoreManager = ServiceHolder.getInstance()
                        .getRealmService()
                        .getBootstrapRealm()
                        .getUserStoreManager()
                        .getSecondaryUserStoreManager(user.getUserStoreDomain());
            } else {
                userStoreManager = ServiceHolder.getInstance()
                        .getRealmService()
                        .getBootstrapRealm()
                        .getUserStoreManager();
            }

            final String[] roleArray = userStoreManager.getRoleListOfUser(user.getUserName());

            final String message = authenticated ?
                    LogMessage.POST_AUTHENTICATE_SUCCESS_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC_RL :
                    LogMessage.POST_AUTHENTICATE_FAILURE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC_RL;

            MDC.put(Constants.AUTHENTICATED, String.valueOf(authenticated));
            MDC.put(Constants.LOG_MESSAGE, message);
            MDC.put(Constants.USER_NAME, user.getUserName());
            MDC.put(Constants.INSTANT, Instant.now().toString());  // UTC timestamp string
            MDC.put(Constants.ROLE_LIST, Arrays.toString(roleArray));

        } catch (final Throwable e) {
            log.error("Error while handling event: " + eventName, e);
        }
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public int getPriority(final MessageContext messageContext) {
        return PRIORITY;
    }

    private static final class LogMessage {
        private static final String POST_AUTHENTICATE_SUCCESS_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC_RL =
                "Login successful for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s | Role-List: %s";

        private static final String POST_AUTHENTICATE_FAILURE_MESSAGE_UNAME_ATTIME_UAGENT_REF_XFF_SC_RL =
                "Login failed for username '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s | Role-List: %s";
    }
}
