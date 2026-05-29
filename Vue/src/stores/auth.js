import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const isLoggedIn = ref(false)
  const currentUser = ref(null)
  const searchKeyword = ref('')
  const conversations = ref([])
  const openChatTarget = ref('')

  function restoreLogin() {
    const saved = localStorage.getItem('currentUser')
    if (saved) {
      try {
        currentUser.value = JSON.parse(saved)
        isLoggedIn.value = true
      } catch (e) {
        localStorage.removeItem('currentUser')
      }
    }
  }

  function login(user) {
    currentUser.value = user
    isLoggedIn.value = true
    localStorage.setItem('currentUser', JSON.stringify(user))
  }

  function logout() {
    currentUser.value = null
    isLoggedIn.value = false
    localStorage.removeItem('currentUser')
  }

  return {
    isLoggedIn, currentUser, searchKeyword,
    conversations, openChatTarget,
    restoreLogin, login, logout
  }
})
