package com.inn.controller;

import com.inn.data.post.Post;
import com.inn.data.post.PostRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/posts") // All routes in this controller will start with /posts
public class PostController {

    private final PostRepository postRepository;

    @Autowired
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Shows the form for creating a new post.
     * Maps to GET /posts/new
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Create a new Post object
        Post postWithTemplate = new Post();

        // Define your template content as an HTML string
        String templateContent = """
        <h1>Enter Your Post Title Here</h1>
        <p>This is a starting paragraph. You can provide instructions or default text here.</p>
        <ul>
            <li>List item 1</li>
            <li>List item 2</li>
        </ul>
        <p>Start writing... ✍️</p>
    """;

        // Set the template content on the object
        postWithTemplate.setContent(templateContent);

        // Add the pre-filled object to the model
        model.addAttribute("post", postWithTemplate);

        return "post/postForm";
    }

    /**
     * Handles the submission of the new post form.
     * It sanitizes the HTML content using Jsoup before saving.
     * Maps to POST /posts
     */
    @PostMapping
    public String createPost(@ModelAttribute Post post) {
        String unsafeHtml = post.getContent();
        Safelist safelist = Safelist.relaxed();
        safelist.addAttributes(":all", "style");
        String safeHtml = Jsoup.clean(unsafeHtml, safelist);

        post.setContent(safeHtml);
        Post savedPost = postRepository.save(post);

        return "redirect:/posts/" + savedPost.getId();
    }

    /**
     * Displays a single post by its ID.
     * Maps to GET /posts/{id}
     */
    @GetMapping("/{id}")
    public String viewPost(@PathVariable("id") Long id, Model model) {
        // Find the post in the repository or throw an exception if not found
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + id));

        // Add the post object to the model for rendering in the view
        model.addAttribute("post", post);

        return "post/postView"; // Renders templates/post/postView.html
    }
}