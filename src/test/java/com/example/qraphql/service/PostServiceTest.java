package com.example.qraphql.service;

import com.example.qraphql.dto.PostRequestDto;
import com.example.qraphql.model.Post;
import com.example.qraphql.repository.PostRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private PostService postService;

    @Test
    void testGetPostsShouldReturnFluxOfPost(){
        Faker faker = new Faker();
        List<Post> postList = IntStream.range(0, 10)
                .mapToObj(i -> Post.builder().id(i + 1).title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build())
                .collect(Collectors.toList());

        when(postRepository.findAll()).thenReturn(Flux.fromIterable(postList));

        Flux<Post> actualPostFlux = postService.getPosts();

        StepVerifier.create(actualPostFlux)
                .expectNextCount(postList.size())
                .verifyComplete();
    }

    @Test
    void testGetPostShouldReturnPostWithValidId(){
        Faker faker = new Faker();
        Post post = Post.builder().id(1).title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build();
        when(postRepository.findById(anyInt())).thenReturn(Mono.just(post));
        Mono<Post> postMono = postService.getPost(post.getId());
        StepVerifier.create(postMono)
                .consumeNextWith(actualPost -> {
                    assertEquals(post.getId(), actualPost.getId());
                    assertEquals(post.getTitle(), actualPost.getTitle());
                    assertEquals(post.getBody(), actualPost.getBody());
                })
                .verifyComplete();
    }

    @Test
    void testGetPostShouldReturnErrorWithInvalidId(){
        when(postRepository.findById(anyInt())).thenReturn(Mono.empty());
        StepVerifier.create(postService.getPost(1))
                .verifyError();
    }

    @Test
    void testCreatePostShouldReturnNewPostWithId(){
        Faker faker = new Faker();
        PostRequestDto postRequestDto = PostRequestDto.builder().title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build();
        Post post = Post.builder().id(1).title(postRequestDto.getTitle()).body(postRequestDto.getBody()).build();
        when(postRepository.save(any())).thenReturn(Mono.just(post));
        StepVerifier.create(postService.createPost(Mono.just(postRequestDto)))
                .consumeNextWith(createdPost -> {
                    assertNotNull(createdPost.getId());
                    assertEquals(post.getId(), createdPost.getId());
                    assertEquals(post.getTitle(), createdPost.getTitle());
                    assertEquals(post.getBody(), createdPost.getBody());
                })
                .verifyComplete();
    }

    @Test
    void testDeletePostReturnErrorWithInvalidId(){
        when(postRepository.findById(anyInt())).thenReturn(Mono.empty());
        StepVerifier.create(postService.deletePost(1))
                .verifyError();
    }

    @Test
    void testDeletePostWithValidId(){
        Faker faker = new Faker();
        Post post = Post.builder().id(1).title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build();
        when(postRepository.findById(anyInt())).thenReturn(Mono.just(post));
        when(postRepository.deleteById(anyInt())).thenReturn(Mono.empty());
        StepVerifier.create(postService.deletePost(post.getId()))
                .consumeNextWith(deletedPost -> {
                    assertEquals(post.getId(), deletedPost.getId());
                })
                .verifyComplete();
    }

    @Test
    void testUpdatePostReturnErrorWithInvalidId(){
        when(postRepository.findById(anyInt())).thenReturn(Mono.empty());
        Faker faker = new Faker();
        PostRequestDto postRequestDto = PostRequestDto.builder().title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build();
        StepVerifier.create(postService.updatePost(1, postRequestDto))
                .verifyError();
    }

    @Test
    void testUpdatePostReturnUpdatedPostWithValidId(){
        String updatedTitle = "This is the updated title";
        String body = "This is the body title";
        Post initialPost = Post.builder().id(1).title("This is the title").body(body).build();
        when(postRepository.findById(anyInt())).thenReturn(Mono.just(initialPost));
        Post updatedPost = Post.builder().id(initialPost.getId()).title(updatedTitle).body(body).build();
        when(postRepository.save(any())).thenReturn(Mono.just(updatedPost));
        PostRequestDto postRequestDto = PostRequestDto.builder().title(updatedTitle).body(body).build();
        StepVerifier.create(postService.updatePost(1, postRequestDto))
                .consumeNextWith(actualPost -> {
                    assertEquals(updatedPost.getId(), actualPost.getId());
                    assertEquals(updatedPost.getTitle(), actualPost.getTitle());
                    assertEquals(updatedPost.getBody(), actualPost.getBody());
                })
                .verifyComplete();
    }

}