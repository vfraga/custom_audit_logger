package org.sample.custom.audit.logger.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
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

    public static String getUserStoreDomain(org.wso2.carbon.user.api.UserStoreManager userStoreManager) {
        String domainNameProperty = null;
        if (userStoreManager instanceof org.wso2.carbon.user.core.UserStoreManager) {
            domainNameProperty = ((org.wso2.carbon.user.core.UserStoreManager)
                    userStoreManager).getRealmConfiguration()
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            if (StringUtils.isBlank(domainNameProperty)) {
                domainNameProperty = IdentityUtil.getPrimaryDomainName();
            }
        }
        return domainNameProperty;
    }

    public static String getTenantDomain(org.wso2.carbon.user.api.UserStoreManager userStoreManager) throws IdentityEventException {
        try {
            return IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());
        } catch (UserStoreException e) {
            throw new IdentityEventException(e.getMessage(), e);
        }
    }

    public static AuthenticationContext getAuthenticationContextFromProperties(final Map<String, Object> properties) {
        return (AuthenticationContext) properties.get(IdentityEventConstants.EventProperty.CONTEXT);
    }

    public static Map<String, Object> getParamsFromProperties(final Map<String, Object> properties) {
        final Map<String, Object> params = safeCastToMapStringObject(properties.get(IdentityEventConstants.EventProperty.PARAMS));
        return params != null ? params : new HashMap<>();
    }

    public static AuthenticatorStatus getAuthenticatorStatusFromProperties(final Map<String, Object> properties) {
        return (AuthenticatorStatus) properties.get(IdentityEventConstants.EventProperty.AUTHENTICATION_STATUS);
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
