import { createStore } from 'vuex'
import gamer from './gamer'
export default createStore({
  state: {
    username: '',
    email: '',
    level: '',
    rank: '',
    photo: '',
    accessToken: '',
    refreshToken: '',
  },
  getters: {
  },
  mutations: {
    setToken(state, token) {
      state.accessToken = token.accessToken
      state.refreshToken = token.refreshToken
      localStorage.setItem("access_token",token.accessToken)
      localStorage.setItem("refresh_token",token.refreshToken)
    },
    setAccessToken(state, accessToken) {
      state.accessToken = accessToken
      localStorage.setItem("access_token",accessToken)
    },
    setUserInfo(state, userInfo) {
      state.username = userInfo.username
      state.email = userInfo.email
      state.level = userInfo.level
      state.rank = userInfo.rank
      state.photo = userInfo.photo
    },
    setPhoto(state, photo) {
      state.photo = photo
    },
    logout(state){
      state.username = ''
      state.email = ''
      state.level = ''
      state.rank = ''
      state.photo = ''
      state.accessToken = ''
      state.refreshToken = ''
      localStorage.removeItem("access_token")
      localStorage.removeItem("refresh_token")
    }
  },
  actions: {
  },
  modules: {
     gamer
  }
})
