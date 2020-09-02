/**
 * 
 */
package dream.first.plugin.support.log;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dream.first.core.platform.log.model.Log;

/**
 * @since 2.0
 */
public class LogRecordUtils {

	/** 是否记录日志 */
	public static final String LOG_IS_RECORD_LOG = "LOG_IS_RECORD_LOG";

	/** 记录的日志 */
	public static final String LOG_RECORD_LOG = "LOG_RECORD_LOG";

	private static ThreadLocal<Log> LOG = new ThreadLocal<Log>();

	/**
	 * 默认的事件类型（操作日志）
	 */
	private static String defaultEventType = "02";

	/**
	 * 设置当前请求的日志描述 调用此方法设置后替换源默认描述
	 * 
	 * @param logDesc 日志描述
	 */
	public static void setLogDesc(String logDesc) {
		getRecordLog().setLogDesc(logDesc);
	}

	/**
	 * 设置是否需要记录日志 此方法在 {@link LogFilter#isRecordLog(HttpServletRequest)}方法为true时才会生效
	 * 
	 * @param isRecordLog
	 */
	public static void setRecordLog(boolean isRecordLog) {
		HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes()))
				.getRequest();
		request.setAttribute(LOG_IS_RECORD_LOG, isRecordLog);
	}

	/**
	 * 设置日志的操作用户名称 默认的是当前请求用户
	 * 
	 * @param userName 用户名称
	 */
	public static void setLogUserName(String userName) {
		getRecordLog().setUserName(userName);
	}

	/**
	 * 设置日志操作模块。这默认是空的
	 * 
	 * @param operModule 操作的模块
	 */
	public static void setLogOperatorModule(String operModule) {
		getRecordLog().setOperModule(operModule);
	}

	/**
	 * 设置日志的事件类型
	 * 
	 * @param eventType 事件类型
	 */
	public static void setEventType(String eventType) {
		getRecordLog().setEventType(eventType);
	}

	/**
	 * 设置记录的日志
	 * 
	 * @param log log
	 */
	public static void setRecordLog(Log log) {
//		HttpServletRequest request = ((ServletRequestAttributes)(RequestContextHolder.currentRequestAttributes())).getRequest();
//		request.setAttribute(LOG_RECORD_LOG,log);
		LOG.set(log);
	}

	/**
	 * 获取记录的日志
	 * 
	 * @return log
	 */
	public static Log getRecordLog() {
//		HttpServletRequest request = ((ServletRequestAttributes)(RequestContextHolder.currentRequestAttributes())).getRequest();
//		Log log = (Log) request.getAttribute(LOG_RECORD_LOG);
		Log log = LOG.get();
		if (null == log) {
			log = new Log();
			setRecordLog(log);
		}
		return log;
	}

	public static void removeLog() {
		LOG.remove();
	}

	public static void setDefaultEventType(String defaultEventType) {
		LogRecordUtils.defaultEventType = defaultEventType;
	}

	public static String getDefaultEventType() {
		return defaultEventType;
	}

	public static void setResponseParams(String responseParams) {
		getRecordLog().setResponseParams(responseParams);
	}

}
