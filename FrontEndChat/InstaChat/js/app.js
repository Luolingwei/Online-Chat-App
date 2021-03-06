window.app = {
	
	
	// 后端服务器地址
	serverUrl: "http://192.168.1.178:8080",
	
	// 图片服务器地址
	imgServerUrl: "http://192.168.1.178:8080",
	
	// netty后端服务发布地址
	nettyServerUrl: "ws://192.168.1.178:8088/ws",
	
	/**
	 * @param {Object} str
	 * 判断字符串是否为空
	 * true: 非空
	 * false: 空
	 */
	isNotNull: function(str){
		if (str!=null && str!="" && str!=undefined){
			return true;
		}
		return false;
		
	},
	
	/**
	 * @param {Object} msg
	 * @param {Object} type
	 * 封装消息提示框，默认mui的消息提示不支持自定义icon和居中，使用h5+的toast
	 */
	showToast: function(msg, type){
		plus.nativeUI.toast(msg, 
		{icon: "image/" + type + ".png", verticalAlign:"center"})
	},
	
	// 存储user对象到本地缓存
	setUserGlobalInfo: function(user) {
		var userInfoStr = JSON.stringify(user);
		plus.storage.setItem("userInfo",userInfoStr);
	},
	
	// 从本地缓存读取user对象
	getUserGlobalInfo: function() {
		var userInfoStr = plus.storage.getItem("userInfo");
		return JSON.parse(userInfoStr);
	},
	
	// 登出后移除用户全局对象
	userLogout: function(){
		plus.storage.removeItem("userInfo");
	},
	
	// 保存当前用户的所有联系人列表
	setContactList: function(contactList){
		var contactListStr = JSON.stringify(contactList);
		plus.storage.setItem("contactList",contactListStr);
	},
	
	// 获取当前用户的所有联系人列表
	getContactList: function(){
		var contactListStr = plus.storage.getItem("contactList");
		if (!this.isNotNull(contactListStr)){
			return [];
		}
		return JSON.parse(contactListStr);
	},
	
	// 根据id从缓存获取friend信息
	getFriendFromContactList: function(friendId){
		var contactListStr = plus.storage.getItem("contactList");
		
		// 判断是否为空
		if (this.isNotNull(contactListStr)){
			// 不为空则根据id返回用户信息
			var contactList = JSON.parse(contactListStr);
			for (var i=0; i<contactList.length; i++){
				var friend = contactList[i];
				if (friend.friendUserId == friendId){
					return friend;
					break;
				}
			}
		} else {
			// 为空则返回null
			return null;
		}

	},
	
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag  判断本条消息是我发送的还是朋友发送的 (1: 我, 2: 朋友)
	 * 保存用户的聊天记录
	 */
	saveUserChatHistory: function(myId, friendId, msg, flag){
		var me = this;
		var chatKey = "chat-" + myId + "-" + friendId;
		
		// 从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if (me.isNotNull(chatHistoryListStr)){
			// 如果记录不为空
			chatHistoryList = JSON.parse(chatHistoryListStr);
		} else {
			// 如果为空，那么就是[]
			chatHistoryList = [];
		}
		
		// 构建聊天记录对象
		var singleMsg = new me.ChatHistory(myId, friendId, msg, flag);
		
		// 向List中追加Msg对象
		chatHistoryList.push(singleMsg);
		
		// 保存到缓存
		plus.storage.setItem(chatKey,JSON.stringify(chatHistoryList));
		
		
	},
	
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag
	 * 获取用户聊天记录
	 */
	getUserChatHistory: function(myId, friendId){
		
		var me = this;
		var chatKey = "chat-" + myId + "-" + friendId;
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if (me.isNotNull(chatHistoryListStr)){
			// 如果记录不为空
			chatHistoryList = JSON.parse(chatHistoryListStr);
		} else {
			// 如果为空，那么就是[]
			chatHistoryList = [];
		}
		
		return chatHistoryList;
	},
	
	// 删除我和朋友的聊天记录
	delUserChatHistory: function(myId, friendId){
		
		var chatKey = "chat-" + myId + "-" + friendId;
		plus.storage.removeItem(chatKey);

	},
	
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead
	 * 聊天记录的快照，仅仅保存和每个朋友聊天的最后一条消息
	 */
	saveUserChatSnapshot: function(myId, friendId, msg, isRead){
		var me = this;
		var chatKey = "chat-snapshot-" + myId;
		
		// 从本地缓存获取聊天记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		
		if (me.isNotNull(chatSnapshotListStr)){
			// 如果记录不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			// 循环快照list，判断当前friendId是否存在list中，如果存在，删除它，不存在则不处理
			for (var i=0; i<chatSnapshotList.length;i++){
				if (chatSnapshotList[i].friendId == friendId){
					// 删除已存在的friendId对应的快照对象
					chatSnapshotList.splice(i,1);
					break;
				}
			}
			
		} else {
			// 如果为空，那么就是[]
			chatSnapshotList = [];
		}
		
		// 构建聊天快照对象
		var singleSnapshot = new me.ChatSnapshot(myId, friendId, msg, isRead);
		
		// 向List中的头部追加Snapshot对象
		chatSnapshotList.unshift(singleSnapshot);
		
		// 保存到缓存
		plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
		
	},
	
	// 获取用户快照记录
	getUserChatSnapshot: function(myId){
		var me = this;
		var chatKey = "chat-snapshot-" + myId;
		
		// 从本地缓存获取聊天记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)){
			// 如果记录不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			
		} else {
			// 如果为空，那么就是[]
			chatSnapshotList = [];
		}
		
		return chatSnapshotList;
		
	},
	
	// 删除本地的用户快照记录
	delUserChatSnapshot: function(myId, friendId){
		var me = this;
		var chatKey = "chat-snapshot-" + myId;
		
		// 从本地缓存获取聊天记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		
		if (me.isNotNull(chatSnapshotListStr)){
			// 如果记录不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			// 循环快照list，判断当前friendId是否存在list中，如果存在，删除它，不存在则不处理
			for (var i=0; i<chatSnapshotList.length;i++){
				if (chatSnapshotList[i].friendId == friendId){
					// 删除已存在的friendId对应的快照对象
					chatSnapshotList.splice(i,1);
					break;
				}
			}
			
		} else {
			// 如果为空，那么就是[]
			return;
		}
		
		// 保存到缓存
		plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
		
	},
	
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * 标记未读消息为已读状态
	 */
	readUserChatSnapshot: function(myId, friendId){
		var me = this;
		var chatKey = "chat-snapshot-" + myId;
		
		// 从本地缓存获取聊天记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)){
			// 如果记录不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			// 循环查找是否存在friendId，如果有则删除原位置的snapshot，并重新插入snapshot
			for (var i=0; i<chatSnapshotList.length; i++){
				var item = chatSnapshotList[i];
				if (item.friendId == friendId){
					item.isRead = true;
					chatSnapshotList.splice(i,1,item); // 替换原有的快照
					break;
				}
			}
			// 保存到缓存
			plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
			
		} else {
			// 如果为空，不需要任何操作
			return;
		}
		
	},
	
	/**
	 * 和后端枚举类型对应
	 */
	CONNECT: 1,  //"第一次(或重连)初始化连接"
	CHAT: 2, //"聊天消息"
	SIGNED: 3, //"消息签收"
	KEEPALIVE: 4, //"客户端保持心跳"
	PULL_FRIEND: 5, //重新拉取好友
	
	/**
	 * 和后端的ChatMsg对象保持一致
	 * @param {Object} senderId
	 * @param {Object} receiverId
	 * @param {Object} msg
	 * @param {Object} msgId
	 */
	ChatMsg: function(senderId,receiverId,msg,msgId){
		
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.msg = msg;
		this.msgId = msgId;
	},
	
	/**
	 * 和后端的DataContent对象保持一致
	 * @param {Object} action
	 * @param {Object} chatMsg
	 * @param {Object} extend
	 */
	DataContent: function(action,chatMsg,extend){
		
		this.action = action;
		this.chatMsg = chatMsg;
		this.extend = extend;
	},
	
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag
	 * 单个聊天记录对象
	 */
	ChatHistory: function(myId, friendId, msg, flag){
		
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.flag = flag;
	},
	
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead
	 * 快照对象
	 */
	ChatSnapshot: function(myId, friendId, msg, isRead){
		
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.isRead = isRead;
	},
	
	
	
}