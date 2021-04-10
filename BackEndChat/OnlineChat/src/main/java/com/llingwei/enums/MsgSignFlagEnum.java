package com.llingwei.enums;


public enum MsgSignFlagEnum {
	
	unsign(0, "unsigned"),
	signed(1, "signed");
	
	public final Integer type;
	public final String content;
	
	MsgSignFlagEnum(Integer type, String content){
		this.type = type;
		this.content = content;
	}
	
	public Integer getType() {
		return type;
	}  
}
