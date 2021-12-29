package com.example.springbatch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.batch.core.*
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import javax.persistence.EntityManager

@RunWith(SpringRunner::class)
@SpringBatchTest
@SpringBootTest(classes = [ItemConfiguration::class, TestBatchConfig::class])
class StakingConfigurationTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val entityManager: EntityManager
) {

    @Test
    fun `Configuration 배치 테스트`() {
        val confMap = hashMapOf(
            Pair("time", JobParameter(System.currentTimeMillis()))
        )
        val jobParameters = JobParameters(confMap)

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)
//        val jobExecution = jobLauncherTestUtils.launchStep("jpaItemWriterStep", jobParameters)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus).isEqualTo(ExitStatus.COMPLETED)

        val stepExecution = jobExecution.stepExecutions as List<StepExecution>

        assertThat(stepExecution[0].commitCount).isEqualTo(1)
    }

//    @AfterEach
//    fun clear() {
//        stakingRepository.deleteAll()
//    }
}
