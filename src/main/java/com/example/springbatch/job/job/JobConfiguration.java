//package com.example.springbatch.job;
//
//import com.example.springbatch.CustomJobParametersValidator;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.job.builder.FlowBuilder;
//import org.springframework.batch.core.job.flow.Flow;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//@RequiredArgsConstructor
//public class JobConfiguration {
//
//    private final JobBuilderFactory jobBuilderFactory;
//    private final StepBuilderFactory stepBuilderFactory;
//
////    @Bean
////    public Job job() {
////        return jobBuilderFactory.get("job")
////                .start(step1())
////                .next(step2())
////                .build();
////    }
//
//    @Bean
//    public Job job2() {
//        return this.jobBuilderFactory.get("job2")
//                .start(flow())
//                .next(step3())
//                .end()
//                //.validator(new DefaultJobParametersValidator(new String[]{"name","date"}, new String[]{"count"}))
//                .validator(new CustomJobParametersValidator())
//                .build();
//    }
//
////    @Bean
////    public Step step1() {
////        return stepBuilderFactory.get("step1")
////                .tasklet(new Tasklet() {
////                    @Override
////                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
////                        System.out.println("step1 was executed");
////                        return RepeatStatus.FINISHED;
////                    }
////                })
////                .build();
////    }
////
////    @Bean
////    public Step step2() {
////        return stepBuilderFactory.get("step2")
////                .tasklet(new Tasklet() {
////                    @Override
////                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
////                        System.out.println("step2 was executed");
////                        return RepeatStatus.FINISHED;
////                    }
////                })
////                .build();
////    }
//
//    @Bean
//    public Flow flow() {
//        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
//        flowBuilder.start(step3())
//                .next(step4())
//                .end();
//
//        return flowBuilder.build();
//    }
//
//    @Bean
//    public Step step3() {
//        return stepBuilderFactory.get("step3")
//                .tasklet(new Tasklet() {
//                    @Override
//                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                        System.out.println("step2 was executed");
//                        return RepeatStatus.FINISHED;
//                    }
//                })
//                .build();
//    }
//
//    @Bean
//    public Step step4() {
//        return stepBuilderFactory.get("step4")
//                .tasklet(new Tasklet() {
//                    @Override
//                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                        System.out.println("step2 was executed");
//                        return RepeatStatus.FINISHED;
//                    }
//                })
//                .build();
//    }
//}
