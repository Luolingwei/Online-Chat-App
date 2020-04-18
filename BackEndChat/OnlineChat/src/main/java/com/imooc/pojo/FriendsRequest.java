package com.imooc.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "friends_request")
public class FriendsRequest {
    @Id
    private String id;

    @Column(name = "send_user_id")
    private String sendUserId;

    @Column(name = "accept_user_id")
    private String acceptUserId;

    @Column(name = "request_data_time")
    private Date requestDataTime;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return send_user_id
     */
    public String getSendUserId() {
        return sendUserId;
    }

    /**
     * @param sendUserId
     */
    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    /**
     * @return accept_user_id
     */
    public String getAcceptUserId() {
        return acceptUserId;
    }

    /**
     * @param acceptUserId
     */
    public void setAcceptUserId(String acceptUserId) {
        this.acceptUserId = acceptUserId;
    }

    /**
     * @return request_data_time
     */
    public Date getRequestDataTime() {
        return requestDataTime;
    }

    /**
     * @param requestDataTime
     */
    public void setRequestDataTime(Date requestDataTime) {
        this.requestDataTime = requestDataTime;
    }
}