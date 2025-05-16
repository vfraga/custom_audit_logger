package org.sample.custom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sample.custom.audit.logger.utils.Utils;
import org.sample.custom.common.Constants;
import org.slf4j.MDC;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.grant.jwt.JWTConstants;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.core.UserStoreManager;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

public class CustomOAuthTokenInterceptor extends AbstractOAuthEventInterceptor {
    private static final Log log = LogFactory.getLog(CustomOAuthTokenInterceptor.class);

    private static boolean isPasswordGrant(final OAuth2AccessTokenReqDTO tokenReqDTO) {
        return OAuthConstants.GrantTypes.PASSWORD.equals(tokenReqDTO.getGrantType());
    }

    private static boolean isJWTBearerGrant(final OAuth2AccessTokenReqDTO tokenReqDTO) {
        return JWTConstants.OAUTH_JWT_BEARER_GRANT_TYPE.equals(tokenReqDTO.getGrantType());
    }

    @Override
    public boolean isEnabled() {
        // enabled by default
        return true;
    }

    @Override
    public void onPreTokenIssue(final OAuth2AccessTokenReqDTO tokenReqDTO,
                                final OAuthTokenReqMessageContext tokReqMsgCtx,
                                final Map<String, Object> params) {
        // Log grant type
        MDC.put(Constants.MDC.GRANT_TYPE, tokenReqDTO.getGrantType());
    }

    @Override
    public void onPostTokenIssue(final OAuth2AccessTokenReqDTO tokenReqDTO,
                                 final OAuth2AccessTokenRespDTO tokenRespDTO,
                                 final OAuthTokenReqMessageContext tokReqMsgCtx,
                                 final Map<String, Object> params) {
        if (!isPasswordGrant(tokenReqDTO) && !isJWTBearerGrant(tokenReqDTO)) return;

        try {
            final boolean authenticated = isTokenRequestSuccessful(tokReqMsgCtx) && !tokenRespDTO.isError();

            MDC.put(Constants.MDC.AUTHENTICATED, String.valueOf(authenticated));
            MDC.put(Constants.MDC.INSTANT, Instant.now().toString());  // UTC timestamp string

            final AuthenticatedUser user = getAuthenticatedUser(tokReqMsgCtx);

            if (authenticated) {
                final UserStoreManager userStoreManager = Utils.getUserStoreManagerFromUser(user);
                final String[] roleArray = userStoreManager.getRoleListOfUser(user.getUserName());

                MDC.put(Constants.MDC.USER_NAME, user.getUserName());
                MDC.put(Constants.MDC.ROLE_LIST, Arrays.toString(roleArray));
                MDC.put(Constants.MDC.LOG_MESSAGE, Constants.LogMessage.AUTHENTICATION_SUCCESS_MESSAGE_UNAME_GTYPE_ATTIME_UAGENT_REF_XFF_SC_RL);
            } else {
                final String username = user != null ? user.getUserName() : getResourceOwnerUsername(tokReqMsgCtx);

                MDC.put(Constants.MDC.USER_NAME, username);
                MDC.put(Constants.MDC.LOG_MESSAGE, Constants.LogMessage.AUTHENTICATION_FAILURE_MESSAGE_UNAME_GTYPE_ATTIME_UAGENT_REF_XFF_SC);
            }
        } catch (Throwable e) {
            log.error(String.format("Error while intercepting token request for %s grant type: ", tokenReqDTO.getGrantType()), e);
        }
    }

    private String getResourceOwnerUsername(final OAuthTokenReqMessageContext tokReqMsgCtx) {
        return tokReqMsgCtx.getOauth2AccessTokenReqDTO().getResourceOwnerUsername();
    }

    private AuthenticatedUser getAuthenticatedUser(final OAuthTokenReqMessageContext tokReqMsgCtx) {
        return tokReqMsgCtx.getAuthorizedUser();
    }

    private boolean isTokenRequestSuccessful(final OAuthTokenReqMessageContext tokReqMsgCtx) {
        return tokReqMsgCtx.getAuthorizedUser() != null;
    }
}
