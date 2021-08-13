# Spring Batch


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
