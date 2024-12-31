<template>
  <div>
    <Banner></Banner>
    <div class="video">
    <video width="100%" height="auto" autoplay muted loop>
      <source src="@/assets/video.mp4" type="video/mp4">
      Your browser does not support the video tag.
    </video>
      <div style="width: 30vw; display: flex; flex-direction:column; position: absolute; top: 70vh; left: 5vw;">
        <div class="headmoviename" style="font-size: 72px; color: white">
          Die Hart
        </div>
        <div style="color: white; font-size: 24px;">
          Watch Now
        </div>
        <div style="color: white; font-size: 12px; margin-top: 3vh;">
          Kevin Hart - playing a version of himself - is on a death-defying quest to become an action star. And with a little help from John Travolta, Nathalie Emmanuel, and Josh Hartnett - he just might pull it off.
        </div>
        <div style="display: flex; width: 100%; margin-top: 3vh;">
          <button style="display:flex; align-items: center; justify-content: center; width: 8vw; height: 5vh; background: white; border: 0; border-radius: 5px; padding: 5px; color: black; margin-right: 2vw; font-weight: bold; font-size: 16px"><el-icon color="black" size="24px" style="margin-right: 0.3vw"><VideoPlay /></el-icon>Play</button>
          <button style="display:flex; align-items: center; justify-content: center; width: 8vw; height: 5vh; background: gray; border: 0; border-radius: 5px; padding: 5px; color: white; font-weight: bold; font-size: 16px"><el-icon color="white" size="24px" style="margin-right: 0.3vw"><InfoFilled /></el-icon>More Info</button>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="movie-container" v-for="region in regions" :key="region.id">
        <div class="subtitle" v-show="regionMovies[region].length > 0">
          <div class="subtitle-name">{{region}}</div>
        </div>
        <div class="movie-block">
        <movie-card  v-for="movie in regionMovies[region]" :key="movie.id" :movie="movie" />
        </div>
      </div>
    </div>

    <div
        class="check-box-container"
        v-show="user != null && user.first"
    >
      <el-checkbox-group
          v-model="checkedGenres"
      >
        <div style="color: white; font-size: 12px">
          Choose the film category that interests you
        </div>
        <el-checkbox class="check-box" v-for="genre in genres" :key="genre" :label="genre">{{
            genre
          }}</el-checkbox>
      </el-checkbox-group>
      <el-button @click="confirmGenres">Confirm</el-button>
    </div>
  </div>
</template>

<script>
import MovieCard from "@/components/MovieCard";
import login from "@/pages/login/login";
import axios from "axios";
import {MOVIE_PATH, USER_PATH} from "@/assets/Constant";
import Banner from "@/components/Banner";
export default {
  name: "home",
  components: {
    Banner,
    MovieCard
  },
  data() {
    return {
      regions: [
          "Latest Releases",// New
          "Trending Now",// Hot
          "Top Rated Hits",// Rate more
          "Cinematic Echoes",// Offline
          "Fresh Picks for You",// Online
      ],
      genres: [
        'Action',
        'Adventure',
        'Animation',
        'Comedy',
        'Crime',
        'Documentary',
        'Drama',
        'Family',
        'Fantasy',
        'Foreign',
        'History',
        'Horror',
        'Music',
        'Mystery',
        'Romance',
        'Science fiction',
        'Tv movie',
        'Thriller',
        'War',
        'Western'
      ],
      checkedGenres: [],
      newMovies: [],
      hotMovies: [],
      rateMoreMovies: [],
      offlineMovies: [],
      onlineMovies: [],
      user: null
    };
  },
  computed: {
    regionMovies() {
      return {
        "Latest Releases": this.newMovies,
        "Trending Now": this.hotMovies,
        "Top Rated Hits": this.rateMoreMovies,
        "Cinematic Echoes": this.offlineMovies,
        "Fresh Picks for You": this.onlineMovies,
      };
    }
  },
  methods: {
    confirmGenres(){
      this.user.first = false
      let genresString  = this.checkedGenres.join('|')
      axios.get(USER_PATH + '/genres', {
        params:{
          username: this.user.username,
          genres: genresString
        }
      }
    )
    },
    getNewMovie(){
      axios.get(MOVIE_PATH+'/new',{
        params:{
          num:16
        }
      })
          .then(res =>{
            if(res.data['success'] == true){
              this.newMovies = res.data['movies']
            }

          })
          .catch(error => {
            console.log(error)
          })
    },
    getHotMovie(){
      axios.get(MOVIE_PATH+'/hot',{
        params:{
          num:16
        }
      })
          .then(res =>{
            if(res.data['success'] == true){
              this.hotMovies = res.data['movies'];
            }

          })
          .catch(error => {
            console.log(error)
          })
    },
    getRateMoreMovie(){
      axios.get(MOVIE_PATH+'/rate',{
        params:{
          num:16
        }
      })
          .then(res =>{
            if(res.data['success'] == true){
              this.rateMoreMovies = res.data['movies'];
            }

          })
          .catch(error => {
            console.log(error)
          })
    },
    getOfflineMovie(){
      if (null == this.user)
        this.goBackForLogin()
      else
        axios.get(MOVIE_PATH+'/offline',{
          params:{
            username: this.user.username,
            num:16
          }
        })
            .then(res =>{
              if(res.data['success'] == true){
                this.offlineMovies = res.data['movies'];
              }

            })
            .catch(error => {
              console.log(error)
            })
    },
    getOnlineMovie(){
      if (null == this.user)
        this.goBackForLogin()
      else
        axios.get(MOVIE_PATH+'/stream',{
          params:{
            username: this.user.username,
            num:16
          }
        })
            .then(res =>{
              if(res.data['success'] == true){
                this.onlineMovies = res.data['movies'];
              }

            })
            .catch(error => {
              console.log(error)
            })
    },
    goBackForLogin(){
      this.$message({
        message: 'Session expiration',
        type: 'error',
      });
      console.log("user not fount")
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
  },
  created(){
    this.checkUser()
    this.getNewMovie()
    this.getHotMovie()
    this.getRateMoreMovie()
    this.getOfflineMovie()
    this.getOnlineMovie()
  },
}
</script>

<style scoped>

.video {
  top: -190px;
  position: relative;
  z-index: -1;
}


.content{
  background-color: black;
  min-height: 100vh;
  padding: 0 50px;
  top: 710px;
  position: absolute;
}

.movie-container{
  margin-top: 40px;
}

.movie-block{
  padding: 10px 0;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

.subtitle{
  color: white;

}
.subtitle-name{
  font-size: 20px;
}

.check-box-container{
  position: fixed;
  top:20vh;
  right: 30vw;
  background-color: rgba(0,0,0,0.8);
  border-radius: 5px;
  width: 40vw;
  padding: 10px 5vw;
  box-sizing: border-box;
}

.check-box{
  margin-top: 2vh;
}
</style>
