<template>
  <div class="container">
    <banner background="black"/>
    <div class="content">
      <div class="subtitle">
        <div class="subtitle-name">Search Result:{{query}}</div>
      </div>
      <div class="movie-block">
        <movie-card  v-for="movie in searchMovies" :key="movie.id" :movie="movie" />
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import {MOVIE_PATH} from "@/assets/Constant";
import MovieCard from "@/components/MovieCard";
import Banner from "@/components/Banner";

export default {
  name: "searchresult",
  components: {Banner,MovieCard},
  data(){
    return{
      query: "",
      searchMovies: []
    };
  },
  methods:{
    init(route){
      console.log(route)
      if (route.query.query) {
        try {
          this.query = route.query.query;
          this.searchMovies = []
          this.getSearchMovie()
        } catch (e) {
          console.error("Error parsing JSON:", e);
          // 处理错误或提供回退
        }
      }
    },
    getSearchMovie(){
      axios.get(MOVIE_PATH+'/query',{
        params:{
          query: this.query,
          num: 32,
          skip: this.searchMovies.length
        }
      })
          .then(res =>{
            if(res.data['success'] == true){
              if (this.searchMovies.length>0)
                this.searchMovies = [...this.searchMovies,...res.data['movies']];
              else
                this.searchMovies = res.data['movies']
            }

          })
          .catch(error => {
            console.log(error)
          })
    },
    throttledHandleScroll() {
      if (this.inThrottle) return;

      this.handleScroll();
      this.inThrottle = true;

      setTimeout(() => {
        this.inThrottle = false;
      }, 100);
    },
    handleScroll() {
      const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 100;

      if (nearBottom) {
        this.getSearchMovie();
      }
    },
  },
  created() {
    this.init(this.$route)
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
.content{
  background-color: black;
  min-height: 100vh;
  padding: 0 50px;
}


.movie-block{
  padding: 10px 0;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  width: 100%;
}

.subtitle{
  color: white;
  padding-top: 50px;
}
.subtitle-name{
  font-size: 20px;
}

</style>
