/**
 * 
 */
package dream.first.plugin.support.xss;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.yelong.support.servlet.wrapper.HttpServletRequestReuseWrapper;

/**
 * @since 2.0
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestReuseWrapper {

	public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
	}

	@Override
	public String getParameter(String name) {
		String xssName = XssShieldUtil.stripXss(name);
		String value = super.getParameter(xssName);
		String xssValue = XssShieldUtil.stripXss(value);
		return xssValue;
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(XssShieldUtil.stripXss(name));
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				values[i] = XssShieldUtil.stripXss(values[i]);
			}
		}
		return values;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> paramMap = new HashMap<String, String[]>(super.getParameterMap());

		for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
			entry.setValue(xssEncode(entry.getValue()));
		}

		return paramMap;
	}

	private static String[] xssEncode(String[] ss) {
		if (null == ss)
			return ss;
		for (int i = 0; i < ss.length; i++) {
			ss[i] = XssShieldUtil.stripXss(ss[i]);
		}
		return ss;
	}

	@Override
	public String getHeader(String name) {

		String value = super.getHeader(XssShieldUtil.stripXss(name));
		if (value != null) {
			value = XssShieldUtil.stripXss(value);
		}
		return value;
	}

}
