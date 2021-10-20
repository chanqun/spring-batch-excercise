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
1. @Configuration 선언
    - 하나의 배치 Job을 정의하고 빈 설정

2. JobBuilderFactory
    - Job 을 생성하는 빌더 팩토리
3. StepBuilderFactory
    - Step을 생성하는 빌더 팩토리
4. Job
    - Job 생성
5. Step
    - Step 생성
6. tasklet
    - step 안에서 단일 태스크로 수행되는 로직 구현
7. Job 구동 -> Step을 실행 -> Tasklet을 실행
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




### 스프링 배치 기본 구조

- Job은 JobLauncher에 의해 실행
- Job은 배치의 실행 단위를 의미
- Job은 N개읜 Step을 실행할 수 있으며, 흐름을 관리할 수 있다.
    - 예를 들면, A Step 실행 후 조건에 따라 B Step 또는 C Step 실행 설정
    
- Step의 실행 단위는 크게 2가지로 나눌 수 있다.
    1. Chunk 기반: 하나의 큰 덩어리를 n개씩 나눠서 실행
    2. Task 기반: 하나의 작업 기반으로 실행
- Chunk 기반 Step은 ItemReader, ItemProcessor, ItemWriter가 있다.
    - 여기서 Item은 배치 처리 대상 객체를 의미
- ItemReader는 배치 처리 대상 객체를 읽어 ItemProcessor 또는 ItemWriter에게 전달
    - 예를 들면, 파일 또는 DB에서 데이터를 읽는다.
- ItemProcessor는 input 객체를 Output 객체로 filtering 또는 processing 해 ItemWriter 에게 전달
    - 예를 들면, ItemReader에서 읽은 데이터를 수정 또는 ItemWriter 대상인지 filtering
    - ItemProcessor는 Optional
    - ItemProcessor가 하는 일을 ItemReader 또는 ItemWriter가 대신 할 수 있다.
- ItemWriter는 배치 처리 대상 객체를 처리한다.
    - 예를 들면, DB update를 하거나, 처리 대상 사용자에게 알림을 보낸다.
    
### 스프링 배치 테이블 구조와 이해



- JobInstance : BATCH_JOB_INSTANCE 와 매핑
- JobExecution : BACH_JOB_EXECUTION
- JobParameters : BATCH_JOB_EXECUTION_PARAMS
- ExecutionContext : BATCH_JOB_EXECUTION_CONTEXT

- JobInstance의 생성 기준은 JobParamters 중복 여부에 따라 생성된다.
- 다른 parameter로 Job이 실행되면, JobInstance가 생성된다.
- 같은 parameter로 Job이 실행되면, 이미 생성된 JobInstance가 실행된다.
- JobExecution은 항상 새롭게 생성된다.
- 예를 들어
    - 처음 Job 실행 시 date parameter가 1월1일로 실행 됐다면, 1번 JobInstance가 생성된다.
    - 다음 Job 실행 시 date parameter가 1월2일로 실행 됐다면, 2번 JobInstance가 생성된다.
    - 다음 Job 실행 시 date parameter가 1월2일로 실행 됐다면, 2번 JobInstance가 재 실행된다.
        - 이때 Job이 재실행 대상이 아닌 경우 에러가 발생한다.
    - Parameter가 없는 Job을 항상 새로운 JobInstance가 실행되도록 RunIdIncrementer가 제공된다.
    



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



배치 애플리케이션이 필요한 상황
일정 주기로 실행되어야 할 때 실시간 처리가 어려운 대량의 데이터를 처리 할 때

(ex. 배민은 88개 넘음 집계, 통계, 조회가 많은 데이터를 캐시)

한 달에 한 번 실행이 된다는 의미는 한 달 동안 쌓인 모든 데이터가 대상이라는 의미

즉. 대용량 데이터 처리가 절대적인 요구 사항



스프링 배치에서는 모든 데이터를 메모리에 쌓지 않는 조회 방식이 기본 방식

- (DB기준) Paging 혹은 Cursor로 pageSize 만큼만 읽어 어고 chunkSize 만큼만 commit한다.

jpaRepository.findAll() 방식으로 진행하면 안 된다.



ChunkOrientedTasklet은 Tasklet의 구현체이다.



@JobSocpe, @StepScope, JobParameter
Spring Batch는 외부에서 파라미터를 주입 받아 Batch 컴포넌트에서 사용 할 수 있다.

이를 JobParameter 라고 한다.

코드에는 컴파일시 null을 넣고 런타임에서는 scope를 넘긴다.

JobParameter 는 Long / String / Double / Date를 지원

Enum / LocalDate / LocalDateTime 은 지원이 안 된다.

보통  받은 뒤 형변환을 한다.

→ 아래처럼 형변환이 가능

→ 형변환을 위한 tasklet이 필요 없어진다.



@JobScope - Job 실행 시점에 Bean 생성

@StepScope - Step 실행 시점에 Bean 생성

Late Binding (늦은 할당)

→ 애플리케이션 실행 후에도 동적으로 reader / processor / writer  bean 생성이 가능

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
--job.name=job이름 \                     -job.name은 스프링 환경 변수
job파라미터이름1=job파라미터값1 \
job파라미터이름2=job파라미터값2 \
Jenkins 공통 설정 관리

모든 Batch Job에서 중복되는 코드가 있음 1.8까지는 G1GC가 default가 아님

Global properties - Environment variables 에 등록함



무중단 배포
무한 jar 중지 못하는 상황 피하려고 함

배포랑 배치 jenkins는 따로 사용해야함 - 슈퍼 jenkins를 피하기 위해

기존에 실행되고 있는 batch jar를 종료하지 않고 교체할 수 있을까 → readlink 원본 자료를 찾아오는 것

여러 작업이 순차적으로 실행이 필요할때 Step으로 나누기 보다는 파이프라인을 우선 고려한다.



멱등성
연산을 여러번 적용하더라도 결과가 달라지지 않는 성질

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



JPA & Spring Batch에 이슈 될 만한 것
JPA N+1

@OneToMay 관계에서 하위 엔티티 Lazy Loading으로 가져올때마다 조회 쿼리가 추가로 발생

→ join fetch로 해결

dafault_batch_fetch_size: 1000

이 옵션은 JpaPagingItemReader 작동하지 않음 - HibernateCursorItemReader는 가능

JpaRepository에서도 사용할 수 있는 정상 기능



JPA Persist Writer 모든 item에 대해 merge 수행 처음 데이터가 save 될 때도 update 쿼리가 항상 실행 됨

JpaItemWriter - save or update 필요할 때

JpaItemPersistWriter - save만 있을 때



The Definitive Guide to Spring Batch
