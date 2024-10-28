package com.bigdata.server.rest;

import com.bigdata.server.model.core.Movie;
import com.bigdata.server.model.core.Rating;
import com.bigdata.server.model.core.Tag;
import com.bigdata.server.model.core.User;
import com.bigdata.server.model.recom.Recommendation;
import com.bigdata.server.model.request.*;
import com.bigdata.server.service.*;
import com.bigdata.java.model.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/rest/movies")

public class MovieRestApi {

    @Autowired
    private RecommenderService recommenderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private TagService tagService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    KafkaTemplate<String, String> kafka;

    private Logger logger = LoggerFactory.getLogger(MovieRestApi.class);

    /**
     * 提供实时推荐信息的接口（混合推荐）
     * /rest/movies/stream?username=123&num=100
     * {success:true, movies:[]}
     * @param username
     * @param model
     * @return
     */
    @RequestMapping(path = "/stream", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getRealtimeRecommendations(@RequestParam("username") String username,@RequestParam("num") int num, Model model){
        User user = userService.findUserByUsername(username);
        List<Recommendation> recommendations = recommenderService.getStreamRecsMovies(new GetStreamRecsRequest(user.getUid(),num));
        if (recommendations.size() == 0){
            Random random = new Random();
            recommendations = recommenderService.getGenresTopMovies(new GetGenresTopMoviesRequest(user.getGenres().get(random.nextInt(user.getGenres().size())), num));
        }
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success", true);
        return model;
    }

    @RequestMapping(path = "/offline", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getOfflineRecommendations(@RequestParam("username") String username,@RequestParam("num") int num, Model model){
        User user = userService.findUserByUsername(username);
        List<Recommendation> recommendations = recommenderService.getUserCFMovies(new GetUserCFRequest(user.getUid(),num));
        if (recommendations.size() == 0){
            Random random = new Random();
            recommendations = recommenderService.getGenresTopMovies(new GetGenresTopMoviesRequest(user.getGenres().get(random.nextInt(user.getGenres().size())), num));
        }
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success", true);
        return model;
    }

    @RequestMapping(path = "/hot", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getHotRecommendations(@RequestParam("num") int num, Model model){
        List<Recommendation> recommendations = recommenderService.getHotRecommendations(new GetHotRecommendationRequest(num));
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success",true);
        return model;
    }

    @RequestMapping(path = "/rate", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getRateMoreRecommendations(@RequestParam("num") int num, Model model){
        List<Recommendation> recommendations = recommenderService.getRateMoreMovies(new GetRateMoreMoviesRequest(num));
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success",true);
        return model;
    }

    @RequestMapping(path = "/new", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getNewRecommendations(@RequestParam("num") int num,Model model){
        List<Recommendation> recommendations = recommenderService.getNewMovies(new GetNewMoviesRequest(num));
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success",true);
        return model;
    }


    @RequestMapping(path = "/query", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getFuzzySearchMovies(@RequestParam("query") String query,@RequestParam("num") int num,@RequestParam("skip") int skip,Model model){
        List<Recommendation> recommendations = recommenderService.getFuzzySearchMovies(new GetFuzzySearchMoviesRequest(query,num,skip));
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success",true);
        return model;
    }

    @RequestMapping(path = "/info/{mid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getMovieInfo(@PathVariable("mid") int mid, Model model){
        model.addAttribute("success",true);
        model.addAttribute("movie",movieService.findMovieInfo(mid));
        return model;
    }

    @RequestMapping(path = "/addtag/{mid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public void addTagToMovie(@PathVariable("mid") int mid,@RequestParam("username") String username,@RequestParam("tagname") String tagname, Model model){
        User user = userService.findUserByUsername(username);
        Tag tag = new Tag(user.getUid(),mid,tagname,System.currentTimeMillis()/1000);
        tagService.addTagToMovie(tag);
    }

    @RequestMapping(path = "/tags/{mid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getMovieTags(@PathVariable("mid") int mid,Model model){
        model.addAttribute("success",true);
        model.addAttribute("movie",tagService.getMovieTags(mid));
        return model;
    }

    @RequestMapping(path = "/same/{mid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSimMoviesRecommendation(@PathVariable("mid") int mid,@RequestParam("num") int num, Model model){
        List<Recommendation> recommendations = recommenderService.getHybridRecommendations(new GetHybridRecommendationRequest(0.5,mid,num));
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success",true);
        return model;
    }

    @RequestMapping(path = "/rate/{mid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public void rateMovie(@RequestParam("username") String usernamae, @PathVariable("mid") int mid, @RequestParam("score") Double score, Model model){
        logger.info("Rate Movie - Username: " + usernamae + ", MID: " + mid + ", Score: " + score);
        User user = userService.findUserByUsername(usernamae);
        if (user == null) {
            logger.error("No user found for username: " + usernamae);
            return;
        } else {
            logger.info("Found user with UID: " + user.getUid());
        }

        Rating rating = new Rating(user.getUid(),mid,score,System.currentTimeMillis()/1000);
        logger.info("Rating Object - UID: " + rating.getUid() + ", MID: " + rating.getMid() +
                ", Score: " + rating.getScore() + ", Timestamp: " + rating.getTimestamp());

        ratingService.rateToMovie(rating);
        String msg = rating.getUid()+"|"+rating.getMid()+"|"+rating.getScore()+"|"+rating.getTimestamp();
        kafka.send("recommender", msg);
        logger.info(msg);
    }

    @RequestMapping(path = "/genres", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getGenresMovies(@RequestParam("genres") String genres,@RequestParam("num") int num,@RequestParam("skip") int skip, Model model){
        List<Recommendation> recommendations = recommenderService.getGenresMovies(new GetGenresMoviesRequest(genres, num, skip));
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recom:recommendations) {
            ids.add(recom.getMid());
        }
        List<Movie> result = movieService.getMoviesByMids(ids);
        model.addAttribute("movies",result);
        model.addAttribute("success",true);
        return model;
    }

    @RequestMapping(path = "/userrating", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getUserRatings(@RequestParam("username") String username,@RequestParam("num") int num, Model model){
        User user = userService.findUserByUsername(username);
        model.addAttribute("success",true);
        model.addAttribute("rating",ratingService.getUserRatings(new GetUserRatingsRequest(user.getUid(),num)));
        return model;
    }

    public Model getUserChart(String username,Model model){
        return null;
    }
}
