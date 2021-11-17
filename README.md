# Spring Batch

정수원님 배치강의 수강
[spring batch](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#curriculum)

1. 스프링 배치 소개
2. 시작
3. 도메인 이해
4. 실행
5. 청크 프로세스 (1)
6. 청크 프로세스 (2)
7. 반복 및 오류 제어
8. 멀티 스레드 프로세싱
9. 리스너
10. 테스트 및 운영

##### 1. 스프링 배치 탄생 배경

- 자바 기분 표준 배치 기술 부재
    - 배치 처리에서 요구하는 재사용 가능한 자바 기반 배치 아키텍처 표준의 필요성이 대두
- 스프링 배치는 SpringSource와 Accenture의 합작품
    - Accenture - 배치 아키텍처를 구현하면서 쌓은 기술적인 경험과 노하우
    - SpringSource - 깊이 있는 기술적 기반과 스프링의 프로그램이 모델

##### 2. 배치 핵심 패턴

- Read - 데이터베이스, 파일, 큐에서 다량의 데이터 조회 (Extract)
- Process - 특정 방법으로 데이터를 가공 (Transform)
- Write - 데이터를 수정된 양식으로 다시 저장 (Load)

##### 3. 배치 시나리오

- 배치 프로세스를 주기적으로 커밋
- 동시 다발적인 Job의 배치 처리, 대용량 병렬 처리
- 실패 후 수동 또는 스케줄링에 의한 재시작
- 의존관계가 있는 step 여러 개를 순차적으로 처리
- 조건적 Flow 구성을 통한 체계적이고 유연한 배치 모델 구성
- 반복, 재시도, Skip 처리

#### 아키텍처

Application

- 스프링 배치 프레임워크를 통해 개발자가 모든 배치 Job과 커스텀 코드를 포함
- 개발자는 엄부로직의 구현에만 집중하고 공통적인 기반기술은 프레임웍이 담당하게 한다.

Batch Core

- Job을 실행, 모니터링, 관리하는 API로 구성되어 있다.
- JobLauncher, Job, Step, Flow 등이 속한다.

Batch Infrastructure

- Application Core 모두 공통 Infrastructure 위에서 빌드한다.
- Job 실행의 흐름과 처리를 위한 틀을 제공함
- Reader, Processor, Writer, Skip, Retry 등이 속한다.

#### 스프링 배치 활성화

- @EnableBatchProcessing
    - 총 4개의 설정 클래스를 실행시키며 스프링 배치의 모든 초기화 및 실행 구성이 이루어진다
    - 스프링 부트 배치의 자동 설정 클래스가 실행됨으로 빈으로 등록된 모든 Job을 검색해서 초기화와 동시에 Job을 수행하도록 구성됨

1. BatchAutoConfiguration

- Job을 수행하는 JobLauncherApplicationRunner

2. SimpleBatchConfiguration

- JobBuilderFactory 와 StepBuilderFactory 생성
- 스프링 배치의 주요 구성 요소 생성 - 프록시 객체로 생성됨

3. BatchConfigurerConfiguration

- BasicBatchConfigurer
    - SimpleBatchConfiguration 에서 생성한 프록시 객체의 시제 대상 객체를 생성하는 설정 클래스
    - 빈으로 의존성 주입 받아서 주요 객체들을 참조해서 사용할 수 있다.
    - JpaBatchConfigurer

```java
1.@Configuration 선언
        -하나의 배치 Job을 정의하고 빈 설정

        2.JobBuilderFactory
        -Job 을 생성하는 빌더 팩토리
        3.StepBuilderFactory
        -Step을 생성하는 빌더 팩토리
        4.Job
        -Job 생성
        5.Step
        -Step 생성
        6.tasklet
        -step 안에서 단일 태스크로 수행되는 로직 구현
        7.Job 구동->Step을 실행->Tasklet을 실행
```

![img1](./image/img1.png)

![img2](./image/img2.png)

### 스프링 배치 시작 - DB 스키마 생성

1. 스프링 배치 메타 데이터

- 스프링 배치의 실행 및 관리를 위한 목적으로 여러 도메인들 (Job, Step, JobParameters)의 정보들을 저장, 업데이트, 조회할 수 있는 스키마 제공
- 과거, 현재의 실행에 대한 세세한 정보, 실행에 대한 성공과 실패 여부 등을 일목요연하게 관리함으로서 배치운옹에 있어 리스크 발생시 빠른 대처 가능
- DB와 연동할 경우 필수적으로 메타 테이블이 생성 되어야 함

2. DB스키마 제공

- 파일 위치 : /org/springframework/batch/core/schema-*.sql
- db 유형별로 제공

3. 스키마 생성 설정

- 수동 생성 - 쿼리 복사 후 직접 실행
- 자동 생성 - spring.batch.jdbc.initialize-schema 설정
    - always : 스크립트 항상 실행, RDBMS 설정이 되어 있을 경우 내장 DB보다 우선적으로 실행
    - embedded : 내장 db일 때만 실행되며 스키마가 자동 생성되, 기본값
    - never : 스크립트 항상 실행 안함, 내장 db 일 경우 스크립트가 생성이 안되기 때문에 오류 발생, 운영에서 수동으로 스크립트 생성 후 설정하는 것을 권장


- 배치 실행을 위한 메타 데이터가 저장되는 테이블

Job 관련 테이블

- BATCH_JOB_INSTANCE
    - Job이 실행되며 생성되는 최상위 계층의 테이블, job_name과 job_key로 중복 저장될 수 없다.
- BATCH_JOB_EXECUTION
    - Job이 실행되는 동안 시작/종료 시간, job등을 관리
- BATCH_JOB_EXECUTION_PARAMS
    - Job을 실행하기 위해 주입된 parameter 정보 저장
- BATCH_JOB_EXECUTION_CONTEXT
    - Job이 실행되며 공유해야할 데이터를 직렬화해 저장(json)

Step 관련 테이블

- BATCH_STEP_EXECUTION
    - Step이 실행되는 동안 필요한 데이터 또는 실행된 결과 저장
- BATCH_STEP_EXECUTION_CONTEXT
    - Step이 실행되며 공유해야할 데이터를 직렬화해 저장, step 별로 저장되며 step 간 서로 공유할 수 없음

-> batch -> core 밑에 sql 밑에 존재

### 스프링 배치 도메인 이해

1. Job
    - Job
    - JobInstance
    - JobParameters
    - JobExecution
2. Step
3. ExecutionContext
4. JobRepository / JobLauncher

#### Job

1. 기본 개념

- 배치 계층 구조에서 가장 상위에 있는 개념으로서 하나의 배치작업 자체를 의미함
    - API 서버의 접속 로그 데이터를 통계 서버로 옮기는 배치인 Job 자체를 의미한다.
- Job Configuration 을 통해 생성되는 객체 단위로서 배치작업을 어떻게 구성하고 실행할 것인지 전체적으로 설정하고 명세해 놓은 객체
- 배치 Job을 구성하기 위한 최상위 인터페이스이며 스프링 배치가 기본 구현체를 제공한다.
- 여러 Step을 포함하고 있는 컨테이너로서 반드시 한개 이상의 Step으로 구성해야 함


2. 기본 구현체

- SimpleJob
    - 순차적으로 Step을 실행시키는 Job
- FlowJob
    - 특정한 조건과 흐름에 따라 Step 을 구성하여 실행시키는 Job

JobParameters | JobLauncher ->              Job execute (Step, Step, Step)
run(job, parameters)

Job <- AbstractJob  <- (SimpleJob, FlowJob)

### JobInstance

1. 기본 개념
    - Job이 실행될 때 생성되는 Job의 논리적 실행 단위 객체로서 고유하게 식별 가능한 작업 실행을 나타냄
    - Job의 설정과 구성은 동일하지만 Job이 실행되는 시점에 처리하는 내용은 다르기 때문에 Job의 실행을 구분해야 함
        - 예를 들어 하루에 한 번 씩 배치 Job이 실행된다면 매일 실행되는 각각의 Job을 JobInstance로 표현한다.
    - JobInstance 생성 및 실행
        - 처음 시작하는 Job + JobParameter 일 경우 새로운 JobInstance 생성
        - 이전과 동일한 Job + JobParameter로 실행 할 경우 이미 존재하는 JobInstance 리턴
            - 내부적으로 JobNmae + jobKey 를 가지고 JobInstance 객체를 얻음
    - Job 과는 1:M 관계

![img3](./image/img3.png)

JobInstance = Job + JobParameters (BATCH_JOB_INSTANCE)에 저장

### JobParameter

1. 기본 개념
    - Job을 실행할 때 함께 포함되어 사용되는 파라미터를 가진 도메인 객체
    - 하나의 Job에 존재할 수 있는 여러개의 JobInstance를 구분하기 위한 용도
    - JobParameters와 JobInstance 1:1 관계

2. 생성 및 바인딩
    - 어플리케이션 실행 시 주입
        - Java -jar LogBatch.jar requestDate = 20210101
    - 코드로 생성
        - JobParameterBuilder, DefaultJobParametersConverter
    - SpEL 이용
        - @Value("#{jobParameter[requestDate]}"), @JobScope, @StepScope 선언 필수

3. BATCH_JOB_EXECUTION_PARAM 테이블과 매핑
    - JOB_EXECUTION 과 1:M 의 관계

> name=user1 seq(long)=2 date(date)=2021/10/25

### JobExecution

1. 기본 개념
    - JobInstance 에 대한 한 번의 시도를 의미하는 객체로서 Job실행 중에 발생한 정보들을 저장하고 있는 객체
        - 시작시간, 종료시간, 상태, 종료상태의 속성을 가짐
    - JobInstance 와의 관계
        - JobExecution은 FAILED, COMPLETED 등의 Job의 실행 결과 상태를 가지고 있음
        - COMPLETED이면 재실행 불가
        - FAILED이면 재실행이 가능
        - COMPLETED 될 떄까지 하나의 JobInstance 내에서 여러 번의 시도가 생길 수 있음
2. BATCH_JOB_EXECUTION 테이블과 매핑
    - JobInstance와 1:M

### Step

1. 기본 개념
    - Batch Job을 구성하는 독립적인 하나의 단계로서 실제 배치 처리를 정의하고 컨트롤하는 데 필요한 모든 정보를 가지고 있는 도메인 객체
    - 단순한 단일 태스크 뿐 아니라 입력과 처리 그리고 출력과 관련된 복잡한 비즈니스 로직을 포함하는 모든 설정들을 담고 있다.
    - 배치작업을 어떻게 구성하고 실행할 것인지 Job의 세부 작업을 Task 기반으로 설정하고 명세해 놓은 객체
    - 모든 Job은 하나 이상의 step으로 구성

2. 기본 구현체
    - TaskletStep
        - 가장 기본이 되는 클래스로서 Tasklet 타입의 구현체들을 제어한다.
    - PartitionStep
        - 멀티 스레드 방식으로 step을 여러 개로 분리해서 실행한다.
    - JobStep
        - Step 내에서 Job을 실행하도록 한다.
    - FlowStep
        - Step 내에서 Flow를 실행하도록 한다. ...

### StepExecution

1. 기본 개념
    - Step에 대한 한 번의 시도를 의미하는 객체로서 Step실행 중에 발생한 정보들을 저장하고 있는 객체
        - 시작시간, 종료시간, 상태, commit count, rollback count등의 속성을 가짐
    - Step이 매번 시도될 때마다 생성되며 각 Step별로 생성
    - Job이 재시작 하더라도 이미 성공적으로 각 Step별로 생성된다.
    - Job이 재시작 하더라도 이미 성공적으로 완료된 Step은 재실행되지 않고 실패한 Step만 실행된다.
    - 이전 단계 Step이 실패해서 현재 Step을 실행하지 않았다면 StepExecution 을 생성하지 않는다. Step이 실제로 시작됐을 때만 StepExecution을 생성한다.
    - JobExecution 과의 관계
        - Step의 StepExecution 이 모두 정상적으로 완료 되어야 JobExecution이 정상적으로 완료된다.
        - Step의 StepExecution 중 하나라도 실패하면 JobExecution은 실패한다.

2. BATCH_STEP_EXECUTION 테이블과 매핑
    - JobExecution 와 StepExecution 은 1:M 의 관계
    - 하나의 Job에 여러 개의 Step으로 구성했을 경우 각 StepExecution은 하나의 JobExecution을 부모로 가진다.

![img4](./image/img4.png)

### StepContribution

1. 기본 개념
    - 청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체
    - 청크 커밋 직전에 StepExecution 의 apply 메서드를 호출하여 상태를 업데이트 함
    - ExitStatus 의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용 할 수 있다

TaskletStep -> StepExecution -> StepContribution | ChunkOrientedTasklet

### ExecutionContext

1. 기본 개념
    - 프레임워크에서 유지 및 관리하는 키/값으로 된 컬렉션으로 StepExecution 또는 JobExecution 객체의 상태를 저장하는 공유 객체
    - DB에 직렬화 한 값으로 저장됨
    - 공유 범위
        - Step 범위 - 각 Step의 StepExecution에 저장되며 Step 간 서로 공유 안 됨
        - Job 범위 - 각 Job의 JobExecution에 저장되며 Job 간 서로 공유 안 되며 해당 Job의 Step간 서로 공유됨
    - Job 재 시작시 이미 처리한 Row 데이터는 건너뛰고 이후로 수행하도록 할 때 상태 정보를 활용한다.

2. 유지 관리에 필요한 키값 설정

```java
Map<String, Object> map=new ConcurrentHashMap
```

### JobRepository

1. 기본 개념
    - 배치 작업 중의 정보를 저장하는 저장소 역할
    - Job이 언제 수행되었고, 언제 끝났으며, 몇 번이 실행되었고 실행에 대한 결과 등의 배치 작업의 수행과 관련된 모든 meta data를 저장함
        - JobLauncher, Job, Step 구현체 내부에서 CRUD 기능을 처리함

- JobRepository 설정
    - @EnableBatchProcessing 어노테이션만 선언하면 JobRepository가 자동으로 빈으로 생성
    - BatchConfigurer 인터페이스를 구현하서나 BasicBatchConfigurer를 상속해서 JobRepository 설정을 커스터마이징 할 수 있다
        - JDBC방식으로 설정 - JobRepositoryFactoryBean
            - 내부적으로 AOP 기술을 통해 트랜잭션 처리를 해주고 있음
            - 트랜잭셩 isolation의 기본값은 SERIALIZEBLE 로 최고 수준, 다른 레벨로 지정 가능
            - 메타테이블의 Table Prefix를 변경할 수 있음 기본값은 BATCH_ 임
    - In Memory 방식으로 설정 - MApJobRepositoryFactoryBean
        - 성능 등의 이유로 도매인 오브젝트를 굳이 데이터베이스에 저장하고 싶지 않을 경우
        - test or proto type

### JobLauncher

1. 기본 개념
    - 배치 Job을 실행시키는 역할을 한다.
    - Job과 Job Parameters를 인자로 받으며 요청된 배치 작업을 수행한 후 최종 client에게 JobExecution을 반환함
    - 스프링 부트 배치가 구동이 되면 JobLauncher 빈이 자동 생성 된다.
    - Job실행
        - JobLauncher.run(Job, JobParameters)
        - 스프링 부트 배치에서는 JobLauncherApplicationRunner 가 자동적으로 JobLauncher 을 실행시킨다.
        - 동기적 실행
            - taskExecutor를 SyncTaskExecutor로 설정한 경우 (기본값은 SyncTaskExecutor)
            - JobExecution 을 획득하고 배치 처리를 최종 완료한 이후 Client에게 JobExecution을 반환
            - 스케줄러에 의한 배치처리에 적합 함 - 배치처리시간이 길어도 상관없는 경우
        - 비 동기적 실행
            - taskExecutor가 SimpleAsyncExecutor로 설정할 경우
            - JobExecution을 획득한 후 Client에게 바로 JobExecution을 반환하고 배치처리를 완료한다.
            - HTTP 요청에 의한 배치처리에 적합함 - 배치처리 시간이 길 경우 응답이 늦어지지 않도록 함
2. 구조 JobLauncher

> JobExecution run(Job, JobParameters)

![img5](./image/img5.png)

### 배치 초기화 설정

1. JobLauncherApplicationRunner
    - Spring Batch 작업을 시작하는 ApplicationRunner로서 BatchConfiguration에서 생성됨
    - 스프링 부트에서 제공하는 ApplicationRunner의 구현체로 어플리케이션이 정상적으로 구동될때 마다 실행됨
    - 기본적으로 빈으로 등록된 모든 job을 실행시킨다.

2. BatchProperties
    - Spring Batch의 환경 설정 클래스
    - Job이름, 스키마 초기화 설정, 테이블 prefix등의 값을 설정할 ㅅ ㅜ있다.
    - application.properties or applicaion.yml 파일에 설정함

```
batch:
    job:
        names: ${job.name:NONE}
    initialize-schema: NEVER
    tablePrefix: SYSTEM
```

3. Job 실행 옵션
    - 지정한 Batch Job만 실행하도록 할 수 있음
    - spring.batch.job.names
    - 어플리케이션 실행시 Program arguments 로 job 이름 입력한다.
    - --job.name=helloJob, simpleJob

### Job and Step

- JobBuilderFactory & JobBuilder
- SimpleJob
- StepBuilderFactory & StepBuilder
- TaskletStep
- JobStep

### JobBuilderFactory / JobBuilder

1. 스프링 배치는 Job 과 Step 을 쉽게 생성 및 설정할 수 있도록 util 성격의 빌더 클래스들을 제공
2. JobBuilderFactory
    - JobBuilder 를 생성하는 팩토리 클래스로서 get(String name) 메서드 제공
    - jobBuilderFactory.get("jobName")
        - "jobName"은 스프링 배치가 Job을 실행시킬 때 참조하는 Job의 이름

3. JobBuilder
    - Job을 구성하는 설정 조건에 따라 두 개의 하위 빌더 클래스를 생성하고 실제 Job생성을 위임한다.
    - SimpleJobBuilder
        - SimpleJob을 생성하는 Builder 클래스
        - Job실행과 관련된 여러 설정 API 를 제공한다.
    - FlowJobBuilder
        - FlowJob을 생성하는 Builder 클래스
        - 내부적으로 FlowBuilder를 반환함으로써 Flow 실행과 관련된 여러 설정 API를 제공한다

start(step) - SimpleJobBuilder[SimpleJob 생성]

start(flow), flow(step) - FlowJobBuilder -> JobFlowBuilder[Flow 생성]

Builder 들은 SimpleJobBuilder에서 bean으로 생성된다.

### SimpleJob

- 개념 및 API 소개
- API 설정
- 아키텍처

1. 기본 개념

- SimpleJob은 Step을 실행시키는 Job 구현체 SimpleJobBuiler에 의해 생성
- 여러 단계의 Step으로 구성 Step을 순차적으로 실행
- 모든 Step의 실행이 성공적으로 완료되어야 Job이 성공적으로 완료
- 맨 마지막에 실행한 Step의 BatchStatus 가 Job의 최종 BatchStatus가 된다.

simpleJob에 저장되는 항목 - start : 처음 실행 할 Step 설정, 최초 한 번 설정, 이 메서드를 실행하면 SimpleJobBuilder 반환 next : 다음에 실행 할 Step 설정, 횟수는
제한이 없으며 모든 next()의 Step이 종료가 되면 Job이 종료 incrementer : JobParameter의 값을 자동으로 증가해 주는 JobParametersIncremeter 설정
preventRestart : Job의 재시작 가능 여부 설정, validator : JobParameter를 실행하기 전에 올바른 구성이 되었는지 검증하는 JobParametersValidator 설정
listener : Job 라이프 사이클의 특정 시점에 콜백 제공받고록 JobExecutionListener 설정 build : SimpleJob 생성

### validator()

1. 기본개념
    - Job 실행에 꼭 필요한 파라미터를 검증하는 용도
    - DefaultJobParametersValidator 구현체를 지원하며, 좀 더 복잡한 제약 조건이 있다면 인터페이스를 직접 구현할 수 있음

2. 구조

```
JobPrametersValidator
void validate(@Nullable JobParameters parameters)
```

### preventRestart()

1. 기본개념
    - Job 의 재시작 여부를 설정
    - 기본 값은 true이며 false 로 설정 시 이 Job은 재시작을 지원하지 않는다. 라는 의미
    - Job이 실패해도 재시작이 안 되며 Job을 재 시작하려고 하면 JobRestartException 발생
    - 재 시작과 관련 있는 기능으로 Job을 처음 실행하는 것 과는 아무런 상관 없음

### incrementer()

1. 기본개념
    - JobParameters에서 필요한 값을 증가시켜 다음에 사용될 JobParameters 오브젝트를 리턴
    - 기존의 JobParameter 변경없이 Job을 여러 번 시작하고자 할때
    - RunIdIncrementer 구현체를 지원하며 인터페이스를 직접 구현할 수 있음

2. 구조 JobParametersIncrementer

```java
JobParameters getNext(@Nullable JobParameters parameters);
```

### SimpleJob 아키텍

![img6](./image/img6.png)
![img7](./image/img7.png)

### StepBuilderFactory / StepBuilder

1. StepBuilderFactory
    - StepBuilder를 생성하는 팩토리 클래스로서 get(String name)메서드 제공
    - StepBuilderFactory.get("stepName") -> stepName으로 Step 생성

2. StepBuilder
    - Step 을 구성하는 설정 조건에 따라 다섯 개의 하위 빌더 클래스를 생성하고 실제 Step생성을 위임한다.
    - TaskletStepBuilder : TaskletStep을 생성하는 기본 빌더 클래스
    - SimpleStepBuilder : TaskletStep을 생성하며 내부적으로 청크기반의 작업을 처리하는 ChunkOrientedTasklet 클래스를 생성한다.
    - PartitionStepBuilder : PartitionStep을 생성하며 멀티 스레드 방식으로 Job을 실행한다.
    - JobStepBuilder : JobStep을 생성하며 Step 안에서 Job을 실행한다.
    - FlowStepBuilder : FlowStep을 생성하여 Step안에서 Flow를 실행한다.

### TaskletStep

1. 기본개념
    - 스프링 배치에서 제공하는 Step의 구현체로서 Tasklet을 실행시키는 도매인 객체
    - RepeatTemplate 를 사용해 Tasklet의 구문을 트랜잭션 경계 내에서 반복해서 실행
    - Task 기반과 Chunk 기반으로 나누어서 Tasklet을 실행함

2. Task vs Chunk
    - 스프링 배치에서 Step의 실행 단위는 크게 2가지로 나누어짐
    - Chunk 기반
        - 하나의 큰 덩어리를 n개씩 나눠서 실행한다 대량 처리를 하는 경우 효과적으로 설계
        - ItemReader, ItemProcessor, ItemWriter 를 사용하며 청크 기반 전용 Tasklet인 ChunkOrientedTasklet 구현체가 제공
    - Task 기반
        - ItemReader 와 ItemWriter와 같은 청크 기반의 작업 보다 단일 작업 기반으로 처리되는 것이 더 효율적인 경우
        - 주로 Tasklet 구현체를 만들어 사용
        - 대량 처리를 하는 경우 chunk 기반에 비해 더 복잡한 구현 필

```java
public Step bachStep(){
    return stepBuilderFactory.get("batchStep")
        .tasklet(Tasklet)
        .startLimit(10)
        .allowStartIfComplete(true)
        .listener(StepExecutionListener)
        .build();
}
```


### tasklet()
1. 기본 개념
    - Tasklet 타입의 클래스를 설정한다.
      - Tasklet
        - Step내에서 구성되고 실행되는 도메인 객체로서 주로 단일 태스크를 수행하기 위한 것
        - TaskletStep 에 의해 반복적으로 수행되며 반환값에 따라 계속 수행 혹은 종료한다.
        - RepeatStatus - Tasklet의 반복 여부 상태 값
          - RepeatStatus.FINISHED - Tasklet 종료 == null
          - RepeatStatus.CONTINUABLE - Tasklet 반복
    - 익명 클래스 혹은 구현 클래스를 만들어서 사용
    - 이 메소드를 실행하게 되면 TaskletStepBuilder가 반환되어 관련 API를 설정할 수 있다.
    - Step 에 오직 하나의 Tasklet 설정이 가능하며 두 개 이상을 설정 했을 경우 마지막에 설정한 객체가 실행된다

2. 구조
Tasklet

RepeatStatus execute(StepContribution, ChinkContext);



### startLimit() / 

1. 기본개념
    - Step의 실행 횟수를 조정할 수 있다.
    - Step 마다 설정할 수 있다.
    - 설정 값을 초과해서 다시 실행하려고 하면 StartLimitExceededException 발생
    - start-limit의 디폴트 값은 Integer.MAX_VALUE

### allowStartIfComplete()

1. 기본개념
   - 재시작 가능한 job에서 Step의 이전 성공 여부와 상관없이 항상 step을 실행하기 위한 설정
   - 실행마다 유혀성을 검증하는 Step이나 사전 작업이 꼭 필요한 Step 등
   - 기본적으로 COMPLETED 상태를 가진 Step은 Job 재 시작 시 실행하지 않고 스킵한다.
   - allow-start-if-complete가 "true"로 설정된 step은 항상 실행한다.


### JobStep

1. 기본 개념
    - Job에 속하는 Step 중 외부의 Job을 포함하고 있는 Step
    - 외부의 Job이 실패하면 해당 Step이 실패하므로 결국 최종 기본 job도 실패한다.
    - 모든 메타데이터는 기본 Job과 외부 Job별로 각각 저장된다.
    - 커다란 시스템을 작은 모듈로 쪼개고 job의 흐름을 관리하고자 할 떄 사용할 수 있다.

2. Api 소개

StepBuilderFactory > StepBuilder > JobStepBuilder > JobStep

```java
public Step jobStep(JobLauncher jobLauncher) {
    return stepBuilderFactory.get("jobStep") // StepBuilder 를 생성하는 팩토리, Step 이름을 매개변수로 받음
        .job(Job) // JobStep 내에서 실행될 Job 설정 JobStepBuilder 반환
        .launcher(jobLauncher) // Job을 실행할 JobLauncher 설정
        .parametersExtractor(JobParametersExtractor) // Step의 ExecutionContext를 Job이 실행되는 데 필요한 JobParameters로 반환
        .build();
}
```


### Job and Flow

### FlowJob

1. 기본개념
    - Step을 순차적으로만 구성하는 것이 아닌 특정한 상태에 따라 흐름을 전환하도록 구성할 수 있으며 FlowJobBuilder에 의해 생성된다.
      - Step이 실패 하더라도 Job은 실패로 끝나지 않도록 해야하는 경우
      - Step이 성공 했을 때 다음에 실행해야 할 Step을 구분해서 실행 해야 하는 경우
      - 특정 Step은 전혀 실행되지 않게 구성해야 하는 경우
    - Flow와 Job의 흐름을 구성하는데만 관여하고 실제 비즈니스 로직은 Step에서 이루어진다.
    - 내부적으로 SimpleFlow 객체를 포함하고 있으며 Job실행시 호출한다.

2. SimpleJob : 순차적 흐름, FlowJob : 조건적 흐름

JobBuilderFactory > JobBuilder > JobFlowBuilder > FlowBuilder > FlowJob

```java
public Job batchJob() {
    return jobBuilderFactory.get("batchJob") 
        .start(Step) // Flow 시작하는 Step 설정
        .on(String pattern) // Step의 실행 결과로 돌려받는 종료상태 (ExitStatus)를 캐치하여 매칭하는 패턴, TransitionBuilder 반환
        .to(Step)  // 다음으로 이동할 Step 지정
        .stop() / fail() / end() / stopAndRestart() // Flow를 중지 / 실패 / 종료 하도록 Flow 종료
        .from(Step) // 이전 단계에서 정의한 Step의 Flow를 추가적으로 정의함
        .next(Step) // 다음으로 이동할 Step 지정
        .end() // build() 앞에 위치하면 FlowBuilder를 종료하고 SimpleFlow 객체 생성
        .build() // FlowJob 생성하고 flow 필드에 SimpleFlow 저장
}
```




















### 우아한 스프링 배치 - 유튜브 정리

https://www.youtube.com/watch?v=_nkJkWVH-mo

Job Repository? , Job Execution ?

배민에서는 주문, 포인트, 정산 → Batch 시스템 구성/개발

대상 : 실무에서 배치를 한 번이라도 사용해본 사람

강의는 Java Config 기반으로 진행

## 기본편

배치 애플리케이션

- 배치 처리는 컴퓨터에서 사람과 상호 작용 없이 이어지는 프로그램 (작업) 들의 실행이다

Web - 실시간 처리 / 상대적인 속도 / QA 용이성

Batch - 후속 처리 / 절대적인 속도 / QA 복잡성

### Spring Batch 와 Quartz

Quartz는 스케줄링 프레임워크 - 매 시간 / 마지막 주 금요일에 실행

Spring Batch의 보안제 역할이지 대체제가 아니다.

배치 애플리케이션이 필요한 상황 일정 주기로 실행되어야 할 때 실시간 처리가 어려운 대량의 데이터를 처리 할 때

(ex. 배민은 88개 넘음 집계, 통계, 조회가 많은 데이터를 캐시)

한 달에 한 번 실행이 된다는 의미는 한 달 동안 쌓인 모든 데이터가 대상이라는 의미

즉. 대용량 데이터 처리가 절대적인 요구 사항

스프링 배치에서는 모든 데이터를 메모리에 쌓지 않는 조회 방식이 기본 방식

- (DB기준) Paging 혹은 Cursor로 pageSize 만큼만 읽어 어고 chunkSize 만큼만 commit한다.

jpaRepository.findAll() 방식으로 진행하면 안 된다.

ChunkOrientedTasklet은 Tasklet의 구현체이다.

@JobSocpe, @StepScope, JobParameter Spring Batch는 외부에서 파라미터를 주입 받아 Batch 컴포넌트에서 사용 할 수 있다.

이를 JobParameter 라고 한다.

코드에는 컴파일시 null을 넣고 런타임에서는 scope를 넘긴다.

JobParameter 는 Long / String / Double / Date를 지원

Enum / LocalDate / LocalDateTime 은 지원이 안 된다.

보통 받은 뒤 형변환을 한다.

→ 아래처럼 형변환이 가능

→ 형변환을 위한 tasklet이 필요 없어진다.

@JobScope - Job 실행 시점에 Bean 생성

@StepScope - Step 실행 시점에 Bean 생성

Late Binding (늦은 할당)

→ 애플리케이션 실행 후에도 동적으로 reader / processor / writer bean 생성이 가능

SLP 연동을 많이해서 사용함 - validation 똑같고 parameter와 테이블 두 개만 다름

## 활용편

관리 도구들

Cron

Spring MVC + API Call

Spring Batch Admin (Deprecated)  → Spring Cloud Data Flow를 쓰라고 나와있긴 함

Quartz + Admin

CI Tools (Jenkins / teamcity 등) -->> 배치랑 잘 어울림

Integration (Slack, Email 등)

실행 이력 / 로그 관리 / Dashboard

다양한 실행 방법 (Rest API / 스케줄링 / 수동 실행)

계정 별 권한 관리

파이프라인

Web UI + Script 둘다 사용 가능

Plugin (Ansible, Github, Logentries) 등

Jenkins에서 배치 실행

java -jar Application.jar \
--job.name=job이름 \ -job.name은 스프링 환경 변수 job파라미터이름1=job파라미터값1 \
job파라미터이름2=job파라미터값2 \
Jenkins 공통 설정 관리

모든 Batch Job에서 중복되는 코드가 있음 1.8까지는 G1GC가 default가 아님

Global properties - Environment variables 에 등록함

무중단 배포 무한 jar 중지 못하는 상황 피하려고 함

배포랑 배치 jenkins는 따로 사용해야함 - 슈퍼 jenkins를 피하기 위해

기존에 실행되고 있는 batch jar를 종료하지 않고 교체할 수 있을까 → readlink 원본 자료를 찾아오는 것

여러 작업이 순차적으로 실행이 필요할때 Step으로 나누기 보다는 파이프라인을 우선 고려한다.

멱등성 연산을 여러번 적용하더라도 결과가 달라지지 않는 성질

애플리케이션 개발에서 멱등성이 깨지는 경우 > 제어할 수 없는 코드를 직접 짤때

어제 데이터를 다시 돌려야하면 어떡하지??? - 원하는 날짜를 입력 받아서 실행

(ex LocalDate.now())

젠킨스에서 어떻게 오늘 일자를 yyyy-MM-dd 를 넣을 수 있을까

### 테스트 코드

QA 시나리오부터가 힘들다

해당 option이 없으면 해당 테스트가 실행 되지 않는다.

전체는 1200개가 넘는다. Spring Batch 370

느려짐 범인은 @ConditionalOnProperty

@ConditionalOnProperty 제거

모든 Config를 Loading 한 뒤 , 원하는 배치 Job Bean을 실행한다.

→ Context 하나 가지고 모든 테스트를 돌릴 수 있게 된다.

JPA & Spring Batch에 이슈 될 만한 것 JPA N+1

@OneToMay 관계에서 하위 엔티티 Lazy Loading으로 가져올때마다 조회 쿼리가 추가로 발생

→ join fetch로 해결

dafault_batch_fetch_size: 1000

이 옵션은 JpaPagingItemReader 작동하지 않음 - HibernateCursorItemReader는 가능

JpaRepository에서도 사용할 수 있는 정상 기능

JPA Persist Writer 모든 item에 대해 merge 수행 처음 데이터가 save 될 때도 update 쿼리가 항상 실행 됨

JpaItemWriter - save or update 필요할 때

JpaItemPersistWriter - save만 있을 때

The Definitive Guide to Spring Batch
