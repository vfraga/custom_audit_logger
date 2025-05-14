package org.sample.custom.audit.logger.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.sample.custom.audit.logger.CustomAuditLogger;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticationService;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "custom.audit.logger.bundle",
        immediate = true)
public class CustomAuditLoggerBundleComponent {
    private static final Log log = LogFactory.getLog(CustomAuditLoggerBundleComponent.class);

    @Activate
    protected void activate(final ComponentContext context) {
        final CustomAuditLogger customAuditLogger = new CustomAuditLogger();

        final ServiceRegistration<?> customAuditLoggerServiceRegistrationResult = context.getBundleContext()
                .registerService(AbstractEventHandler.class.getName(), customAuditLogger, null);

        if (customAuditLoggerServiceRegistrationResult == null) {
            log.error("Error registering CustomAuditLogger as a UserOperationEventListener.");
        } else {
            log.info("CustomAuditLogger successfully registered as a UserOperationEventListener.");
        }

        log.info("Custom bundle activated.");
    }

    @Deactivate
    protected void deactivate(final ComponentContext ignored) {
        log.info("Custom bundle deactivated.");
    }

    @Reference(
            name = "RealmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(final RealmService realmService) {
        log.debug("Setting the Realm Service.");
        ServiceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(final RealmService realmService) {
        log.debug("Unsetting the Realm Service.");
        ServiceHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "identity.application.authentication.framework",
            service = ApplicationAuthenticationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationAuthenticationService"
    )
    protected void setApplicationAuthenticationService(final ApplicationAuthenticationService ignored) {
        // do nothing: waiting for ApplicationAuthenticationService to initialise
    }

    protected void unsetApplicationAuthenticationService(final ApplicationAuthenticationService ignored) {
        // do nothing: method declaration for the unbind action for setApplicationAuthenticationService
    }

    @Reference(
            name = "identity.core.init.event.service",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(final IdentityCoreInitializedEvent ignored) {
        // do nothing: waiting for IdentityCoreInitializedEvent to initialise
    }

    protected void unsetIdentityCoreInitializedEventService(final IdentityCoreInitializedEvent ignored) {
        // do nothing: method declaration for the unbind action for setIdentityCoreInitializedEventService
    }
}
