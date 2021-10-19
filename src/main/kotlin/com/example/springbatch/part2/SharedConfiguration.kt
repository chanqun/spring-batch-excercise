//package com.example.springbatch.part2
//
//import mu.KotlinLogging
//import org.springframework.batch.core.Job
//import org.springframework.batch.core.Step
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
//import org.springframework.batch.core.launch.support.RunIdIncrementer
//import org.springframework.batch.item.ExecutionContext
//import org.springframework.batch.repeat.RepeatStatus
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//
//private val logger = KotlinLogging.logger {}
//
//@Configuration
//class SharedConfiguration(
//    private val jobBuilderFactory: JobBuilderFactory,
//    private val stepBuilderFactory: StepBuilderFactory
//) {
//    @Bean
//    fun sharedJob(): Job {
//        return jobBuilderFactory.get("sharedJob")
//            .incrementer(RunIdIncrementer())
//            .start(this.sharedStep())
//            .next(this.sharedStep2())
//            .build()
//    }
//
//    @Bean
//    fun sharedStep2(): Step { //step은 job의 실행 단위 하나의 job은 한 개이상의 step 을 가질 수 있음
//        return stepBuilderFactory.get("sharedStep2")
//            .tasklet { contribution, chunkContext ->
//                val stepExecution = contribution.stepExecution
//                val stepExecutionContext = stepExecution.executionContext
//
//                stepExecutionContext.putString("stepKey", "step execution context")
//
//                val jobExecution = stepExecution.jobExecution
//                val jobInstance = jobExecution.jobInstance
//                val jobExecutionContext = jobExecution.executionContext
//                jobExecutionContext.putString("jobKey", "job execution context")
//                val jobParameters = jobExecution.jobParameters
//
//                logger.info {
//                    "jobName: ${jobInstance.jobName}," +
//                            " stepName: ${stepExecution.stepName}, parameter ${
//                                jobParameters.getLong(
//                                    "run.id"
//                                )
//                            }"
//                }
//                RepeatStatus.FINISHED
//            }.build()
//    }
//
//    @Bean
//    fun sharedStep(): Step {
//        return stepBuilderFactory.get("sharedStep")
//            .tasklet { contribution, chunkContext ->
//                // step ExecutionContext.get
//                // step ExecutionContext.get
//                val stepExecution = contribution.stepExecution
//                val stepExecutionContext: ExecutionContext = stepExecution.getExecutionContext()
//
//                // job ExecutionContext.get
//
//                // job ExecutionContext.get
//                val jobExecution = stepExecution.getJobExecution()
//                val jobExecutionContext: ExecutionContext = jobExecution.getExecutionContext()
//
//                // log
//
//                // log
//                logger.info {
//                    "jobValue : ${
//                        jobExecutionContext.getString(
//                            "jobKey",
//                            "emptyJob"
//                        )
//                    }, stepValue : ${stepExecutionContext.getString("stepKey", "emptyStep")}"
//                }
//
//                RepeatStatus.FINISHED
//            }.build()
//    }
//}