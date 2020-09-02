/**
 * 
 */
package dream.first.plugin.support.log;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
/**
 * 记录日志 标注此注解的Controller或者处理器将根据设置进行记录日志
 * 
 * @since 2.0
 */
public @interface LogRecord {

	/**
	 * 是否记录日志
	 * 
	 * @return <code>true</code> 记录日期
	 */
	boolean isRecordLog() default true;

	/**
	 * 操作模块
	 * 
	 * @return 操作的模块名称
	 */
	String operModule() default "";

	/**
	 * 操作人员姓名
	 * 
	 * @return 操作的用户名称
	 */
	String userName() default "";

	/**
	 * 日志描述
	 * 
	 * @return 日志的描述
	 */
	String logDesc() default "";

	/**
	 * 事件类型
	 * 
	 * @return 事件类型
	 */
	String eventType() default "";
}
