<template>
  <div class="banner" :style="{ background: background }">
    <div class="name" @click="navigateToHome()">
      MovieFans
    </div>
    <div class="options">
      <div class="option" @click="navigateToHome()">Home</div>
      <div class="option" @click="navigateToGenreMovie('Action')">Action</div>
      <div class="option" @click="navigateToGenreMovie('Romance')">Romance</div>
      <div class="option" @click="navigateToGenreMovie('Comedy')">Comedy</div>
      <div class="option" @click="navigateToGenreMovie('Fantacy')">Fantacy</div>
      <div class="option" @click="navigateToGenreMovie('Horror')">Horror</div>
      <div class="option" @click="navigateToGenreMovie('War')">War</div>
      <el-popover
          placement="bottom"
          :width="100"
          trigger="click"
      >
        <div>
          <div @click="navigateToGenreMovie('Western')">Western</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Animation')">Animation</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Crime')">Crime</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Drama')">Drama</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Mystery')">Mystery</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Adventure')">Adventure</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Thriller')">Thriller</div>
          <el-divider  class="divider"/>
          <div @click="navigateToGenreMovie('Documentary')">Documentary</div>
        </div>
        <template #reference>
          <div class="option">More</div>
        </template>
      </el-popover>
    </div>
    <div class="right">
      <el-icon color="white" style="margin-right: 1vw" @click="clickSearch" class="search">
        <!-- 使用 v-if 来决定是否渲染 Search 组件 -->
        <Search/>
        <div v-if="isquery">
          <form @submit.prevent="onSearch">
            <el-input v-model="query" @blur="onBlur" style="width: 20vw; position: relative; right: 9vw" :autofocus="true" />
          </form>
        </div>
      </el-icon>
      <el-badge :value="6" style="display: flex; align-items: center; width: 1vw; margin-right: 1vw">
        <el-popover
            placement="bottom"
            title="Notificaitons"
            :width="200"
            trigger="click"
        >
          <div>
            <div class="notification">New Movie Recommendations: "Hi there! We've updated your personalized movie recommendations. Check out the latest picks tailored just for you!"</div>
            <el-divider  class="divider"/>
            <div class="notification">Friend Activity Alert: "Your friend Alex has just rated 'The Grand Budapest Hotel' 5 stars. See what they thought about the movie!"</div>
            <el-divider  class="divider"/>
            <div class="notification">Weekly Top Picks: "This week's top movies are here! Dive into our curated list of must-watch films, trending now."</div>
            <el-divider  class="divider"/>
            <div class="notification">Reminder for Watchlist: "Don't forget to watch! 'Inception' and 3 other movies on your watchlist are leaving our platform soon. Catch them before they're gone!"</div>
            <el-divider  class="divider"/>
            <div class="notification">Special Offer: "Exclusive deal just for you! Upgrade to premium and enjoy an ad-free experience with access to exclusive content."</div>
            <el-divider  class="divider"/>
            <div class="notification">User Review Milestone: "Congratulations! Your review for 'Parasite' is now one of the top-rated reviews on our site. Keep sharing your insights!"</div>
          </div>
          <template #reference>
            <el-icon color="white" ><Bell /></el-icon>
          </template>
        </el-popover>
      </el-badge>
      <el-popover
          placement="bottom"
          :width="100"
          trigger="click"
      >
        <div style="display: flex; justify-content: center; font-size: 12px;">
          {{user.username}}</div>
        <template #reference>
          <div v-if="null != user" style="color:white; display: flex; align-items: center; width: 1vw">
            <el-icon color="white"><User /></el-icon>
          </div>
        </template>
      </el-popover>

    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Banner",
  props: {
    background: {
      type: String,
      default: 'linear-gradient(to bottom, rgba(0,0,0,0.8), transparent)'
    }
  },
  data(){
    return{
      isquery: false,
      query: ""
    };
  },
  methods: {
    onBlur(){
      this.isquery = false
    },
    onSearch(){
      console.log(this.query)
      this.$router.push({
        name: 'searchresult',
        query: {
          query: this.query
        }
      });
    },
    clickSearch() {
      this.isquery = true
    },
    navigateToHome() {
      this.$router.push('/home');
    },
    navigateToGenreMovie(genre) {
      this.$router.push({
        name: 'genremovie',
        query: {
          genre: genre
        }
      });
    }
  },
  computed: {
    user() {
      return this.$store.state.user;
    }

  }
}


</script>

<style scoped>
.banner{
  /*background-color: black;*/
  /*background: linear-gradient(to bottom, rgba(0,0,0,0.8), transparent);*/
  width: 100%;
  height: 80px;
  padding: 15px 30px;
  box-sizing: border-box;
  display: flex;
  align-items: center; /* 垂直居中 */
}

.name{
  color: RGB(218,0,1);
  font-size: 30px;
  font-weight: bold;
}

.options{
  margin-left: 50px;
  display: flex;
  flex-direction: row;
}

.option{
  color: white;
  margin-right: 20px;
}

.right{
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: right;
}
.search{
  display: flex;
  align-items: center;
}

::v-deep .el-input__wrapper{
  box-shadow: none !important;
  border: 1px solid rgb(218,0,1) !important;
  background-color: black !important;
}
::v-deep .el-input__wrapper:focus-within {
  box-shadow: none !important;
  border: 1px solid rgb(218,0,1) !important;
}

::v-deep .el-input__inner{
  color: white !important;
}


::v-deep .el-badge .el-badge__content {
  background-color: rgb(218,0,1) !important;
  border: 1px solid rgb(218,0,1) !important;
  font-size: 10px  !important;
  height: 12px !important;
  padding: 1px 4.5px !important;
}

.notification{
  padding: 1vh 0;
  font-size: 12px;
  max-height: 4.2vh;
  overflow: hidden;
}

.divider{
  margin: 0;
}

</style>
