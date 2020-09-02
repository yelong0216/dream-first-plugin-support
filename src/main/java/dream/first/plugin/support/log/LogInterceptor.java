/**
 * 
 */
package dream.first.plugin.support.log;

import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.yelong.commons.lang.annotation.AnnotationUtilsE;
import org.yelong.core.model.service.SqlModelService;
import org.yelong.support.spring.mvc.interceptor.AbstractHandlerInterceptor;

import dream.first.core.login.CurrentLoginUserInfo;
import dream.first.core.login.CurrentLoginUserInfoHolder;
import dream.first.core.model.service.DreamFirstModelService;
import dream.first.core.platform.log.model.Log;
import dream.first.core.platform.module.constants.ModuleLog;
import dream.first.core.platform.module.model.Module;
import dream.first.core.platform.module.utils.ModuleUtils;

/**
 * 日志拦截器 </br>
 * 此拦截器并不会记录日志，只会判断是否记录日志及设置日志属性 </br>
 * 记录日志在 {@link LogFilter}中 </br>
 * 
 * 记录日志范围(包含优先级排序)： </br>
 * 1、请求的handler或其handler的类中使用{@link LogRecord}注解标注，且{@link LogRecord#isRecordLog()}为true
 * </br>
 * 2、请求的路径module存在，且此module支持记录日志 </br>
 * 日志操作人： </br>
 * 1、如果token认证成功则操作人为当前登录人 </br>
 * 2、token未认证则操作人为system </br>
 * 
 * @since 2.0
 */
public class LogInterceptor extends AbstractHandlerInterceptor {

	@Resource
	private DreamFirstModelService modelService;

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if (!(handler instanceof HandlerMethod)) {
			return;
		}
		boolean isRecordLog = false;
		// 防止查询表异常
		LogRecord logRecord = getLogRecord((HandlerMethod) handler);
		if (null != logRecord) {
			if (!logRecord.isRecordLog())
				return;
			isRecordLog = true;
		}
		Module module = ModuleUtils.getModule(request, getModelService());
		if (null != module) {
			String moduleLog = module.getModuleLog();
			if (ModuleLog.RECORD.code().equals(moduleLog)) {
				isRecordLog = true;
			}
		}
		// 不记录日志
		if (!isRecordLog) {
			return;
		}
		LogRecordUtils.setRecordLog(true);
		Log log = LogRecordUtils.getRecordLog();
		String logDesc = log.getLogDesc();
		String operModule = log.getOperModule();
		String userName = log.getUserName();
		String eventType = log.getEventType();

		if (null != logRecord) {
			if (StringUtils.isEmpty(logDesc)) {
				logDesc = logRecord.logDesc();
			}
			if (StringUtils.isEmpty(operModule)) {
				operModule = logRecord.operModule();
			}
			if (StringUtils.isEmpty(userName)) {
				userName = logRecord.userName();
			}
			if (StringUtils.isEmpty(eventType)) {
				eventType = logRecord.eventType();
			}
		}
		if (null != module) {
			if (StringUtils.isEmpty(operModule)) {
				operModule = module.getModuleName();
			}
			if (StringUtils.isEmpty(logDesc)) {
				logDesc = "#{realName}执行了【" + operModule + "】操作。";
			}
		}
		CurrentLoginUserInfo currentLoginUserInfo = CurrentLoginUserInfoHolder.currentLoginUserInfo();
		if (null != currentLoginUserInfo && currentLoginUserInfo.getUser() != null) {
			logDesc = logDesc.replace("#{realName}", currentLoginUserInfo.getUser().getRealName());
			if (StringUtils.isEmpty(userName)) {
				userName = currentLoginUserInfo.getUser().getUsername();
			}
		}
		if (StringUtils.isEmpty(userName)) {
			userName = "system";
		}
		if (StringUtils.isBlank(eventType)) {
			eventType = LogRecordUtils.getDefaultEventType();
		}

		log.setLogDesc(logDesc);
		log.setOperModule(operModule);
		log.setUserName(userName);
		log.setEventType(eventType);

		LogRecordUtils.setRecordLog(log);
		super.afterCompletion(request, response, handler, ex);
	}

	/**
	 * 从处理器、处理器的类、处理器的类的父类中查询{@link LogRecord}注解
	 * 
	 * @param handlerMethod 处理器方法
	 * @return {@link LogRecord}
	 */
	private LogRecord getLogRecord(HandlerMethod handlerMethod) {
		Method method = handlerMethod.getMethod();
		if (method.isAnnotationPresent(LogRecord.class)) {
			return method.getAnnotation(LogRecord.class);
		}
		Class<?> c = method.getDeclaringClass();
		return AnnotationUtilsE.getAnnotation(c, LogRecord.class, true);
	}

	public SqlModelService getModelService() {
		return modelService;
	}

	public void setModelService(DreamFirstModelService modelService) {
		this.modelService = modelService;
	}

}
