package com.example.qraphql.controller;

import com.example.qraphql.dto.PostRequestDto;
import com.example.qraphql.model.Post;
import com.example.qraphql.service.PostService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public Flux<Post> getPosts(){
        return postService.getPosts();
    }

    @QueryMapping
    public Mono<Post> getPost(@Argument int id){
        return postService.getPost(id);
    }

    @MutationMapping
    public Mono<Post> createPost(@Argument(name = "createPostInput") @Valid PostRequestDto postRequestDto){
        return postService.createPost(Mono.just(postRequestDto));
    }

    @MutationMapping
    public Mono<Post> updatePost(@Argument int id, @Argument(name = "updatePostInput") @Valid PostRequestDto postRequestDto){
        return postService.updatePost(id, postRequestDto);
    }

    @MutationMapping
    public Mono<Post> deletePost(@Argument int id){
        return postService.deletePost(id);
    }
}
