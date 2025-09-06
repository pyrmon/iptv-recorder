package me.schickel.recorder.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.processing.Generated;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Generated("Spring Configuration")
@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}