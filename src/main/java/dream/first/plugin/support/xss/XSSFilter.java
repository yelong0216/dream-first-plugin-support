/**
 * 
 */
package dream.first.plugin.support.xss;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @since 2.0
 */
public class XSSFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(req);
		chain.doFilter(xssRequest, response);
	}

}
