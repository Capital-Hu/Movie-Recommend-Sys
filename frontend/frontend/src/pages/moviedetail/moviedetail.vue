<template>
  <div>
    <banner></banner>
    <div  class="container">
      <img class="background-image" :src="movie.picture">
      <div class="background-overlay"></div>
    <div class="top">
      <div class="name">{{movie.name}}</div>
      <div class="poster"><img :src="movie.poster" fit="contain"></div>
      <div class="info">
        <div class="line"><div class="head" style="margin-right: 2.8vw">Directors</div> <div class="tail">{{movie.directors}}</div></div>
        <div class="line"><div class="head" style="margin-right: 5vw">Stars</div> <div class="tail"> <el-scrollbar max-height="20vh">
          {{movie.actors}}
        </el-scrollbar></div></div>
        <div class="line"><div class="head" style="margin-right: 3.95vw">Genres</div> <div class="tail">{{movie.genres}}</div></div>
        <div class="line"><div class="head" style="margin-right: 2.5vw">Language</div> <div class="tail">{{movie.language}}</div></div>
        <div class="line"><div class="head" style="margin-right: 0.9vw">Release Date</div> <div class="tail">{{movie.issue}}</div></div>
        <div class="line"><div class="head" style="margin-right: 3.15vw">Runtime</div> <div class="tail">{{movie.timelong}}</div></div>
      </div>
      <el-divider direction="vertical" style="height: 20vh; " color="black"/>
      <div class="rating">
        <div>MovieFans RATING</div>
        <div class="rating-detail">
          <el-icon color="RGB(240,196,17)" size="32px"><StarFilled /></el-icon>
        <div style="font-size: 24px; color: white">{{ movie.score }}</div>
          <div style="font-size: 21px; color: RGB(190,187,185);">/10</div>
        </div>
        <el-rate v-model="userrating" allow-half @click="rateMovie" />
      </div>
      <div>
        <div class="useroperation">
          <button @click="addToWatchlist">Add to Watchlist</button>
          <button @click="markAsWatched">Mark as Watched</button>
        </div>
        <div v-show="movie.descri.length >0 " style="margin-top: 5vh">
          <el-scrollbar height="18vh">
            <div style="font-size: 18px">Storyline</div>
            <div>{{ movie.descri }}</div>
          </el-scrollbar>
        </div>
      </div>
    </div>
      <div class="recommend">
        <div class="subtitle">
          <div class="subtitle-name">More Like This</div>
        </div>
        <div class="movie-block">
          <movie-card  v-for="movie in simMovies" :key="movie.id" :movie="movie" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Banner from "@/components/Banner";
import axios from "axios";
import {MOVIE_PATH} from "@/assets/Constant";
import MovieCard from "@/components/MovieCard";
export default {
  name: "moviedetail",
  components: {Banner, MovieCard},
  data(){
    return{
      movie: null,
      simMovies: [],
      userrating: 0
    };
  },
  methods: {
    markAsWatched(){
      this.$message({
        message: 'Mark success',
        type: 'success',
      });
    },
    addToWatchlist(){
      this.$message({
        message: 'Add success',
        type: 'success',
      });
    },
    goBackForLogin(){
      this.$message({
        message: 'Session expiration',
        type: 'error',
      });
      this.$router.push('/')
    },
    checkUser(){
      if (null == this.user)
        if (null == this.$store.state.user) {
          this.goBackForLogin()
        }
        else
          this.user = this.$store.state.user
    },
    rateMovie(){
      this.checkUser()
      if (null != this.user)
        axios.get(MOVIE_PATH+'/rate/'+this.movie.mid, {
          params: {
            username: this.user.username,
            score: this.userrating
          }
        })
            .then(response => {
              this.$message({
                message: 'Rating success',
                type: 'success',
              });
            })
            .catch(error => {
              console.error('There was an error!', error);
            });
    },
      init(route){
        // window.scrollTo(0, 0);
        window.scrollTo({ top: 0, behavior: 'smooth' });
        if (route.query.movie) {
          try {
            this.movie = JSON.parse(this.$route.query.movie);
            console.log(this.movie)
          } catch (e) {
            console.error("Error parsing JSON:", e);
            // 处理错误或提供回退
          }
        }
        this.simMovies = []
        this.userrating = 0
        this.getSimMovie()
      },
      getSimMovie(){
        axios.get(MOVIE_PATH+'/same/'+this.movie.mid, {
          params: {
            num: 16
          }
        })
            .then(response => {
              // 处理响应数据
              console.log(response);
              this.simMovies = response.data["movies"]
              console.log(this.simMovies)
            })
            .catch(error => {
              // 处理错误
              console.error('There was an error!', error);
            });
      }
  },
  created() {
    this.init(this.$route)
  },
  mounted() {
    window.addEventListener('scroll', this.throttledHandleScroll);
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.throttledHandleScroll);
  },
  watch: {
    // 监听路由变化
    '$route': function(newRoute) {
      this.init(newRoute)
    }
  }
}
</script>

<style scoped>
.container{
  /*width: 100vw; */
  height: 100vh;
}
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-size: cover;
  background-position: center;
  z-index: -2;
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  backdrop-filter: blur(100px); /* 调整模糊度 */
  background-color: rgba(128, 128, 128, 0.5); /* 磨砂灰的背景色，可以调整颜色和透明度 */
  z-index: -1;
}
.top{
  padding: 0 15vw 0 20vw;
  display: flex;
  flex-wrap: wrap;
}
.name{
  width: 100%;
  font-size: 30px;
  margin: 5vh 0;
}
.poster{
  width: 15vw;
}
.info{
  width: 35vw;
}
.line{
  display: flex;
}

.head{
  font-weight: bold;
}
.rating{
  width: 10vw;
}

.rating-detail{
  display: flex;
}

.useroperation{
  margin-top: 5vh;
}

.recommend{
  position: absolute;
  top: 100vh;
  background-color: black;
  padding: 0 50px;
  width: 100%;
}

.movie-block{
  padding: 10px 0;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

.subtitle{
  color: white;
  padding-top: 50px;
}
.subtitle-name{
  font-size: 20px;
}



</style>
