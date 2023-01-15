package com.example.qraphql.config;

import com.example.qraphql.model.Post;
import com.example.qraphql.repository.PostRepository;
import com.github.javafaker.Faker;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import reactor.core.publisher.Flux;

@Configuration
public class Config {
    @Autowired
    private PostRepository postRepository;

    @Bean
	public ConnectionFactoryInitializer connectionFactoryInitializer(ConnectionFactory connectionFactory){
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
		return initializer;
	}

    @PostConstruct
    public void initialDataLoader(){
        Faker faker = new Faker();
        Flux<Post> postFlux = Flux.range(0, 10)
                .map(i -> Post.builder().title(faker.lorem().fixedString(15)).body(faker.lorem().fixedString(255)).build());

        postRepository.deleteAll()
                .thenMany(postFlux.flatMap(post -> postRepository.save(post)))
                .log()
                .subscribe();
    }
}
