<template>
<div class="card-container" @click="navigateToMovieDetail">
  <div class="movie-image">
    <el-image class="image" :src="movie.poster" fit="cover"></el-image>
  </div>
<!--  <div class="movie-rating ribbon">{{ movie.rating }}</div>-->
  <div class="movie-info">
    <div class="movie-title">{{ movie.name }}</div>
<!--    <div class="movie-release-date">{{ movie.issue }}</div>-->
  </div>
</div>
</template>

<script>
import axios from "axios";

export default {
  name: "MovieCard",
  props: {
    movie: {
      type: Object,
      required: true
    }
  },
  data(){
    return {
      // movie:{
      //   poster:"",
      //   title: "",
      //   releaseDate: ""
      // }
    };
  },
  methods: {
    async getMoviePicture() {
      // const posterPath = `/images/${this.movie.mid}/poster.jpg`;
      // const picturePath = `/images/${this.movie.mid}/picture.jpg`;
      const posterPath = '/poster.jpg';
      const picturePath = '/picture.jpg';

      const loadPoster = () => import(/* webpackChunkName: "posters" */ `@/assets${posterPath}`);
      const loadPicture = () => import(/* webpackChunkName: "pictures" */ `@/assets${picturePath}`);

      try {
        const posterModule = await loadPoster();
        const pictureModule = await loadPicture();
        this.movie.poster = posterModule.default;
        this.movie.picture = pictureModule.default;
      } catch (error) {
        console.error('Error loading images:', error);
      }
    },
    navigateToMovieDetail() {
      this.$router.push({
        name: 'moviedetail', // 使用路由名称
        query: {
          movie: JSON.stringify(this.movie) // 将对象转换为 JSON 字符串
        }
      });


    }
  },
  created() {
    this.getMoviePicture()
  },
}
</script>

<style scoped>

.card-container{
  width: 158.28px;
  padding: 0 8.8px;
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}


.movie-image {
  height: 221.2px;
}

.image {
  width: 100%;
  height: 100%;
}

.movie-rating {
}

.ribbon {
}


.movie-info {
  background-color: #1b1b1b;
  width: 100%;
  height: 75px;
  padding: 12px;
  box-sizing: border-box;
}

.movie-title {
  color: white;
  font-size: 16px;
}

.movie-release-date {
  color: darkgrey;
  font-size: 14px;
}
</style>
