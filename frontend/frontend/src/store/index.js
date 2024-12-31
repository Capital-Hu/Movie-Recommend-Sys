import { createStore } from 'vuex'
import { userPersist } from './userPersist'; // 引入插件

export default createStore({
  state: {
    user: null
  },
  getters: {
  },
  mutations: {
    setUser(state, user) {
      state.user = user
    }
  },
  actions: {
  },
  modules: {
  },
  plugins: [userPersist] // 使用插件
})
