package com.llingwei.utils;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

public class MD5Utils {

	/**
	 * @Description: conduct MD5 encryption for String
	 */
	public static String getMD5Str(String strValue) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		String newstr = Base64.encodeBase64String(md5.digest(strValue.getBytes()));
		return newstr;
	}

	public static void main(String[] args) {
		try {
			String md5 = getMD5Str("llingwei");
			System.out.println(md5);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
