<template>
  <div class="friend-module">
    <el-tabs v-model="activeTab" class="friend-tabs">
      <el-tab-pane label="好友列表" name="friends">
        <div class="friend-search">
          <el-input
            v-model="newFriendName"
            placeholder="请输入好友用户名"
            class="input-with-button"
          >
            <template #append>
              <el-button @click="addFriend">添加</el-button>
            </template>
          </el-input>
        </div>
        
        <div class="friend-list" v-loading="friendsLoading">
          <el-empty v-if="friends.length === 0" description="暂无好友" />
            <ul v-else>
            <li v-for="friend in friends" :key="friend.friendUuid" class="friend-item">
            <div class="friend-info">
              <div class="friend-header">
                <el-avatar 
                  :size="40" 
                  :src="friend.photo || ''" 
                  class="friend-avatar"
                >{{ friend.friendUsername?.charAt(0) }}</el-avatar>
              <div class="friend-name-info">
                <div class="friend-name">{{ friend.friendUsername || '未知用户' }}</div>
                <div class="friend-email">{{ friend.email || '' }}</div>
              </div>
            </div>
            <div class="friend-stats">
            <el-tag size="small" type="success">等级: {{ friend.level || 0 }}</el-tag>
            <el-tag size="small" type="info" class="score-tag">分数: {{ friend.score || 0 }}</el-tag>
            </div>
        </div>
      <el-button 
        type="danger" 
        size="small" 
        @click="deleteFriend(friend.friendUuid)"
      >删除</el-button>
    </li>
  </ul>
</div>
      </el-tab-pane>
      
      <el-tab-pane label="好友请求" name="requests" :badge="requestCount > 0 ? requestCount.toString() : ''">
        <div class="friend-requests" v-loading="requestsLoading">
  <el-empty v-if="friendRequests.length === 0" description="暂无好友请求" />
  <ul v-else>
    <li v-for="request in friendRequests" :key="request.id" class="request-item">
      <div class="request-info">
        <div class="request-header">
          <el-avatar 
            :size="40" 
            :src="request.photo || ''" 
            class="request-avatar"
          >{{ request.friendUsername?.charAt(0) }}</el-avatar>
          <div class="request-name-info">
            <div class="request-name">{{ request.friendUsername || '未知用户' }}</div>
            <div class="request-email">{{ request.email || '' }}</div>
            <div class="request-time">{{ formatTime(request.createTime) }}</div>
          </div>
        </div>
        <div class="request-stats">
          <el-tag size="small" type="success">等级: {{ request.level || 0 }}</el-tag>
          <el-tag size="small" type="info">分数: {{ request.score || 0 }}</el-tag>
        </div>
      </div>
      <div class="request-actions">
        <el-button 
          type="success" 
          size="small" 
          @click="acceptFriendRequest(request.id)"
        >接受</el-button>
        <el-button 
          type="danger" 
          size="small" 
          @click="rejectFriendRequest(request.id)"
        >拒绝</el-button>
      </div>
    </li>
  </ul>
</div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 同名用户选择对话框 -->
    <el-dialog
      v-model="userSelectionVisible"
      title="选择用户"
      width="30%"
    >
      <div class="user-selection-list">
        <div
          v-for="user in sameNameUsers"
          :key="user.uuid"
          class="user-item"
          @click="selectUser(user)"
        >
          <div class="user-info">
            <div class="username">{{ user.username }}</div>
            <div class="email">{{ user.email }}</div>
          </div>
          <div class="user-level">等级: {{ user.level }}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userSelectionVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { get } from '@/ts/request'
import { post } from '@/ts/request'

export default defineComponent({
  name: 'FriendModule',
  setup() {
    const store = useStore()
    const activeTab = ref('friends')
    const friends = ref([])
    const friendRequests = ref([])
    const newFriendName = ref('')
    const friendsLoading = ref(false)
    const requestsLoading = ref(false)
    const userSelectionVisible = ref(false)
    const sameNameUsers = ref([])
    
    // 计算好友请求数量，用于显示徽标
    const requestCount = computed(() => friendRequests.value.length)
    
    // 获取好友列表
    const getFriendList = async () => {
      friendsLoading.value = true
      try {
        const response = await get('/friend/listWithDetails', store.state.accessToken)
    
        const result = await response
        if (result.code === 1101) {
          friends.value = result.data || []
        } else {
          console.error('获取好友列表失败:', result.msg)
        }
      } catch (error) {
        console.error('获取好友列表出错:', error)
        ElMessage.error('获取好友列表失败，请检查网络连接')
      } finally {
        friendsLoading.value = false
      }
    }
    
    // 获取好友请求列表
    const getFriendRequests = async () => {
      requestsLoading.value = true
      try {
        const response = await get('/friend/requestsWithDetails', store.state.accessToken)
    
        const result = await response
        if (result.code === 1101) {
        friendRequests.value = result.data || []
        } else {
        console.error('获取好友请求列表失败:', result.msg)
        }
      } catch (error) {
        console.error('获取好友请求出错:', error)
        ElMessage.error('获取好友请求失败，请检查网络连接')
      } finally {
        requestsLoading.value = false
      }
    }
    // 添加好友
    const addFriend = async () => {
      if (!newFriendName.value) {
        ElMessage.warning('请输入好友用户名')
        return
      }
      
      try {
        const response = await get('/friend/add', store.state.accessToken, newFriendName.value)
        
        const result = await response
        if (result.code === 1081) {
          ElMessage.success('好友请求已发送，等待对方接受')
          newFriendName.value = ''
        } else if (result.code === 3101 && result.data) {
          // 存在多个同名用户，显示选择对话框
          sameNameUsers.value = result.data
          userSelectionVisible.value = true
        } else {
          ElMessage.error(result.msg || '添加好友失败')
        }
      } catch (error) {
        console.error('添加好友出错:', error)
        ElMessage.error('添加好友失败，请检查网络连接')
      }
    }
    
    // 选择特定用户（处理同名情况）
    const selectUser = async (user) => {
      try {
        const response = await post('/friend/addByUuid', store.state.accessToken, user.uuid)
        
        const result = await response
        if (result.code === 1081) {
          ElMessage.success('好友请求已发送，等待对方接受')
          userSelectionVisible.value = false
          newFriendName.value = ''
        } else {
          ElMessage.error(result.msg || '添加好友失败')
        }
      } catch (error) {
        console.error('添加指定好友出错:', error)
        ElMessage.error('添加好友失败，请检查网络连接')
      }
    }
    
    // 删除好友
    const deleteFriend = async (FriendUuid) => {
      try {
        const response = await get('/friend/delete', store.state.accessToken, FriendUuid)
        
        const result = await response
        if (result.code === 1091) {
          ElMessage.success('删除好友成功')
          await getFriendList() // 刷新好友列表
        } else {
          ElMessage.error(result.msg || '删除好友失败')
        }
      } catch (error) {
        console.error('删除好友出错:', error)
        ElMessage.error('删除好友失败，请检查网络连接')
      }
    }
    
    // 接受好友请求
    const acceptFriendRequest = async (requestId) => {
      try {
        const response = await get('/friend/accept', store.state.accessToken, requestId)
        
        const result = await response
        if (result.code === 1081) {
          ElMessage.success('已接受好友请求')
          await getFriendRequests() // 刷新请求列表
          await getFriendList() // 刷新好友列表
        } else {
          ElMessage.error(result.msg || '接受好友请求失败')
        }
      } catch (error) {
        console.error('接受好友请求出错:', error)
        ElMessage.error('操作失败，请检查网络连接')
      }
    }
    
    // 拒绝好友请求
    const rejectFriendRequest = async (requestId) => {
      try {
        const response = await get('/friend/reject', store.state.accessToken, requestId)
        
        const result = await response
        if (result.code === 1091) {
          ElMessage.success('已拒绝好友请求')
          await getFriendRequests() // 刷新请求列表
        } else {
          ElMessage.error(result.msg || '拒绝好友请求失败')
        }
      } catch (error) {
        console.error('拒绝好友请求出错:', error)
        ElMessage.error('操作失败，请检查网络连接')
      }
    }
    
    // 获取发送者名字
    const getSenderName = (request) => {
      return request.senderUsername || '未知用户'
    }
    
    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return ''
      
      try {
        const date = new Date(timeStr)
        return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
      } catch (e) {
        return timeStr
      }
    }
    
    const padZero = (num) => {
      return num < 10 ? '0' + num : num
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      getFriendList()
      getFriendRequests()
    })
    
    return {
      activeTab,
      friends,
      friendRequests,
      requestCount,
      newFriendName,
      friendsLoading,
      requestsLoading,
      userSelectionVisible,
      sameNameUsers,
      addFriend,
      deleteFriend,
      getFriendList,
      getFriendRequests,
      acceptFriendRequest,
      rejectFriendRequest,
      selectUser,
      getSenderName,
      formatTime
    }
  }
})
</script>

<style scoped>
.friend-module {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 16px;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.friend-tabs {
  margin-bottom: 20px;
}

.friend-search {
  margin-bottom: 16px;
}

.input-with-button {
  width: 100%;
}

.friend-list, .friend-requests {
  min-height: 200px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.friend-item, .request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.friend-item:last-child, .request-item:last-child {
  border-bottom: none;
}

.request-info {
  display: flex;
  flex-direction: column;
}

.request-info .username {
  font-weight: 500;
  margin-bottom: 4px;
}

.request-info .time {
  font-size: 12px;
  color: #999;
}

.user-selection-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.user-item:hover {
  background-color: #f5f7fa;
}

.user-item:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-info .username {
  font-weight: 500;
  margin-bottom: 4px;
}

.user-info .email {
  font-size: 12px;
  color: #999;
}

.user-level {
  font-size: 14px;
  color: #409eff;
}
</style>