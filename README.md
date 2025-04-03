# Custom Audit Logger

This project includes a custom user operation event listener `CustomAuditLogger` that intercepts 
pre- and post-authentication for auditing purposes, and a Tomcat Valve `RequestDataExtractorValve` for 
retrieving HTTP request information (e.g., HTTP headers) and adding it to SLF4J's Mapped Diagnostic Context (MDC) map 
so that it can be used in `CustomAuditLogger` where such information is not available. 

See [1] for more information on User Operation Listeners, and [2][3] for more information on Tomcat Valves.

This base version logs the following HTTP headers:

- User-Agent
- X-Forwarded-For
- Referer

---

- [1] https://is.docs.wso2.com/en/5.10.0/develop/user-store-listeners/
- [2] https://tomcat.apache.org/tomcat-9.0-doc/config/valve.html
- [3] https://tomcat.apache.org/tomcat-9.0-doc/api/org/apache/catalina/Valve.html