package org.sample.custom.audit.logger.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceHolder {
    private static final ServiceHolder instance = new ServiceHolder();
    private static final Log log = LogFactory.getLog(ServiceHolder.class);

    private RealmService realmService;

    private ServiceHolder() {
    }

    public static ServiceHolder getInstance() {
        return instance;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}
