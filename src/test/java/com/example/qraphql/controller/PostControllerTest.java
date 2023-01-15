package com.example.qraphql.controller;

import com.example.qraphql.exception.PostNotFoundException;
import com.example.qraphql.model.Post;
import com.example.qraphql.repository.PostRepository;
import com.example.qraphql.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@GraphQlTest(PostController.class)
@ExtendWith(MockitoExtension.class)
class PostControllerTest {
    @Autowired
    private GraphQlTester graphQlTester;
    @MockBean
    private PostService postService;
    @MockBean
    private PostRepository postRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private Faker faker = new Faker();


    @Test
    void testGetPostsShouldReturnPosts(){
        String query = """
        query {
            getPosts{
                id
                title
                body
            }
        }
        """;
        List<Post> postList = IntStream.range(0, 10)
                .mapToObj(i -> Post.builder().id(i + 1).title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build())
                .collect(Collectors.toList());
        when(postService.getPosts()).thenReturn(Flux.fromIterable(postList));

        graphQlTester.document(query)
                .execute()
                .path("getPosts")
                .entityList(Post.class)
                .hasSize(postList.size());
    }

    @Test
    void testGetPostWithInvalidIdShouldReturnError(){
        String document = """
        query GetPost($id: ID!){
            getPost(id: $id) {
                id
                title
                body
            }
        }       
        """;
        when(postService.getPost(anyInt())).thenReturn(Mono.error(new PostNotFoundException("Post Not Found. id: 1")));
        graphQlTester.document(document)
                .variable("id", 1)
                .execute()
                .errors()
                .expect(error -> error.getMessage() == "Post Not Found. id: 1")
                .verify()
                .path("getPost")
                .valueIsNull();
    }

    @Test
    void testGetPostWithValidIdShouldReturnPost(){
        String document = """
        query GetPost($id: ID!){
            getPost(id: $id) {
                id
                title
                body
            }
        }       
        """;
        Post post = Post.builder().id(1).title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build();
        when(postService.getPost(anyInt())).thenReturn(Mono.just(post));
        graphQlTester.document(document)
                .variable("id", post.getId())
                .execute()
                .path("getPost")
                .entity(Post.class)
                .satisfies(actualPost -> {
                    assertEquals(post.getId(), actualPost.getId());
                    assertEquals(post.getTitle(), actualPost.getTitle());
                    assertEquals(post.getBody(), actualPost.getBody());
                });
    }

    @Test
    void testCreatePostWithBlankOrEmptyInputShouldReturnError() throws JsonProcessingException {
        String document = """
        mutation CreatePost($input: PostInput!) {
            createPost(createPostInput: $input) {
                id
                title
                body
            }
        }
        """;
        Map<String, String> postRequest = Map.of("title","  ", "body", "");
        graphQlTester.document(document)
                .variable("input", postRequest)
                .execute()
                .errors()
                .expect(error -> error.getErrorType().toString() == "DataFetchingException")
                .verify()
                .path("createPost")
                .valueIsNull();
    }

    @Test
    void testCreatePostWithInvalidLengthInputShouldReturnError() throws JsonProcessingException {
        String document = """
        mutation CreatePost($input: PostInput!) {
            createPost(createPostInput: $input) {
                id
                title
                body
            }
        }
        """;
        Map<String, String> postRequest = Map.of("title","This is the post with a very long title", "body", "This is the post body");
        graphQlTester.document(document)
                .variable("input", postRequest)
                .execute()
                .errors()
                .expect(error -> error.getErrorType().toString() == "DataFetchingException")
                .verify()
                .path("createPost")
                .valueIsNull();
    }

    @Test
    void testCreatePostWithValidInputShouldReturnNewPost() throws JsonProcessingException {
        String document = """
        mutation CreatePost($input: PostInput!) {
            createPost(createPostInput: $input) {
                id
                title
                body
            }
        }
        """;
        String title = "This is the title";
        String body =  "This is the post body";
        Map<String, String> postRequest = Map.of("title",title, "body", body);
        when(postService.createPost(any())).thenReturn(Mono.just(Post.builder().id(1).title(title).body(body).build()));
        graphQlTester.document(document)
                .variable("input", postRequest)
                .execute()
                .path("createPost")
                .entity(Post.class)
                .satisfies(post -> {
                    assertNotNull(post.getId());
                    assertEquals(title, post.getTitle());
                    assertEquals(body, post.getBody());
                });
    }

    @Test
    void testDeleteWithInvalidIdShouldReturnError() {
        String document = """
        mutation DeletePost($id: ID!){
            deletePost(id: $id) {
                id
                title
                body
            }
        }       
        """;
        when(postService.deletePost(anyInt())).thenReturn(Mono.error(new PostNotFoundException("Post Not Found. id: 1")));
        graphQlTester.document(document)
                .variable("id", 1)
                .execute()
                .errors()
                .expect(error -> error.getMessage() == "Post Not Found. id: 1")
                .verify()
                .path("deletePost")
                .valueIsNull();
    }

    @Test
    void testDeleteWithValidIdShouldReturnDeletedPost(){
        String document = """
        mutation DeletePost($id: ID!){
            deletePost(id: $id) {
                id
                title
                body
            }
        }       
        """;
        Post deletedPost = Post.builder().id(1).title("This is deleted").body("This is the title of the deleted post").build();
        when(postService.deletePost(anyInt())).thenReturn(Mono.just(deletedPost));
        graphQlTester.document(document)
                .variable("id", deletedPost.getId())
                .execute()
                .path("deletePost")
                .entity(Post.class)
                .satisfies(post -> {
                    assertEquals(deletedPost.getId(), post.getId());
                    assertEquals(deletedPost.getTitle(), post.getTitle());
                    assertEquals(deletedPost.getBody(), post.getBody());
                });
    }

    @Test
    void testUpdatePostWithInvalidIdShouldReturnError(){
        String document = """
        mutation UpdatePost($id: ID!, $input: PostInput!) {
            updatePost(id: $id, updatePostInput: $input) {
                id
                title
                body
            }
        }
        """;
        when(postService.updatePost(anyInt(), any())).thenReturn(Mono.error(new PostNotFoundException("Post Not Found. id: 1")));
        Map<String, String> postRequest = Map.of("title","Updated title", "body", "This is the updated post body");
        graphQlTester.document(document)
                .variable("id", 1)
                .variable("input", postRequest)
                .execute()
                .errors()
                .expect(error -> error.getMessage() == "Post Not Found. id: 1")
                .verify()
                .path("updatePost")
                .valueIsNull();
    }

    @Test
    void testUpdatePostShouldReturnUpdatedPost(){
        String document = """
        mutation UpdatePost($id: ID!, $input: PostInput!) {
            updatePost(id: $id, updatePostInput: $input) {
                id
                title
                body
            }
        }
        """;
        String updatedTitle = "Updated TItle";
        String body = "This is the post body";
        Post updatedPost = Post.builder().id(1).title(updatedTitle).body(body).build();
        Map<String, String> postRequest = Map.of("title", updatedTitle, "body", body);
        when(postService.updatePost(anyInt(), any())).thenReturn(Mono.just(updatedPost));
        graphQlTester.document(document)
                .variable("id", 1)
                .variable("input", postRequest)
                .execute()
                .path("updatePost")
                .entity(Post.class)
                .satisfies(post -> {
                    assertEquals(updatedPost, post);
                });
    }

}