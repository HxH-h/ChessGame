<template>
  <div class="friend-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">

      </div>
      <div class="header-center">
        <h1>好友列表</h1>
      </div>
      <div class="header-right">
        <el-input
            v-model="searchQuery"
            placeholder="搜索好友"
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch" icon="Search">搜索</el-button>
          </template>
        </el-input>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧好友列表 -->
      <div class="friend-list-container">
        <h2 class="list-title">好友列表</h2>
        <el-divider />
        <div v-if="filteredFriends.length === 0" class="empty-list">
          <el-empty description="暂无好友" />
        </div>
        <div v-else>
          <div
              v-for="friend in filteredFriends"
              :key="friend.id"
              class="friend-item"
              @click="showFriendPopup(friend)"
          >
            <div class="friend-avatar">
              <img :src="friend.photo" alt="好友头像" class="avatar-img" />
              <span :class="getOnlineStatusClass(friend)" />
            </div>
            <div class="friend-info">
              <div class="friend-name">{{ friend.username }}</div>
              <div class="friend-status">{{ friend.status}}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-container">
        <div v-if="currentChatFriend" class="chat-area">
          <div class="chat-header">
            <div class="chat-avatar">
              <img :src="currentChatFriend.photo" alt="聊天对象头像" class="chat-avatar-img" />
            </div>
            <div class="chat-title">{{ currentChatFriend.username }}</div>
            <el-button @click="closeChat" icon="Close" circle size="small" />
          </div>
          <el-divider />
          <div class="chat-messages" ref="messageContainer">
            <div v-for="msg in chatMessages" :key="msg.id" class="chat-message" :class="msg.isMe ? 'my-message' : 'friend-message'">
              <div v-if="!msg.isMe" class="friend-msg-avatar">
                <img :src="currentChatFriend.photo" alt="好友头像" class="msg-avatar-img" />
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
            </div>
          </div>
          <el-divider />
          <div class="chat-input-area">
            <el-input
                v-model="messageContent"
                type="textarea"
                placeholder="输入消息..."
                class="chat-input"
                @keyup.enter.ctrl="sendMessage"
            />
            <el-button @click="sendMessage" icon="Send" class="send-button" :disabled="!messageContent.trim()" />
          </div>
        </div>
        <div v-else class="empty-chat">
          <el-empty description="请选择好友开始聊天" />
        </div>
      </div>
    </div>

    <!-- 好友操作弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        title="好友操作"
        width="30%"
        center
        :before-close="handleDialogClose"
    >
      <div v-if="currentFriend" class="popup-content">
        <div class="popup-avatar">
          <img :src="currentFriend.photo" alt="好友头像" class="popup-avatar-img" />
          <div class="popup-status">
            <span v-if="currentFriend.status !== 'offline'" class="online-tag">在线</span>
            <span v-else class="offline-tag">离线</span>
          </div>
        </div>
        <div class="popup-info">
          <div class="popup-name">{{ currentFriend.username }}</div>
          <div class="popup-desc">{{ currentFriend.status}}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="openChat">发送消息</el-button>
          <el-button
              v-if="currentFriend && currentFriend.status !== 'playing'"
              type="success"
              @click="inviteBattle"
              :disabled="currentFriend.status === 'playing' || battleDialogVisible"
          >
            邀请对战
          </el-button>
          <el-button
              v-if="currentFriend && currentFriend.status === 'playing'"
              type="warning"
              disabled
          >
            游戏中
          </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 对战邀请弹窗 -->
    <el-dialog
        v-model="battleDialogVisible"
        title="邀请对战"
        width="30%"
        center
    >
      <div class="battle-invite-content">
        <p>确定要邀请 <span class="invited-friend">{{ currentFriend.nickname }}</span> 进行五子棋对战吗？</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="battleDialogVisible = false">取消</el-button>
          <el-button type="success" @click="sendBattleInvitation">发送邀请</el-button>
        </span>
      </template>
    </el-dialog>




  </div>
</template>

<script>
import {ref, computed, onMounted, watch} from 'vue';
import { ElMessage } from 'element-plus';
import {useStore} from "vuex";
import useWsStore from '@/store/WsSocket';
import router from "@/router";
import { get } from '@/ts/request'
import {FRIEND_URL ,HIS_MESSAGE_URL} from '@/ts/url'
export default {
  name: 'FriendList',
  setup() {

    // 数据状态
    const store = useStore()
    // 获取websocket
    const wsstore = useWsStore();

    const searchQuery = ref('');
    const dialogVisible = ref(false);
    const battleDialogVisible = ref(false);
    const currentFriend = ref(null);
    const currentChatFriend = ref(null);
    const invitedFriend = ref('');
    const messageContent = ref('');
    const chatMessages = ref([]);



    const friends = ref([]);
    onMounted(async () => {
      watch(() => wsstore.msg, (newValue, oldValue) => {
        const data = JSON.parse(newValue)
        if (data.event == "syncMessage") receiveMessage(data)

      })
      getFriend()
    })

    // 获取好友信息
    const getFriend = async () => {
      let friendList = await get(FRIEND_URL, store.state.accessToken)
      console.log(friendList.data)
      friends.value = friendList.data
    }
    // 获取在线状态类
    const getOnlineStatusClass = (friend) => {
      if (friend.status === 'offline') return 'offline-dot';
      return friend.status === 'playing' ? 'gaming-dot' : 'online-dot';
    };

    // 计算属性 - 排序和过滤后的好友列表（先在线后离线，再按昵称排序）
    const filteredFriends = computed(() => {
      const filtered = friends.value.filter(friend =>
          friend.username.includes(searchQuery.value)
      );

      return filtered.sort((a, b) => {
        // 在线好友排在前面
        if (a.isOnline !== b.isOnline) {
          return b.isOnline - a.isOnline;
        }
        // 按昵称排序
        return a.username.localeCompare(b.username);
      });
    });

    // 处理搜索
    const handleSearch = () => {
      // 搜索逻辑已在计算属性中处理
    };

    // 显示好友弹窗
    const showFriendPopup = (friend) => {
      currentFriend.value = friend;
      dialogVisible.value = true;
    };

    // 处理对话框关闭
    const handleDialogClose = () => {
      dialogVisible.value = false;
      currentFriend.value = null;
    };

    // 打开聊天界面
    const openChat = () => {
      currentChatFriend.value = currentFriend.value;
      dialogVisible.value = false;
      getHistoryMessages(currentChatFriend.value.username)
    };

    // 关闭聊天界面
    const closeChat = () => {
      currentChatFriend.value = null;
      messageContent.value = '';
    };
    // 获取历史消息
    const getHistoryMessages = async (username) => {
      let resp = await get(HIS_MESSAGE_URL, store.state.accessToken, username)
      if (resp.code == 2061){
        ElMessage({
          type: 'error',
          message: '获取历史消息失败'
        });
        return
      }
      console.log(resp.data)
      chatMessages.value = resp.data.map(msg => ({
        id: Date.now(),
        content: msg.message,
        isMe: msg.isme, // 确保这个字段在 resp.data 中有定义或根据逻辑判断
        time: msg.time
      }));
    }
    // 发送消息
    const sendMessage = () => {
      if (!messageContent.value.trim()) {
        ElMessage.warning('输入消息不能为空')
        return;
      }

      const now = new Date();

      //const time = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      const time1 = now.toLocaleString('zh', { hour12: false }).replaceAll('/', '-')
      const time2 = now.toLocaleTimeString('zh', { hour12: false })

      wsstore.sendMsg({
        event: "chat",
        sendname: store.state.username,
        receivename:currentChatFriend.value.username,
        message:messageContent.value.trim(),
        datetime:time1
      })

      chatMessages.value.push({
        id: Date.now(),
        content: messageContent.value,
        isMe: true,
        time: time2
      });

      // 滚动到底部
      scrollToBottom();

      messageContent.value = '';
    };
    // 收到消息
    const receiveMessage = (data) => {
      chatMessages.value.push({
        id: Date.now(),
        content: data.syncMessage.message,
        isMe: false,
        time: data.syncMessage.time
      });
      scrollToBottom();
    }
    // 邀请对战
    const inviteBattle = () => {
      console.log(currentFriend.value.status)
      if (currentFriend.value.status === 'offline') {
        ElMessage.warning('对方不在线，无法邀请对战')
        return;
      }else if (currentFriend.value.status !== 'online') {
        ElMessage.warning('对方正在游戏中，无法邀请对战')
        return;
      }

      battleDialogVisible.value = true;
      console.log(battleDialogVisible.value)
    };

    // 发送对战邀请
    const sendBattleInvitation = () => {
      wsstore.sendMsg({
        event: "invite",
        sender: store.state.username,
        invite:currentFriend.value.username,
      })
      ElMessage.warning(`已向 ${currentFriend.value.username} 发送五子棋对战邀请`)
      battleDialogVisible.value = false;

      var data = { 'event': 'invite' }
      router.push({ name: 'chessgame', state: { data } })

    };



    // 滚动到底部
    const scrollToBottom = () => {
      const container = document.querySelector('.chat-messages');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    };

    // 挂载时滚动到底部
    onMounted(() => {
      scrollToBottom();
    });

    return {
      searchQuery,
      dialogVisible,
      battleDialogVisible,
      inviteBattle,
      sendBattleInvitation,
      currentFriend,
      invitedFriend,
      currentChatFriend,
      messageContent,
      friends,
      filteredFriends,
      chatMessages,
      handleSearch,
      showFriendPopup,
      handleDialogClose,
      openChat,
      closeChat,
      sendMessage,
      getOnlineStatusClass,
    };
  }
};
</script>

<style scoped>
.friend-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 10;
  background-color: #fff;
}

.header-left, .header-center, .header-right {
  display: flex;
  align-items: center;
}

.header-left {
  width: 10%;
}

.header-center {
  width: 80%;
  text-align: center;
}

.header-right {
  width: 30%;
  justify-content: flex-end;
}

.search-input {
  width: 200px;
  margin-right: 10px;
}

.main-content {
  display: flex;
  flex: 1;
  padding: 10px;
  background-color: #f5f7fa;
}

.friend-list-container {
  flex: 3;
  margin-right: 10px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 15px;
  overflow-y: auto;
}

.chat-container {
  flex: 5;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.list-title {
  display: inline-block;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.friend-item:hover {
  background-color: #f5f7fa;
}

.friend-avatar {
  width: 50px;
  height: 50px;
  margin-right: 15px;
  position: relative;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.online-dot, .offline-dot, .gaming-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
}

.online-dot {
  background-color: #67c23a;
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.3);
}

.offline-dot {
  background-color: #909399;
}

.gaming-dot {
  background-color: #e6a23c;
  box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.3);
}

.friend-info {
  flex: 1;
}

.friend-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.friend-status {
  font-size: 14px;
  color: #909399;
}

.empty-list {
  padding: 30px 0;
  text-align: center;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

.chat-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.chat-title {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.chat-message {
  display: flex;
  margin-bottom: 15px;
  padding: 0 10px;
}

.my-message {
  justify-content: flex-end;
}

.friend-message {
  justify-content: flex-start;
}

.friend-msg-avatar {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.msg-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
  margin-bottom: 3px;
}

.my-message .message-text {
  background-color: #409EFF;
  color: #fff;
}

.friend-message .message-text {
  background-color: #f5f7fa;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.friend-message .message-time {
  text-align: left;
}

.chat-input-area {
  display: flex;
  padding-top: 10px;
}

.chat-input {
  flex: 1;
  margin-right: 10px;
}

.send-button {
  height: 40px;
}

.empty-chat {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.popup-content {
  text-align: center;
  padding: 20px 0;
}

.popup-avatar {
  display: inline-block;
  position: relative;
  margin-bottom: 15px;
}

.popup-avatar-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.popup-status {
  position: absolute;
  bottom: 0;
  right: 0;
}

.online-tag, .offline-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.online-tag {
  background-color: #e6f7ff;
  color: #1890ff;
}

.offline-tag {
  background-color: #f5f5f5;
  color: #bfbfbf;
}

.popup-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.popup-desc {
  font-size: 14px;
  color: #909399;
}

.battle-invite-content {
  padding: 20px;
}

.invited-friend {
  font-weight: bold;
  color: #409EFF;
}
</style>
