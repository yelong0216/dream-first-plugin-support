/**
 * 
 */
package dream.first.plugin.support.rights;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 权限验证 标注在Controller与Method中。 在Controller上面标注时适用于该类及其子类的所有Method
 * 在Method上面标注优先级大于在Controller上面的
 * 
 * @since 2.0
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface RightsValidate {

	/**
	 * 是否验证权限
	 * 
	 * @return <tt>true</tt> 需要验证
	 */
	boolean validate() default true;

}
