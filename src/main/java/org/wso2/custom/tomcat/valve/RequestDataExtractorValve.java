package org.wso2.custom.tomcat.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RequestDataExtractorValve extends ValveBase {

    @Override
    public void invoke(final Request request, final Response response) throws ServletException, IOException {
        try {
            final String userAgent = request.getHeader(Constants.USER_AGENT);
            final String forwardedFor = request.getHeader(Constants.X_FORWARDED_FOR);
            final String referer = request.getHeader(Constants.REFERER);

            if (userAgent != null) {
                MDC.put(Constants.USER_AGENT, userAgent);
            }
            if (forwardedFor != null) {
                MDC.put(Constants.X_FORWARDED_FOR, forwardedFor);
            }
            if (referer != null) {
                MDC.put(Constants.REFERER, referer);
            }

            getNext().invoke(request, response);
        } finally {
            // Unset the MDC thread locals after use (request should be completed) to avoid memory leaks
            unsetMDCThreadLocals();
        }
    }

    private void unsetMDCThreadLocals() {
        Constants.REMOVAL_LIST.forEach(MDC::remove);
    }

    public static final class Constants {
        public static final String USER_AGENT = "User-Agent";
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String REFERER = "Referer";

        public static final List<String> REMOVAL_LIST = Arrays.asList(
                USER_AGENT,
                X_FORWARDED_FOR,
                REFERER
        );
    }
}
