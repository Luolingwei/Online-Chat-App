package com.llingwei.enums;

public enum SearchFriendsStatusEnum {
	
	SUCCESS(0, "OK"),
	USER_NOT_EXIST(1, "No such user..."),
	NOT_YOURSELF(2, "You can not add yourself..."),
	ALREADY_FRIENDS(3, "This user is already your friend...");
	
	public final Integer status;
	public final String msg;
	
	SearchFriendsStatusEnum(Integer status, String msg){
		this.status = status;
		this.msg = msg;
	}
	
	public Integer getStatus() {
		return status;
	}  
	
	public static String getMsgByKey(Integer status) {
		for (SearchFriendsStatusEnum type : SearchFriendsStatusEnum.values()) {
			if (type.getStatus() == status) {
				return type.msg;
			}
		}
		return null;
	}
	
}
