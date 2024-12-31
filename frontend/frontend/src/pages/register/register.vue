<template>
  <div class="main-container">
    <div class="background"></div>
    <div class="overlay"></div>
    <div class="container">
      <div class="title">Sign Up</div>
      <div class="main">
        <el-input class="input" v-model="username" placeholder="Email or phone number" autosize size="large"/>
        <el-input class="input" v-model="password" placeholder="Password" type="password" autosize size="large"/>
      </div>
      <div class="footer">
        <el-button  class="registerbtn" @click="register" >Sign Up</el-button>
        <div class="login" @click="switchToLogin">Sign In</div></div>
    </div>

  </div>
</template>

<script>
import axios from 'axios';
import {USER_PATH} from '@/assets/Constant'
export default {
  name: "register",
  data() {
    return {
      username: "",
      password: "",
      user: null
    };
  },
  methods: {
    switchToLogin() {
      this.$emit('change-component', 'login');
    },
    register() {
      axios.get(USER_PATH+'/register', {
        params: {
          username: this.username,
          password: this.password
        }
      })
          .then(response => {
            if (response.data.success){
              this.$message({
                message: 'Register Success',
                type: 'success',
              });
              this.user = response.data.user;
              this.$store.commit('setUser', this.user);
              this.switchToLogin()
            }
            else {
              this.$message({
                message: 'Username has already existed',
                type: 'error',
              });
            }
          })
          .catch(error => {
            console.log(error);
            this.$message({
              message: 'Register Fail',
              type: 'error',
            });
          });

    },
  },
}
</script>

<style scoped>

.main-container{
  display: flex;
  justify-content: center;
  height: 100vh; /* 设置容器的高度为视口的高度 */
  position: relative; /* 为伪元素定位提供参照 */
}

.background {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-image: url('../../assets/background.jpg');
  background-size: cover; /* 背景图片覆盖整个容器 */
  background-size: 128.2%; /* 将背景图片放大到120% */
  /*background-position: center; !* 确保放大后的图片仍然居中 *!*/
  z-index: -2;
}

.overlay {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.5); /* 半透明的黑色 */
  z-index: -1;
}

.container{
  width: 450px;
  height: 600px;
  border-radius: 3px;
  margin-top: 93px;
  padding: 60px 68px 40px;
  display: flex;
  flex-direction: column;
  background-color: black;
  box-sizing: border-box;
}

.title {
  font-size: 30px;
  color: white;
  margin-bottom: 10px;
}
.main{
  display: flex;
  flex-direction: column;
  align-items: center;
}

.input{
  margin-top: 20px;
  /*background-color: gray;*/
}

.footer{

}

.registerbtn {
  background-color: RGB(218,0,1);
  border-color: RGB(218,0,1);
  color: white;
  font-size: 20px;
  padding: 5px 10px;
  height: 50px;
  width: 100%;
  margin-top: 40px;
}

.login{
  margin-top: 20px;
  color: white;
}
</style>
