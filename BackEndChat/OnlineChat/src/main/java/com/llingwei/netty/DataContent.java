package com.llingwei.netty;


import java.io.Serializable;

public class DataContent implements Serializable {

    private static final long serialVersionUID = 8021381444738260454L;

    private Integer action;  // action type

    private ChatMsg chatMsg;  // chat content

    private String extend;  //extend fields

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

}
