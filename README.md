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

### Configuration

Add the below to the `<IS_HOME>/repository/conf/deployment.toml` file:
```toml
[[event_listener]]
id = "custom_audit_logger"
type = "org.wso2.carbon.user.core.listener.UserOperationEventListener"
name = "org.sample.custom.audit.logger.CustomAuditLogger"
order = "9983"
enable = true

[[catalina.valves]]
properties.className = "org.sample.custom.tomcat.valve.CustomAuditLoggerValve"
```

Add the below to the `<IS_HOME>/repository/conf/log4j2.properties` file:
1. Create a Log4J2 Appender [4] named `CUSTOM_AUDIT_LOGFILE` and add it to the existing `appenders` variable:

```properties
appenders = CARBON_CONSOLE, ..., CUSTOM_AUDIT_LOGFILE

appender.CUSTOM_AUDIT_LOGFILE.type = RollingFile
appender.CUSTOM_AUDIT_LOGFILE.name = CUSTOM_AUDIT_LOGFILE
appender.CUSTOM_AUDIT_LOGFILE.fileName = ${sys:carbon.home}/repository/logs/custom_access_log.log
appender.CUSTOM_AUDIT_LOGFILE.filePattern = ${sys:carbon.home}/repository/logs/custom_access_log-%d{MM-dd-yyyy}.%i.log
appender.CUSTOM_AUDIT_LOGFILE.layout.type = PatternLayout
appender.CUSTOM_AUDIT_LOGFILE.layout.pattern = [%X{Correlation-ID}] %mm%n
appender.CUSTOM_AUDIT_LOGFILE.policies.type = Policies
appender.CUSTOM_AUDIT_LOGFILE.policies.time.type = TimeBasedTriggeringPolicy
appender.CUSTOM_AUDIT_LOGFILE.policies.time.interval = 1
appender.CUSTOM_AUDIT_LOGFILE.policies.time.modulate = true
appender.CUSTOM_AUDIT_LOGFILE.policies.size.type = SizeBasedTriggeringPolicy
appender.CUSTOM_AUDIT_LOGFILE.policies.size.size = 10MB
appender.CUSTOM_AUDIT_LOGFILE.strategy.type = DefaultRolloverStrategy
appender.CUSTOM_AUDIT_LOGFILE.strategy.max = 20
appender.CUSTOM_AUDIT_LOGFILE.filter.threshold.type = ThresholdFilter
appender.CUSTOM_AUDIT_LOGFILE.filter.threshold.level = INFO
```

* _The policies for this appender rotate the log file every 10 MB and every day. You can adjust this to your liking, see [5]._
* _You can adjust the appender log pattern (`appender.CUSTOM_AUDIT_LOGFILE.layout.pattern`)  with the pattern converters that adjust best to your requirement, see [6]. It's recommended to keep the `%X{Correlation-ID}` for cross-reference with other log files._

2. Create a Log4J2 Logger [7] named `CUSTOM_AUDIT_LOG` mapped to the `org.sample.custom.audit.logger.CustomAuditLogger` class, set the appender reference to `CUSTOM_AUDIT_LOGFILE`, and add it to the existing `loggers` variable:

```properties
loggers = AUDIT_LOG, . . ., CUSTOM_AUDIT_LOG

logger.CUSTOM_AUDIT_LOG.name = org.sample.custom.audit.logger.CustomAuditLogger
logger.CUSTOM_AUDIT_LOG.level = INFO
logger.CUSTOM_AUDIT_LOG.appenderRef.CUSTOM_AUDIT_LOGFILE.ref = CUSTOM_AUDIT_LOGFILE
logger.CUSTOM_AUDIT_LOG.additivity = false
```

---

- [1] https://is.docs.wso2.com/en/5.10.0/develop/user-store-listeners/
- [2] https://tomcat.apache.org/tomcat-9.0-doc/config/valve.html
- [3] https://tomcat.apache.org/tomcat-9.0-doc/api/org/apache/catalina/Valve.html
- [4] https://logging.apache.org/log4j/2.x/manual/appenders.html
- [5] https://logging.apache.org/log4j/2.x/manual/appenders/rolling-file.html
- [6] https://logging.apache.org/log4j/2.x/manual/pattern-layout.html#converters
- [7] https://logging.apache.org/log4j/2.x/manual/configuration.html#configuring-loggers
