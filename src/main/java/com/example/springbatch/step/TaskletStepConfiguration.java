//package com.example.springbatch.step;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.support.ListItemReader;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@RequiredArgsConstructor
//public class TaskletStepConfiguration {
//    private final JobBuilderFactory jobBuilderFactory;
//    private final StepBuilderFactory stepBuilderFactory;
//
//    @Bean
//    public Job batchJob() {
//        return jobBuilderFactory.get("batchJob")
//                .incrementer(new RunIdIncrementer())
//                .start(chunkStep())
//                .build();
//    }
//
//    @Bean
//    public Step chunkStep() {
//        return stepBuilderFactory.get("chunkStep")
//                .<String, String>chunk(10)
//                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5")))
//                .processor(new ItemProcessor<String, String>() {
//                    @Override
//                    public String process(String item) throws Exception {
//                        return item.toUpperCase();
//                    }
//                })
//                .writer(new ItemWriter<String>() {
//                    @Override
//                    public void write(List<? extends String> items) throws Exception {
//                        items.forEach(item -> System.out.println(item));
//                    }
//                })
//                .build();
//    }
//}
//
