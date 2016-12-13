package com.TKP.firstapp.Controller;

import com.TKP.firstapp.domain.Post;
import com.TKP.firstapp.domain.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by TKP on 13.12.2016.
 */
@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public  String listPost(Model model){
        model.addAttribute("posts", repository.findAll());
        return "posts/list";
    }
    @RequestMapping(value="/{id}/delete", method = RequestMethod.GET)
    public ModelAndView delete(@PathVariable long id){
        repository.delete(id);
        return new ModelAndView("redirect:/posts");
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newPost(){
        return "posts/new";
    }

    @RequestMapping(value="/create", method = RequestMethod.POST)
    public ModelAndView create (@RequestParam("message") String mesage){
        repository.save(new Post(mesage));
        return new ModelAndView("redirect:/posts");
    }


    @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
    public String edit(@PathVariable long id, Model model) {
        Post post = repository.findOne(id);
        model.addAttribute("post", post);
        return "posts/edit";
    }

    @RequestMapping(value = ("/update"), method = RequestMethod.POST)
    public ModelAndView update(@RequestParam("post_id") long id,
                               @RequestParam("message") String message){
        Post post = repository.findOne(id);
        post.setMessage(message);
        repository.save(post);
        return new ModelAndView("redirect:/posts");
    }

}
