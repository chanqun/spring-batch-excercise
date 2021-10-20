//package com.example.springbatch.part1
//
//import mu.KotlinLogging
//import org.springframework.batch.core.Job
//import org.springframework.batch.core.Step
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
//import org.springframework.batch.repeat.RepeatStatus
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//private val logger = KotlinLogging.logger {}
//
//@Configuration
//class HelloConfiguration(
//    private val jobBuilderFactory: JobBuilderFactory,
//    private val stepBuilderFactory: StepBuilderFactory
//) {
//
//    // --spring.batch.job.names=helloJob 을 주면 이것만 실행
//    @Bean
//    fun helloJob(): Job { //spring batch 의 실행 단위
//        return jobBuilderFactory.get("helloJob")
//            .start(helloStep()) // job 실행시 최초로 실행 될 메소드
//            .next(helloStep2())
//            .build()
//    }
//
//    @Bean
//    fun helloStep(): Step { //step은 job의 실행 단위 하나의 job은 한 개이상의 step 을 가질 수 있음
//        return stepBuilderFactory.get("helloStep")
//            .tasklet { stepContribution, chunkContext ->
//                logger.info { "hello spring batch" }
//                RepeatStatus.FINISHED
//            }.build()
//    }
//
//    @Bean
//    fun helloStep2(): Step {
//        return stepBuilderFactory.get("helloStep2")
//            .tasklet { stepContribution, chunkContext ->
//                logger.info { "hello spring batch2" }
//                RepeatStatus.FINISHED
//            }.build()
//    }
//}
