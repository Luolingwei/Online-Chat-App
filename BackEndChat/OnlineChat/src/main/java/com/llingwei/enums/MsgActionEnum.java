package com.llingwei.enums;


public enum MsgActionEnum {
	
	CONNECT(1, "initial connection"),
	CHAT(2, "chat msg"),
	SIGNED(3, "msg signed"),
	KEEPALIVE(4, "keep active"),
	PULL_FRIEND(5, "pull friend list");
	
	public final Integer type;
	public final String content;
	
	MsgActionEnum(Integer type, String content){
		this.type = type;
		this.content = content;
	}
	
	public Integer getType() {
		return type;
	}  
}
