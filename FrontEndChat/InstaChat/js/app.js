window.app = {
	
	// 后端服务器地址
	serverUrl: "http://192.168.1.178:8080",
	
	// 图片服务器地址
	imgServerUrl: "http://192.168.1.178:8080",
	
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
	}
	
	
}