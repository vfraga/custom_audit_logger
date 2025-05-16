package org.sample.custom.audit.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sample.custom.audit.logger.internal.ServiceHolder;
import org.sample.custom.audit.logger.utils.Utils;
import org.sample.custom.common.Constants;
import org.slf4j.MDC;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.wso2.carbon.identity.oauth.common.OAuthConstants.GrantTypes.AUTHORIZATION_CODE;

public class CustomAuditLogger extends AbstractEventHandler {
    private static final Log log = LogFactory.getLog(CustomAuditLogger.class);
    private static final String HANDLER_NAME = "CustomAuditLogger";
    private static final int PRIORITY = 50;

    private static final Set<String> SUBSCRIPTIONS = new HashSet<>(Arrays.asList(
            IdentityEventConstants.EventName.AUTHENTICATION_SUCCESS.name(),
            IdentityEventConstants.EventName.AUTHENTICATION_FAILURE.name(),
            IdentityEventConstants.EventName.AUTHENTICATION_STEP_FAILURE.name()
    ));

    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String AUTH_CODE_RESPONSE_TYPE = "code";

    public static void logFromMDCData() {
        final String logMessage = MDC.get(Constants.MDC.LOG_MESSAGE);

        if (logMessage != null) {
            final String userName = MDC.get(Constants.MDC.USER_NAME);
            final String instant = MDC.get(Constants.MDC.INSTANT);
            final String userAgent = MDC.get(Constants.MDC.USER_AGENT);
            final String referer = MDC.get(Constants.MDC.REFERER);
            final String grantType = MDC.get(Constants.MDC.GRANT_TYPE);

            final String xForwardedFor = MDC.get(Constants.MDC.X_FORWARDED_FOR);
            final String statusCode = MDC.get(Constants.MDC.HTTP_STATUS_CODE);
            final String roleList = MDC.get(Constants.MDC.ROLE_LIST);

            log.info(String.format(logMessage, userName, grantType, instant, userAgent, referer, xForwardedFor, statusCode, roleList));
        } else {
            log.debug("Cannot log with a null message.");
        }
    }

    private static boolean isAuthorizationCode(final AuthenticationContext context) {
        final AuthenticationRequest authRequest = context.getAuthenticationRequest();

        if (authRequest != null) {
            final String[] responseTypes = authRequest.getRequestQueryParam(RESPONSE_TYPE_PARAM);

            if (responseTypes != null) {
                return Arrays.stream(responseTypes)
                        .filter(Objects::nonNull)
                        .anyMatch(responseTypeString -> responseTypeString.contains(AUTH_CODE_RESPONSE_TYPE));
            }
        }

        return false;
    }

    @Override
    public void handleEvent(final Event event) {
        final String eventName = event.getEventName();

        log.debug(eventName + " event received to " + getName() + ".");

        if (!SUBSCRIPTIONS.contains(eventName)) return;

        try {
            final Map<String, Object> eventProperties = event.getEventProperties();
            final AuthenticationContext context = Utils.getAuthenticationContextFromProperties(eventProperties);
            final Map<String, Object> params = Utils.getParamsFromProperties(eventProperties);
            final AuthenticatorStatus status = Utils.getAuthenticatorStatusFromProperties(eventProperties);

            if (isAuthorizationCode(context)) {
                MDC.put(Constants.MDC.GRANT_TYPE, AUTHORIZATION_CODE);
            }

            final boolean authenticated = status == AuthenticatorStatus.PASS;

            MDC.put(Constants.MDC.AUTHENTICATED, String.valueOf(authenticated));
            MDC.put(Constants.MDC.INSTANT, Instant.now().toString());  // UTC timestamp string

            if (authenticated) {
                final AuthenticatedUser user = context.getLastAuthenticatedUser();
                final UserStoreManager userStoreManager = Utils.getUserStoreManagerFromUser(user);
                final String[] roleArray = userStoreManager.getRoleListOfUser(user.getUserName());

                MDC.put(Constants.MDC.USER_NAME, user.getUserName());
                MDC.put(Constants.MDC.ROLE_LIST, Arrays.toString(roleArray));
                MDC.put(Constants.MDC.LOG_MESSAGE, Constants.LogMessage.AUTHENTICATION_SUCCESS_MESSAGE_UNAME_GTYPE_ATTIME_UAGENT_REF_XFF_SC_RL);
            } else {
                final User user = Utils.getUserFromParams(params);

                MDC.put(Constants.MDC.USER_NAME, user.getUserName());
                MDC.put(Constants.MDC.LOG_MESSAGE, Constants.LogMessage.AUTHENTICATION_FAILURE_MESSAGE_UNAME_GTYPE_ATTIME_UAGENT_REF_XFF_SC);
            }

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
}
