<template>
  <div class="container">
    <banner background="black"/>
    <div class="content">
        <div class="subtitle">
          <div class="subtitle-name">{{genre}}</div>
        </div>
        <div class="movie-block">
          <movie-card  v-for="movie in genreMovies" :key="movie.id" :movie="movie" />
        </div>
    </div>
  </div>
</template>

<script>
import MovieCard from "@/components/MovieCard";
import Banner from "@/components/Banner";
import axios from "axios";
import {MOVIE_PATH} from "@/assets/Constant";
export default {
  name: "genremovie",
  components: {Banner,MovieCard},
  data(){
    return{
      genreMovies: [],
      // movieBlock: [
      //     {
      //       mid: 102,
      //       name: "name"
      //     },
      //     {
      //       mid: 102,
      //       name: "name"
      //     },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   },
      //   {
      //     mid: 102,
      //     name: "name"
      //   }
      //     ]
    };
  },
  methods:{
    handleScroll() {
      // 检查用户是否滚动到了页面底部
      const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 100; // 可以根据需要调整这个值

      if (nearBottom) {
        // 用户已到达底部
        this.getMovies(); // 调用加载数据的方法
      }
    },
    getMovies(){
      axios.get(MOVIE_PATH+'/genres',{
        params: {
          genres: this.genre,
          num: 32,
          skip: this.genreMovies.length
        }
      })
          .then(res =>{
            if(res.data['success'] == true){
              if (this.genreMovies.length>0)
                this.genreMovies = [...this.genreMovies,...res.data['movies']];
              else
                this.genreMovies = res.data['movies']
            }

          })
          .catch(error => {
            console.log(error)
          })

      // if (this.genreMovies.length>0)
      //   this.genreMovies = [...this.genreMovies, ...this.movieBlock]
      // else
      //   this.genreMovies = this.movieBlock
    },
    throttledHandleScroll() {
      // 如果已在节流模式中，则不执行函数
      if (this.inThrottle) return;

      // 否则，执行handleScroll并设置节流模式
      this.handleScroll();
      this.inThrottle = true;

      // 在限定时间后重置节流模式
      setTimeout(() => {
        this.inThrottle = false;
      }, 100); // 2000毫秒后重置节流状态
    }
  },
  created() {
    this.genre = this.$route.query.genre
    this.getMovies()
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
      this.genre = newRoute.query.genre;
      this.genreMovies = []
      this.getMovies()
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
