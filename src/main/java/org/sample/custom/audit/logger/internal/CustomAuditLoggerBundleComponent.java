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
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.sample.custom.audit.logger.CustomAuditLogger;

@Component(
        name = "custom.audit.logger.bundle",
        immediate = true)
public class CustomAuditLoggerBundleComponent {
    private static final Log log = LogFactory.getLog(CustomAuditLoggerBundleComponent.class);

    @Activate
    protected void activate(final ComponentContext context) {
        final CustomAuditLogger customAuditLogger = new CustomAuditLogger();

        final ServiceRegistration<?> customAuditLoggerServiceRegistrationResult = context.getBundleContext()
                .registerService(UserOperationEventListener.class.getName(), customAuditLogger, null);

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
            name = "user.operation.event.listener.service",
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserOperationEventListenerService")
    protected synchronized void setUserOperationEventListenerService(final UserOperationEventListener ignored) {
        // do nothing: waiting for component to initialise so that we can register the custom event listener in the UMListenerServiceComponent
    }

    protected synchronized void unsetUserOperationEventListenerService(final UserOperationEventListener ignored) {
        // do nothing: method declaration for the unbind action for setUserOperationEventListenerService
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(final IdentityCoreInitializedEvent ignored) {
        // do nothing: waiting for component to initialise
    }

    protected void unsetIdentityCoreInitializedEventService(final IdentityCoreInitializedEvent ignored) {
        // do nothing: method declaration for the unbind action for setIdentityCoreInitializedEventService
    }
}
