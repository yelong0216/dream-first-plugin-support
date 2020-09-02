/**
 * 
 */
package dream.first.plugin.support.log;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.yelong.core.model.service.ModelService;
import org.yelong.support.servlet.HttpServletUtils;
import org.yelong.support.servlet.filter.log.AbstractLogFilter;
import org.yelong.support.servlet.filter.log.HttpServletLogInfo;

import dream.first.core.platform.log.model.Log;

/**
 * 日志过滤器
 * 
 * @since 2.0
 */
public class LogFilter extends AbstractLogFilter {

	private static final String DEFAULT_LOG_CREATOR = "system";

	private ModelService modelService;

	public LogFilter(ModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	protected boolean isRecordLog(HttpServletRequest request) {
		// 这总会返回true。
		return true;
	}

	@Override
	protected void recordLog(HttpServletLogInfo logInfo, HttpServletRequest request, HttpServletResponse response) {
		// 注意：此方法在执行时处理器已经执行完毕
		Boolean isRecordLog = (Boolean) request.getAttribute(LogRecordUtils.LOG_IS_RECORD_LOG);
		if (null == isRecordLog || !isRecordLog) {
			return;
		}
		Log log = LogRecordUtils.getRecordLog();

		log.setStartTime(DateFormatUtils.format(logInfo.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
		log.setEndTime(DateFormatUtils.format(logInfo.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
		log.setOperTimes(Long.valueOf(logInfo.getOperationTime()).intValue());
		String requestParams = "requestParams:{${requestParams}},requestBody:{${requestBody}}";
		requestParams = requestParams.replace("${requestParams}", HttpServletUtils.getRequestParamsStr(request));
		try {
			requestParams = requestParams.replace("${requestBody}", new String(logInfo.getRequestBody(), "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			requestParams = requestParams.replace("${requestBody}", new String(logInfo.getRequestBody()));
		}
		log.setRequestParams(requestParams);

		// 相应参数。如果响应的是一个文件或者页面，默认不记录这个响应的结果
		if (StringUtils.isBlank(log.getResponseParams())) {
			if (isResponseFileOrView(response)) {
				log.setResponseParams("--");
			}
		}

		if (logInfo.getResponseResult() != null) {
			if (StringUtils.isBlank(log.getResponseParams())) {
				try {
					log.setResponseParams(new String(logInfo.getResponseResult(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.setResponseParams(new String(logInfo.getResponseResult()));
				}
			}
		}
//		log.setRequestPath(request.getRequestURL().toString());
		log.setRequestPath(request.getRequestURI());
		String userIp = "";
		if (StringUtils.isNotEmpty(request.getHeader("X-Forwarded-For"))) {
			userIp = request.getHeader("X-Forwarded-For");
		} else {
			userIp = request.getRemoteAddr();
			if (userIp.equals("0:0:0:0:0:0:0:1") || userIp.equals("127.0.0.1")) {
				try {
					userIp = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		log.setUserIp(userIp);
		log.setCreator(DEFAULT_LOG_CREATOR);
		log.setUpdator(DEFAULT_LOG_CREATOR);
		LogRecordUtils.removeLog();
		modelService.save(log);
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	/**
	 * 是否响应的是一个页面或者文件
	 * 
	 * @param response
	 * @return
	 */
	protected boolean isResponseFileOrView(HttpServletResponse response) {
		String contentType = response.getContentType();
		if (contentType == null) {
			return true;
		}
		if (contentType.contains("application/octet-stream")) {// 文件
			return true;
		}
		if (contentType.contains("html")) {// 页面
			return true;
		}
		return false;
	}

}
