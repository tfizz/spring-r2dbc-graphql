package com.example.qraphql.service;

import com.example.qraphql.dto.PostRequestDto;
import com.example.qraphql.exception.PostNotFoundException;
import com.example.qraphql.model.Post;
import com.example.qraphql.repository.PostRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Flux<Post> getPosts(){
        return postRepository.findAll();
    }

    public Mono<Post> getPost(int id){
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException("Post Not Found. id: " + id)));
    }

    public Mono<Post> createPost(Mono<PostRequestDto> postRequestDtoMono){
        return postRequestDtoMono
                .map(postRequestDto -> Post.builder().title(postRequestDto.getTitle()).body(postRequestDto.getBody()).build())
                .flatMap(post -> postRepository.save(post));
    }

    public Mono<Post> deletePost(int id) {
        return getPost(id)
                .flatMap(post -> postRepository.deleteById(id).thenReturn(post) );
    }

    public Mono<Post> updatePost(int id, PostRequestDto postRequestDto) {
        return getPost(id)
                .map(post -> {
                    post.setTitle(postRequestDto.getTitle());
                    post.setBody(postRequestDto.getBody());
                    return post;
                })
                .flatMap(post -> postRepository.save(post));
    }
}
