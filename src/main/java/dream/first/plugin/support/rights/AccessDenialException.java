/**
 * 
 */
package dream.first.plugin.support.rights;

import dream.first.core.exception.RequestException;

/**
 * 越权访问
 * 
 * @since 2.0
 */
public class AccessDenialException extends RequestException {

	private static final long serialVersionUID = 2645319618800119492L;

	public static final int STATUS = 901;

	public AccessDenialException() {
		super(STATUS);
	}

	public AccessDenialException(String message) {
		super(STATUS, message);
	}

}
