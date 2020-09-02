/**
 * 
 */
package dream.first.plugin.support.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.yelong.security.sm3.SM3Utils;
import org.yelong.security.sm4.SM4Utils;
import org.yelong.support.servlet.filter.security.AbstractSecurityFilter;
import org.yelong.support.servlet.filter.security.IntegrityValidationException;
import org.yelong.support.servlet.filter.security.SecurityException;
import org.yelong.support.servlet.filter.security.SecurityHttpServletRequestWrapper;

import dream.first.core.utils.RSAUtils;

/**
 * @since 2.0
 */
public class SecurityFilter extends AbstractSecurityFilter {

	private static final String SM4_KEY = "A1B2C3D4E5F6H7G8";

	private static final String RESPONSE_SM4_KEY = "f1fcafc938e0b5f637f66705597ec460";

	/** 请求参数解密 */
	public static final String PARAM_DECRYPT_MODE = "PARAM_DECRYPT_MODE";

	/** 请求消息体解密 */
	public static final String BODY_DECRYPT_MODE = "BODY_DECRYPT_MODE";

	/** 响应消息体加密 */
	public static final String BODY_ENCRYPT_MODE = "BODY_ENCRYPT_MODE";

	/** 完整性验证 */
	public static final String INTEGRITY_VALIDATION_MODE = "INTEGRITY_VALIDATION_MODE";

	/** 完整性数据标识 */
	public static final String OPER_DATA_SIGN = "OPER_DATA_SIGN";

	/** sm4密钥key */
	public static final String SM4_ENCODE_KEY = "SM4_ENCODE_KEY";

	/** 不需要解密的字段 */
	private static final List<String> NOT_DECRYPT_PARAM_KEY = Arrays.asList(PARAM_DECRYPT_MODE, BODY_DECRYPT_MODE,
			INTEGRITY_VALIDATION_MODE, OPER_DATA_SIGN, BODY_ENCRYPT_MODE, SM4_ENCODE_KEY);

	private static final Logger LOGGER = LoggerFactory.getLogger(Logger.class);

	@Override
	public boolean isParamDecrypt(HttpServletRequest request) {
		return Boolean.valueOf(request.getHeader(PARAM_DECRYPT_MODE))
				|| Boolean.valueOf(request.getParameter(PARAM_DECRYPT_MODE));
	}

	@Override
	public boolean isBodyDecrypt(HttpServletRequest request) {
		return Boolean.valueOf(request.getHeader(BODY_DECRYPT_MODE))
				|| Boolean.valueOf(request.getParameter(BODY_DECRYPT_MODE));
	}

	@Override
	public boolean isIntegrityValidation(HttpServletRequest request) {
		return Boolean.valueOf(request.getHeader(INTEGRITY_VALIDATION_MODE))
				|| Boolean.valueOf(request.getParameter(INTEGRITY_VALIDATION_MODE));
	}

	@Override
	public byte[] decryptBody(HttpServletRequest request, byte[] body) throws SecurityException {
		try {
			return SM4Utils.decodeByHexStr(new String(body), SM4_KEY).getBytes();
		} catch (IOException e) {
			throw new SecurityException(e);
		}

	}

	/**
	 * 暂未测试
	 */
	@Override
	public boolean integrityValidation(SecurityHttpServletRequestWrapper request) throws IntegrityValidationException {
//		Enumeration<String> pm = request.getParameterNames();
//
//		String queryStr = "";
//		while(pm.hasMoreElements()) {
//			String key = (String) pm.nextElement();
//			String value = request.getParameter(key);
//			if (!OPER_DATA_SIGN.equals(key)) {
//				queryStr += key + "=" + value + "&";
//			}
//		}
//
//		if (StringUtils.isNotBlank(queryStr)) {
//			queryStr = queryStr.substring(0, queryStr.length() - 1);
//			String []params = queryStr.split("&");
//			Arrays.sort(params, new Comparator<String>(){
//
//				@Override
//				public int compare(String a, String b) {
//					// TODO Auto-generated method stub
//					String k1 = a.split("=")[0].toLowerCase();
//					String k2 = b.split("=")[0].toLowerCase();
//					return k1.compareTo(k2);
//				}
//
//			});
//			queryStr = "";
//			for (String p : params) {
//				if (!p.contains(OPER_DATA_SIGN)) {
//					queryStr += p + "&";
//				}
//			}
//			queryStr = queryStr.substring(0, queryStr.length() - 1);
//			///queryStr = queryStr.replace("\n", " ");
//			//通过sm3进行完整性验证。失败则返回
//			if(!SM3Utils.verify(queryStr, request.getParameter(OPER_DATA_SIGN))){
//				return false;
//			}
//		}
//		Enumeration<String> pm = request.getParameterNames();
		Enumeration<String> pm = request.getSourceParameterNames();

		String queryStr = "";
		while (pm.hasMoreElements()) {
			String key = (String) pm.nextElement();
			// 前台使用的加密后的值进行拼接的
			String value = request.getSourceParameter(key);
			if (!"OPER_DATA_SIGN".equals(key) && !"PARAM_DECRYPT_MODE".equals(key)
					&& !"INTEGRITY_VALIDATION_MODE".equals(key) && !"OPER_USER".equals(key)) {
				queryStr += key + "=" + value + "&";
			}
		}
		if (StringUtils.isNotBlank(queryStr)) {
			queryStr = queryStr.substring(0, queryStr.length() - 1);
			String[] params = queryStr.split("&");
			Arrays.sort(params, new Comparator<String>() {

				@Override
				public int compare(String a, String b) {
					// TODO Auto-generated method stub
					String k1 = a.split("=")[0].toLowerCase();
					String k2 = b.split("=")[0].toLowerCase();
					return k1.compareTo(k2);
				}

			});
			queryStr = "";
			for (String p : params) {
				if (!p.contains("OPER_DATA_SIGN")) {
					queryStr += p + "&";
				}
			}
			queryStr = queryStr.substring(0, queryStr.length() - 1);
			/// queryStr = queryStr.replace("\n", " ");
			// 通过sm3进行完整性验证。失败则返回
			if (!SM3Utils.verify(queryStr, request.getParameter("OPER_DATA_SIGN"))) {
				return false;
			}

			/*
			 * 原md5完整性验证，现在更改为sm3完整性验证 String sign =
			 * SecurityUtils.encryptMD5(queryStr).toLowerCase(); if
			 * (!sign.equals(req.getParameter("OPER_DATA_SIGN"))) {
			 * ((HttpServletResponse)response).setStatus(801); return; }
			 */

		}
		return true;
	}

	public void integrityValidationExceptionProcessor(IntegrityValidationException e, HttpServletResponse response)
			throws IOException {
		response.setStatus(HttpStatus.OK.value()); // 设置状态码
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE); // 设置ContentType
		response.setCharacterEncoding("UTF-8"); // 避免乱码
		response.getWriter()
				.write("{" + "    \"msg\": \"数据在传输过程中被篡改，操作终止！如有问题，请与客服联系！\"," + "    \"success\": false" + "}");
		LOGGER.error("", e);

	}

	@Override
	public Map<String, String[]> decryptParam(HttpServletRequest request, Map<String, String[]> parameterMap)
			throws SecurityException {
		String sm4Key = null;
		try {
			sm4Key = getSm4Key(request);
		} catch (Exception e1) {
			throw new SecurityException(e1);
		}
		Map<String, String[]> newParam = new HashMap<String, String[]>(parameterMap.size());
		try {
			for (Entry<String, String[]> entry : parameterMap.entrySet()) {
				String k = entry.getKey();
				String[] v = entry.getValue();
				if (NOT_DECRYPT_PARAM_KEY.contains(k)) {
					newParam.put(k, parameterMap.get(k));
				} else {
					String[] value = v;
					String[] newValue = new String[value.length];
					if (null != value && value.length >= 0) {
						for (int i = 0; i < value.length; i++) {
							String str = value[i];
							if (StringUtils.isNotEmpty(str)) {
								newValue[i] = SM4Utils.decodeByHexStr(str, sm4Key);
							} else {
								newValue[i] = str;
							}
						}
					}
					newParam.put(k, newValue);
				}
			}
		} catch (IOException e) {
			throw new SecurityException(e);
		}
		return newParam;
	}

	@Override
	public void decryptExceptionProcessor(SecurityException e, HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write("解密参数异常");
	}

	@Override
	public boolean isResponseEncrypt(HttpServletRequest request) {
		return Boolean.valueOf(request.getHeader(BODY_ENCRYPT_MODE));
	}

	@Override
	public byte[] responseEncrypt(byte[] content) throws IOException {
		// 响应内容明文
		String contentStr = new String(content);
		// 响应内容密文
		String contentEncode = SM4Utils.encodeByHexStr(contentStr, RESPONSE_SM4_KEY);
		// sm3完整性验证字符
		String sm3Ciphertext = SM3Utils.encrypt(RESPONSE_SM4_KEY + contentEncode);
		// 响应内容+sm3完整性密文+sm4秘钥
		return (contentEncode + sm3Ciphertext + RESPONSE_SM4_KEY).getBytes();
	}

	private String getSm4Key(HttpServletRequest request) throws Exception {
		// sm4密钥
		Object sm4KeyObj = request.getParameter(SM4_ENCODE_KEY);
		String sm4Key = "";
		// 获取密钥
		if (sm4KeyObj instanceof Object[]) {
			Object[] objs = (Object[]) sm4KeyObj;
			sm4Key = String.valueOf(objs[0]);
		} else {
			sm4Key = (String) sm4KeyObj;
		}
		if (StringUtils.isEmpty(sm4Key)) {// sm4Key如果为空则设置为默认密钥
			sm4Key = SM4_KEY;
		} else {// 如果不为空，则解密该密钥
			sm4Key = RSAUtils.decodeJsValue(sm4Key);
		}
		return sm4Key;
	}

}
