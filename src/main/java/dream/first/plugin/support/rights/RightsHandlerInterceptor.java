/**
 * 
 */
package dream.first.plugin.support.rights;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.yelong.commons.lang.annotation.AnnotationUtilsE;
import org.yelong.support.spring.mvc.interceptor.AbstractHandlerInterceptor;

import dream.first.core.login.CurrentLoginUserInfo;
import dream.first.core.login.CurrentLoginUserInfoHolder;
import dream.first.core.model.service.DreamFirstModelService;
import dream.first.core.platform.module.model.Module;
import dream.first.core.platform.module.utils.ModuleUtils;
import dream.first.core.platform.user.Users;
import dream.first.core.platform.user.model.User;
import dream.first.plugin.support.log.LogRecordUtils;

/**
 * 权限拦截器
 * 
 * 这个拦截器应该在登录验证拦截器之后
 * 
 * @since 2.0
 */
public class RightsHandlerInterceptor extends AbstractHandlerInterceptor {

	@Resource
	private DreamFirstModelService modelService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		RightsValidate rightsValidate = getHandlerMethodAnnotation((HandlerMethod) handler, RightsValidate.class);
		if (rightsValidate == null) {
			return true;
		}
		if (!rightsValidate.validate()) {
			return true;
		}
		// 该拦截器在登录拦截器之后。如果这里用户为空，说明这个请求不用验证登录
		CurrentLoginUserInfo loginUserInfo = CurrentLoginUserInfoHolder.currentLoginUserInfo();
		if (null == loginUserInfo) {
			// throw new AccessDenialException("未登录的用户不具有权限！");
			return true;
		}
		User user = loginUserInfo.getUser();
		// 超级用户不用验证
		if (Users.isSuper(loginUserInfo.getUser())) {
			return true;
		}
		// 用户拥有的权限（模块id）
		List<String> rights = loginUserInfo.getOpRights();
		if (CollectionUtils.isEmpty(rights)) {
			LogRecordUtils.setRecordLog(true);
			String username = user.getUsername();
			LogRecordUtils.setLogUserName(username);
			LogRecordUtils.setEventType("02");
			LogRecordUtils.setLogDesc("用户【" + user.getRealName() + "】进行越权访问。该用户未被授予任何权限。");
			throw new AccessDenialException("您未被授予任何权限，请联系管理员为您设置权限！");
		}
		// 根据请求获取用户访问的模块
		Module module = ModuleUtils.getModule(request, modelService, true);
		// 不存在该模块时，默认允许
		if (null == module) {
			return true;
		}
		// 不拥有该模块的权限
		if (!rights.contains(module.getId())) {
			LogRecordUtils.setRecordLog(true);
			String username = user.getUsername();
			LogRecordUtils.setLogUserName(username);
			LogRecordUtils.setEventType("02");
			LogRecordUtils.setLogDesc("用户【" + user.getRealName() + "】进行越权访问。");
			throw new AccessDenialException("您没有访问该功能的权限！");
		}
		return true;
	}

	protected RightsValidate getRightsValidate(HandlerMethod handler) {
		Method method = handler.getMethod();
		if (method.isAnnotationPresent(RightsValidate.class)) {
			return method.getAnnotation(RightsValidate.class);
		}
		Class<?> c = handler.getBeanType();
		return AnnotationUtilsE.getAnnotation(c, RightsValidate.class, true);
	}

	/**
	 * 获取handler方法上面的的注解。如果方法上面没有到，则根据该方法所属的类层级递归查找
	 * 
	 * @param <A>
	 * @param handler    处理器
	 * @param annotation 注解
	 * @return annotation
	 * @since 1.0.5
	 */
	protected <A extends Annotation> A getHandlerMethodAnnotation(HandlerMethod handler, Class<A> annotation) {
		return getHandlerMethodAnnotation(handler, annotation, true);
	}

	/**
	 * 获取handler方法上面的的注解。
	 * 
	 * @param <A>
	 * @param handler        处理器
	 * @param annotation     注解
	 * @param classRecursive 类递归。<tt>true</tt> 如果方法上面没有到，则根据该方法所属的类层级递归查找
	 * @return annotation
	 * @since 1.0.5
	 */
	protected <A extends Annotation> A getHandlerMethodAnnotation(HandlerMethod handler, Class<A> annotation,
			boolean classRecursive) {
		Method method = handler.getMethod();
		if (method.isAnnotationPresent(annotation)) {
			return method.getAnnotation(annotation);
		}
		if (!classRecursive) {
			return null;
		}
		Class<?> c = handler.getBeanType();
		return AnnotationUtilsE.getAnnotation(c, annotation, true);
	}

}
