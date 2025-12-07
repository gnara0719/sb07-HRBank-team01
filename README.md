# HR BANK
**Batch로 데이터를 관리하는 Open EMS**


<img width="1872" height="905" alt="image" src="https://github.com/user-attachments/assets/2fab1d18-2e9a-4e51-b646-f52b6e195d46" />




🏢 기업의 핵심 자산, 인적 자원을 체계적으로 관리하세요!

HR Bank는 인사 데이터를 안전하고 효율적으로 관리할 수 있도록 설계된 Open EMS(Enterprise Management System)입니다. 대량의 데이터를 안정적으로 처리할 수 있는 Batch 시스템을 기반으로 부서 및 직원 정보를 체계적으로 운영할 수 있으며, 백업 자동화, 이력 관리, 대시보드 제공을 통해 기업 인사 관리를 더욱 효과적으로 지원합니다

***프로젝트 기간 2025.11.27 ~ 2025.12.08***

----
**기술스택**


<img width="516" height="516" alt="image" src="https://github.com/user-attachments/assets/236e9997-e195-496a-98dd-a1e74ceb5ae9" />



---
## 🧑‍💻 팀원 소개 및 담당 역할

| 이름 | 역할 | GitHub 프로필 |
| :--- | :--- | :--- |
| **신제원** | 팀장 / 부서 도메인 개발 | [신제원](https://github.com/sinjawon) |
| **이정훈** | 백업 도메인 개발 | [이정훈](https://github.com/mij9929) |
| **이형일** | 직원 도메인 개발 | [이형일](https://github.com/hilee93) |
| **최현석** | CSV 대용량 파일 처리 및 로그 기록 | [최현석](https://github.com/chs0617) |
| **박재완** | 파일 도메인 개발 | [박재완](https://github.com/gnara0719) |
| **김유미** | 직원이력 도메인 개발 | [김유미](https://github.com/yuuum0214) |

## 주요 기능

**부서 관리**
- 부서 정보 등록
  - 이름, 설명, 설립일을 입력하고 부서를 등록할 수 있음
  - 이름은 중복될 수 없음
- 부서 정보 수정
  - 이름, 설명, 설립일을 수정할 수 있음
- 부서 정보 삭제
  - 소속된 직원이 없는 부서는 삭제할 수 있음
- 부서 목록 조회
  - 이름 또는 설명으로 검색하여 부서 목록을 조회할 수 있음
  - 이름, 설립일로 정렬하여 부서 목록을 조회할 수 있음

**직원정보 관리**
- 직원 등록
  - 이름, 이메일, 부서, 직함, 입사일, 프로필 이미지를 통해 직원을 등록할 수 있음
  - 이메일은 중복될 수 없음
  - 프로필 이미지는 선택적으로 등록할 수 있음
- 직원 정보 수정
  - 사원번호를 제외한 모든 정보는 수정 가능
  - 이메일은 다른 직원과 중복 불가
- 직원 정보 삭제
  - 직원을 삭제하면 프로필 이미지도 삭제
  - 퇴사는 삭제가 아닌 수정으로 처리
- 직원 목록 조회
  - 이름 또는 이메일, 부서, 직함, 사원번호, 입사일, 상태로 직원 목록 조회할 수 있음
  - 이름 또는 이메일, 부서, 직함, 사원번호는 부분 일치 조건
  - 입사일 범위 조건
  - 상태일 완전 일치
  -  이름, 입사일, 사원번호로 정렬하여 직원 목록을 조회할 수 있음
- 직원 정보 상세 조회
  - Id로 직원의 상세 정보를 조회할 수 있음

**파일 관리**
- 파일 저장
  - 파일명, 파일형식, 크기 메타 정보를 저장하고, 실제 파일은 로컬에 저장
- 파일 다운로드
  - 메타 정보의 ID를 통해 파일을 다운로드 할 수 있음

**직원 정보 수정 이력 관리**
- 직원 이력 등록
  - 직원 추가, 직원 정보 수정 화면에서 입력하는 메모 정보를 통해 등록
  - IP 주소는 서버에서 자동으로 추출 후 저장
- 이력 목록 조회
  - 직원 사번, 메모, IP주소, 시간, 유형으로 이력 목록을 조회할 수 있음
  - 직원 사번, 메모, IP주소는 부분 일치
  - 시간은 범위
  - 유형은 완전 일치
  - IP주소, 시간으로 정렬하여 직원 정보 수정 이력 관리를 수정할 수 있음
- 이력 변경 내용 상세 조회
  - id로 이력의 변경 상세 내용을 조회할 수 있음

**데이터 백업 관리**
- 데이터 백업 생성
  - 데이터 필요 여부를 판단하고, 새로 수정된 직원 이력이 없다면 건너뜀 상태
  - 백업 필요 시, 작업자의 IP 주소를 받고, 진행중으로 설정
  - 백업 성공 시, 완료 상태로 백업을 생성하고 csv 파일 저장
  - 백업 실패 시, 실패 상태로 백업을 생성하고 log 파일 저장
- 배치에 의한 데이터 백업
  - 서버는 백업 생성 프로세스를 주기마다 자동으로 반복
  - 작업자는 system
- 데이터 백업 이력 목록 조회
  - 작업자, 시작시간, 상태로 이력 목록을 조회할 수 있음
  - 작업자는 부분 일치
  - 시작시간은 범위
  - 상태는 완전 일치
  - 시작시간, 종료시간, 상태로 정렬하여 조회할 수 있음

 **대시보드**
 - 대시보드는 다음과 같은 정보로 구성
   - 총 직원 수
   - 최근 일주일 수정 이력 건수
   - 이번달 입사자 수
   - 마지막 백업 시간
   - 최근 1년 월별 직원수 변동 추이
   - 부서별 직원 

## END POINT
## 🚀 API Endpoints (주요 기능 목록)

| 구분 | HTTP Method | Endpoint | 설명 |
| :--- | :--- | :--- | :--- |
| **직원 관리** | `GET` | `/api/employees` | 필터 및 커서 기반 **직원 목록 조회** |
| **직원 관리** | `POST` | `/api/employees` | **새로운 직원 등록** (프로필 이미지 포함) |
| **직원 관리** | `GET` | `/api/employees/{id}` | 특정 **직원 상세 정보 조회** |
| **직원 관리** | `PATCH` | `/api/employees/{id}` | 특정 **직원 정보 수정** (프로필 이미지 포함) |
| **직원 관리** | `DELETE` | `/api/employees/{id}` | 특정 **직원 삭제** |
| **직원 관리** | `GET` | `/api/employees/stats/trend` | 기간별 **직원 증감 추이** 조회 |
| **직원 관리** | `GET` | `/api/employees/count` | 상태 및 기간 조건에 따른 **직원 수** 조회 |
| **직원 관리** | `GET` | `/api/employees/stats/distribution` | 상태 및 기간 조건에 따른 **직원 수** 조회 |
| **부서 관리** | `GET` | `/api/departments` | 커서 기반 **부서 목록 조회** |
| **부서 관리** | `POST` | `/api/departments` | **새로운 부서 등록** |
| **부서 관리** | `GET` | `/api/departments/{id}` | 특정 **부서 단건 조회** |
| **부서 관리** | `PATCH` | `/api/departments/{id}` | 특정 **부서 정보 수정** |
| **부서 관리** | `DELETE` | `/api/departments/{id}` | **부서 삭제** (소속 직원 있을 시 불가) |
| **백업 관리** | `POST` | `/api/backups` | **새로운 백업 작업 생성** |
| **백업 관리** | `GET` | `/api/backups` | 커서 기반 **백업 목록 조회** |
| **백업 관리** | `GET` | `/api/backups/latest` | **최신 백업** 조회 |
| **파일 관리** | `GET` | `/api/files/{id}/download` | 파일 ID로 **파일 다운로드** |
| **직원 이력 관리** | `GET` | `/api/change-logs` | 필터 조건으로 **직원 변경 이력 검색** |
| **직원 이력 관리** | `GET` | `/api/change-logs/{id}/diffs` | 특정 이력 ID의 **세부 변경 내용** 조회 |
| **직원 이력 관리** | `GET` | `/api/change-logs/count` | 전체 **변경 이력 수** 조회 |

--- 

## 트리 구조 ##
```
─sb07_hrbank_team01
    ├─backup
    │  ├─controller
    │  ├─dto
    │  │  ├─request
    │  │  └─response
    │  ├─entity
    │  ├─mapper
    │  ├─repository
    │  ├─Scheduler
    │  └─service
    ├─base
    ├─common
    │  ├─aop
    │  ├─api
    │  ├─config
    │  ├─doc
    │  ├─dto
    │  │  └─response
    │  ├─exception
    │  │  └─dto
    │  ├─interceptor
    │  ├─mapper
    │  └─util
    ├─department
    │  ├─controller
    │  ├─entity
    │  ├─mapper
    │  ├─repository
    │  ├─request
    │  ├─response
    │  └─service
    ├─employee
    │  ├─controller
    │  ├─dto
    │  │  ├─request
    │  │  └─response
    │  ├─entity
    │  ├─mapper
    │  ├─repository
    │  └─service
    ├─file
    │  ├─controller
    │  ├─dto
    │  ├─entity
    │  ├─mapper
    │  ├─repository
    │  ├─service
    │  └─storage
    └─history
        ├─controller
        ├─dto
        │  ├─requestDto
        │  └─responseDto
        ├─entity
        ├─repository
        ├─service
        └─utils
```

