package org.sample.custom.audit.logger.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.HashMap;
import java.util.Map;

public final class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    private Utils() {
        // Prevent instantiation
    }

    public static String getUserStoreDomain(final org.wso2.carbon.user.api.UserStoreManager userStoreManager) {
        if (userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager) {
            final String domainNameProperty = ((org.wso2.carbon.user.core.UserStoreManager)userStoreManager)
                    .getRealmConfiguration()
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            if (StringUtils.isBlank(domainNameProperty)) {
                return IdentityUtil.getPrimaryDomainName();
            } else {
                return domainNameProperty;
            }
        } else {
            return null;
        }
    }

    public static String getTenantDomain(final org.wso2.carbon.user.api.UserStoreManager userStoreManager) throws IdentityEventException {
        try {
            return IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());
        } catch (UserStoreException e) {
            log.error("Error while getting tenant domain for user store manager: " + userStoreManager, e);
            return null;
        }
    }

    public static Map<String, Object> getParamsFromProperties(final Map<String, Object> properties) {
        final Map<String, Object> params = safeCastToMapStringObject(properties.get(IdentityEventConstants.EventProperty.PARAMS));
        return params != null ? params : new HashMap<>();
    }

    public static AuthenticationContext getAuthenticationContextFromProperties(final Map<String, Object> properties) {
        final Object contextObject = properties.get(IdentityEventConstants.EventProperty.CONTEXT);

        if (contextObject instanceof AuthenticationContext) {
            return (AuthenticationContext) contextObject;
        } else {
            return new AuthenticationContext();
        }
    }

    public static User getUserFromParams(final Map<String, Object> params) {
        final Object userObject = params.get(FrameworkConstants.AnalyticsAttributes.USER);

        if (userObject instanceof User) {
            return (User) userObject;
        } else {
            return new User();
        }
    }

    public static AuthenticatorStatus getAuthenticatorStatusFromProperties(final Map<String, Object> properties) {
        final Object authenticationStatus = properties.get(IdentityEventConstants.EventProperty.AUTHENTICATION_STATUS);

        if (authenticationStatus instanceof AuthenticatorStatus) {
            return (AuthenticatorStatus) properties.get(IdentityEventConstants.EventProperty.AUTHENTICATION_STATUS);
        } else {
            return AuthenticatorStatus.FAIL;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> safeCastToMapStringObject(final Object obj) {
        if (!(obj instanceof Map)) {
            return null;
        }

        final Map<?, ?> map = (Map<?, ?>) obj;

        for (final Object key : map.keySet()) {
            if (!(key instanceof String)) {
                return null;
            }
        }

        return (Map<String, Object>) map;
    }

    public static Map<String, Object> safeConvertToMapStringObject(final Object obj) {
        if (!(obj instanceof Map)) {
            return null;
        }

        final Map<?, ?> sourceMap = (Map<?, ?>) obj;
        final Map<String, Object> resultMap = new HashMap<>();

        for (final Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            final Object key = entry.getKey();

            if (!(key instanceof String)) {
                return null;
            }

            resultMap.put((String) key, entry.getValue());
        }

        return resultMap;
    }
}
