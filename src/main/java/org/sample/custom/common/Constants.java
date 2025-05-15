package org.sample.custom.common;

import java.util.Arrays;
import java.util.List;

public final class Constants {
    public static final class MDC {
        public static final String USER_AGENT = "User-Agent";
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String REFERER = "Referer";
        public static final String INSTANT = "Instant";
        public static final String USER_NAME = "UserName";
        public static final String AUTHENTICATED = "Authenticated";
        public static final String LOG_MESSAGE = "LogMessage";
        public static final String HTTP_STATUS_CODE = "HttpStatusCode";
        public static final String ROLE_LIST = "RoleList";
        public static final String GRANT_TYPE = "GrantType";

        public static final List<String> REMOVAL_LIST = Arrays.asList(
                USER_AGENT,
                X_FORWARDED_FOR,
                REFERER,
                INSTANT,
                USER_NAME,
                AUTHENTICATED,
                LOG_MESSAGE,
                HTTP_STATUS_CODE,
                ROLE_LIST,
                GRANT_TYPE
        );
    }

    public static final class LogMessage {
        public static final String AUTHENTICATION_SUCCESS_MESSAGE_UNAME_GTYPE_ATTIME_UAGENT_REF_XFF_SC_RL =
                "Login successful for username '%s' with grant type '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s | Role-List: %s";

        public static final String AUTHENTICATION_FAILURE_MESSAGE_UNAME_GTYPE_ATTIME_UAGENT_REF_XFF_SC =
                "Login failed for username '%s' with grant type '%s' at %s. " +
                        "User Agent: %s | Referer: %s | X-Forwarded-For: %s | Status-Code: %s";
    }
}
