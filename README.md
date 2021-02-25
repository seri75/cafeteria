# cafeteria
# Table of contents

- [음료주문](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [API Gateway](#API-GATEWAY)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출--시간적-디커플링--장애격리--최종-eventual-일관성-테스트)
    - [CQRS / Meterialized View](#CQRS--Meterialized-View)
  - [운영](#운영)
    - [Liveness / Readiness 설정](#Liveness--Readiness-설정)
    - [CI/CD 설정](#cicd-설정)
    - [Self Healing](#Self-Healing)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출--서킷-브레이킹--장애격리)
    - [Persistence Volum Claim](#Persistence-Volum-Claim)

# 서비스 시나리오

음료주문

기능적 요구사항
1. 고객이 음료를 주문한다
1. 고객이 결제한다
1. 결제가 되면 판매 금액을 월별 누적 금액에 전달한다
1. 고객이 월별 구매한 누적 금액을 조회 한다 
1. 결제가 되면 주문 내역이 바리스타에게 전달된다
1. 바리스타는 주문내역을 확인하여 음료를 접수하고 제조한다.
1. 고객이 주문을 취소할 수 있다
1. 주문이 취소되면 음료를 취소한다.
1. 음료가 취소되면 결제를 취소한다.
1. 고객이 주문상태를 중간중간 조회한다.
1. 주문상태가 바뀔 때 마다 카톡으로 알림을 보낸다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 주문건은 판매 금액에 누적되지 않는다. Sync호출
1. 장애격리
    1. 결제가 취소처리가 완료 되면 누적된 판매 금액이 취소된다Async (event-driven), Eventual Consistency
    1. 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다.  Circuit breaker, fallback
1. 성능
    1. 고객이 판매된 누적 금액을 세일즈페이지(프론트엔드)에서 확인할 수 있어야 한다. CQRS
    1. 주문상태가 바뀔때마다 카톡 등으로 알림을 줄 수 있어야 한다.  Event driven

# 분석설계

1. Event Storming 모델
![image](https://user-images.githubusercontent.com/74699168/108949998-ca90c280-76a8-11eb-9f84-f3e5a8dad55a.png)
1. 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/74699168/108950156-1b082000-76a9-11eb-8e37-1cfdaed767b1.png)



## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다

```

package cafeteria;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Sale_table")
public class Sale {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String phoneNumber;
    private String yyyymm;
    private Integer sumAmt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getYyyymm() {
        return yyyymm;
    }

    public void setYyyymm(String yyyymm) {
        this.yyyymm = yyyymm;
    }
    public Integer getSumAmt() {
        return sumAmt;
    }

    public void setSumAmt(Integer sumAmt) {
        this.sumAmt = sumAmt;
    }

}


```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package cafeteria;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface SaleRepository extends PagingAndSortingRepository<Sale, Long>{

	public List<Sale> findByPhoneNumberAndYyyymm(String phoneNumber, String yyyymm);

}

```
- 적용 후 REST API 의 테스트
```
# order 서비스의 주문처리
root@siege-5b99b44c9c-f2ftw:/# http http://order:8080/orders phoneNumber="01012345678" productName="coffee" qty=2 amt=7000
HTTP/1.1 201 
Content-Type: application/json;charset=UTF-8
Date: Thu, 25 Feb 2021 04:28:24 GMT
Location: http://order:8080/orders/11
Transfer-Encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://order:8080/orders/11"
        },
        "self": {
            "href": "http://order:8080/orders/11"
        }
    },
    "amt": 7000,
    "createTime": "2021-02-25T04:28:24.716+0000",
    "phoneNumber": "01012345678",
    "productName": "coffee",
    "qty": 2,
    "status": "Ordered"
}

# payment 등록
root@siege-5b99b44c9c-f2ftw:/# http http://payment:8080/payments/search/findByOrderId?orderId=11
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Thu, 25 Feb 2021 04:46:28 GMT
Transfer-Encoding: chunked

{
    "_embedded": {
        "payments": [
            {
                "_links": {
                    "payment": {
                        "href": "http://payment:8080/payments/6"
                    },
                    "self": {
                        "href": "http://payment:8080/payments/6"
                    }
                },
                "amt": 7000,
                "createTime": "2021-02-25T04:28:24.722+0000",
                "orderId": 11,
                "phoneNumber": "01012345678",
                "status": "PaymentApproved"
            }
        ]
    },
    "_links": {
        "self": {
            "href": "http://payment:8080/payments/search/findByOrderId?orderId=11"
        }
    }
}

# sale 서비스의 등록처리
root@siege-5b99b44c9c-f2ftw:/# http http://sale:8080/sales
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Thu, 25 Feb 2021 04:47:31 GMT
Transfer-Encoding: chunked

{
    "_embedded": {
        "sales": [
            {
                "_links": {
                    "sale": {
                        "href": "http://sale:8080/sales/1"
                    },
                    "self": {
                        "href": "http://sale:8080/sales/1"
                    }
                },
                "phoneNumber": "01012345678",
                "sumAmt": 21000,
                "yyyymm": "202102"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://sale:8080/profile/sales"
        },
        "search": {
            "href": "http://sale:8080/sales/search"
        },
        "self": {
            "href": "http://sale:8080/sales{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}

# salepage 서비스의 조회
root@siege-5b99b44c9c-f2ftw:/# http http://sale:8080/salePages
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Thu, 25 Feb 2021 04:48:41 GMT
Transfer-Encoding: chunked

{
    "_embedded": {
        "salePages": [
            {
                "_links": {
                    "salePage": {
                        "href": "http://sale:8080/salePages/2"
                    },
                    "self": {
                        "href": "http://sale:8080/salePages/2"
                    }
                },
                "amt": 7000,
                "orderId": 9,
                "phoneNumber": "01012345678",
                "productName": "coffee",
                "sumAmt": 7000,
                "yyyymm": "202102"
            },
            {
                "_links": {
                    "salePage": {
                        "href": "http://sale:8080/salePages/3"
                    },
                    "self": {
                        "href": "http://sale:8080/salePages/3"
                    }
                },
                "amt": 7000,
                "orderId": 10,
                "phoneNumber": "01012345678",
                "productName": "coffee",
                "sumAmt": 14000,
                "yyyymm": "202102"
            },
            {
                "_links": {
                    "salePage": {
                        "href": "http://sale:8080/salePages/4"
                    },
                    "self": {
                        "href": "http://sale:8080/salePages/4"
                    }
                },
                "amt": 7000,
                "orderId": 11,
                "phoneNumber": "01012345678",
                "productName": "coffee",
                "sumAmt": 21000,
                "yyyymm": "202102"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://sale:8080/profile/salePages"
        },
        "search": {
            "href": "http://sale:8080/salePages/search"
        },
        "self": {
            "href": "http://sale:8080/salePages"
        }
    }
}
```

## API Gateway
API Gateway를 통하여 동일 진입점으로 진입하여 각 마이크로 서비스를 접근할 수 있다.
외부에서 접근을 위하여 Gateway의 Service는 LoadBalancer Type으로 생성했다.

```
# application.yml

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/orders/** 
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
        - id: drink
          uri: http://drink:8080
          predicates:
            - Path=/drinks/**,/orderinfos/**
        - id: customercenter
          uri: http://customercenter:8080
          predicates:
            - Path= /mypages/**
        - id: sale
          uri: http://sale:8080
          predicates:
            - Path= /sales/**,/salePages/**


# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: gateway
  labels:
    app: gateway
spec:
  type: LoadBalancer
  ports:
    - port: 8080
      targetPort: 8080
  selector:
    app: gateway
    
# kubectl get svc 
NAME             TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
customercenter   ClusterIP      10.100.193.90   <none>        8080/TCP         4h46m
drink            ClusterIP      10.100.42.138   <none>        8080/TCP         4h47m
gateway          LoadBalancer   10.100.182.2    <pending>     8080:30223/TCP   5h3m
order            ClusterIP      10.100.206.47   <none>        8080/TCP         5h4m
payment          ClusterIP      10.100.30.75    <none>        8080/TCP         5h3m
sale             ClusterIP      10.100.192.26   <none>        8080/TCP         4h46m 

```

## 폴리글랏 퍼시스턴스

판매량 조회는 다른 서비스의 정보도 같이 조회 서비스를 제공하는 특성을 가지고 있고, in memory DB를 사용하기 위해 HSQL을 적용하였다.
HSQL 적용을 위해 데이터베이스 제품 설정을 pom.xml에 반영하였다.
```
# pom.yml
		<!-- HSQL -->
 		<dependency>
    		<groupId>org.hsqldb</groupId>
   			<artifactId>hsqldb</artifactId>
   			<scope>runtime</scope>
		</dependency>

```

## 동기식 호출 과 Fallback 처리

결제(payment)->판매(sale) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 판매 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```

# (payment) SaleService.java

package cafeteria.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="sale", url="${feign.client.sale.url}")
public interface SaleService {

        @PutMapping("/sumtAmt")
         public void sumAmt(@RequestBody Sale sale);
}
```

- 주문을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
# Payment.java (Entity)

    @PostPersist
    public void onPostPersist(){
        :
        
        PaymentApplication.applicationContext.getBean(SaleService.class).sumAmt(sale);
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 판매 시스템이 장애가 나면 주문도 못받는다는 것을 확인:

```
# 판매 (sale) 서비스를 잠시 내려놓음
$ kubectl delete deploy sale
deployment.apps "sale" deleted

#주문처리
root@siege-5b99b44c9c-f2ftw:/# http http://order:8080/orders phoneNumber="01012345678" productName="tea" qty=1 amt=5000
HTTP/1.1 500 
Connection: close
Content-Type: application/json;charset=UTF-8
Date: Thu, 25 Feb 2021 05:49:15 GMT
Transfer-Encoding: chunked

{
    "error": "Internal Server Error",
    "message": "Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Error while committing the transaction",
    "path": "/orders",
    "status": 500,
    "timestamp": "2021-02-25T05:49:15.697+0000"
}


#판매서비스 재기동
$ kubectl apply -f deployment.yml
deployment.apps/sale created

#주문처리

root@siege-5b99b44c9c-f2ftw:/# http http://order:8080/orders phoneNumber="01012345678" productName="tea" qty=1 amt=5000
HTTP/1.1 201 
Content-Type: application/json;charset=UTF-8
Date: Thu, 25 Feb 2021 05:51:58 GMT
Location: http://order:8080/orders/14
Transfer-Encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://order:8080/orders/14"
        },
        "self": {
            "href": "http://order:8080/orders/14"
        }
    },
    "amt": 5000,
    "createTime": "2021-02-25T05:51:57.166+0000",
    "phoneNumber": "01012345678",
    "productName": "tea",
    "qty": 1,
    "status": "Ordered"
}
```

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

결제취소가 이루어진 후에 판매시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 판매 시스템의 처리를 위하여 결제취소처리가 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 취소이력에 기록을 남긴 후에 곧바로 결제취소가 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package cafeteria;

@Entity
@Table(name="Payment")
public class Payment {

 :
    @PostUpdate
    public void onPostUpdate(){
        PaymentCanceled paymentCanceled = new PaymentCanceled();
        BeanUtils.copyProperties(this, paymentCanceled);
        paymentCanceled.publishAfterCommit();


    }

}
```
- 판매 서비스에서는 PaymentCanceled 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package cafeteria;

:

@StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_2(@Payload PaymentCanceled paymentCanceled) {
        try {
            if (paymentCanceled.isMe()) {
            
                String yyyymm = paymentCanceled.getTimestamp().substring(0, 6);
                List<Sale> sales = saleRepository.findByPhoneNumberAndYyyymm(paymentCanceled.getPhoneNumber(), yyyymm);
                
                if(sales.size() !=  1) throw new RuntimeException("There is not exacted[" + yyyymm + " / " + paymentCanceled.getPhoneNumber() + "]");
                Sale sale = sales.get(0);
                sale.setSumAmt(sale.getSumAmt() - paymentCanceled.getAmt());
            
                saleRepository.save(sale);
                
               :
            
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

```

- 결제 취소처리 후  비 동기식으로 처리하여 판매금액 감소를 확인할수 있다

```
#판매금액 
root@siege-5b99b44c9c-f2ftw:/# http http://sale:8080/sales
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Thu, 25 Feb 2021 06:14:35 GMT
Transfer-Encoding: chunked

{
    "_embedded": {
        "sales": [
            {
                "_links": {
                    "sale": {
                        "href": "http://sale:8080/sales/2"
                    },
                    "self": {
                        "href": "http://sale:8080/sales/2"
                    }
                },
                "phoneNumber": "01012345678",
                "sumAmt": 10000,
                "yyyymm": "202102"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://sale:8080/profile/sales"
        },
        "search": {
            "href": "http://sale:8080/sales/search"
        },
        "self": {
            "href": "http://sale:8080/sales{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}

#주문취소 
root@siege-5b99b44c9c-f2ftw:/# http PATCH http://order:8080/orders/15 status="OrderCanceled"
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Date: Thu, 25 Feb 2021 06:14:51 GMT
Transfer-Encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://order:8080/orders/15"
        },
        "self": {
            "href": "http://order:8080/orders/15"
        }
    },
    "amt": 5000,
    "createTime": "2021-02-25T06:14:23.427+0000",
    "phoneNumber": "01012345678",
    "productName": "coffee",
    "qty": 2,
    "status": "OrderCanceled"
}


#판매금액 확인

root@siege-5b99b44c9c-f2ftw:/# http http://sale:8080/sales
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Thu, 25 Feb 2021 06:14:54 GMT
Transfer-Encoding: chunked

{
    "_embedded": {
        "sales": [
            {
                "_links": {
                    "sale": {
                        "href": "http://sale:8080/sales/2"
                    },
                    "self": {
                        "href": "http://sale:8080/sales/2"
                    }
                },
                "phoneNumber": "01012345678",
                "sumAmt": 5000,
                "yyyymm": "202102"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://sale:8080/profile/sales"
        },
        "search": {
            "href": "http://sale:8080/sales/search"
        },
        "self": {
            "href": "http://sale:8080/sales{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}

```

## CQRS / Meterialized View
Sale의 SalePage를 구현하여 Order 서비스, Payment 서비스, Sale 서비스의 데이터를 Composite서비스나 DB Join없이 조회할 수 있다.
```
root@siege-5b99b44c9c-f2ftw:/# http http://sale:8080/salePages
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Thu, 25 Feb 2021 06:29:37 GMT
Transfer-Encoding: chunked

{
    "_embedded": {
        "salePages": [
            {
                "_links": {
                    "salePage": {
                        "href": "http://sale:8080/salePages/2"
                    },
                    "self": {
                        "href": "http://sale:8080/salePages/2"
                    }
                },
                "amt": 5000,
                "orderId": 17,
                "phoneNumber": "01012345678",
                "productName": "coffee",
                "sumAmt": 5000,
                "yyyymm": "202102"
            },
            {
                "_links": {
                    "salePage": {
                        "href": "http://sale:8080/salePages/4"
                    },
                    "self": {
                        "href": "http://sale:8080/salePages/4"
                    }
                },
                "amt": 7000,
                "orderId": 18,
                "phoneNumber": "01012345555",
                "productName": "tea",
                "sumAmt": 7000,
                "yyyymm": "202102"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://sale:8080/profile/salePages"
        },
        "search": {
            "href": "http://sale:8080/salePages/search"
        },
        "self": {
            "href": "http://sale:8080/salePages"
        }
    }
}
```

# 운영

## Self Healing(livenessProbe)
livenessProbe를 설정하여 문제가 있을 경우 스스로 재기동 되도록 한다.
```	  
# liveness Probe호출 주소를 변경
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sale
  labels:
    app: sale
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sale
  template:
    metadata:
      labels:
        app: sale
    spec:
      containers:
        - name: sale
          image: 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser01-sale:v4
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health2'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
  
# kubectl get pods -w
NAME                              READY   STATUS    RESTARTS   AGE
customercenter-849b884dd8-j8cbn   1/1     Running   0          5h12m
drink-7c8b47d46b-psdgc            1/1     Running   0          5h13m
gateway-55d7bf77f5-9g49x          1/1     Running   0          5h11m
order-846646bf8c-dz8hd            1/1     Running   0          5h20m
payment-79d858d957-fbf5z          1/1     Running   0          4h32m
sale-789f85978f-sjnh4             0/1     Running   0          13s
siege-5b99b44c9c-f2ftw            1/1     Running   0          5h4m
sale-789f85978f-sjnh4             0/1     Running   1          1m40s
sale-789f85978f-sjnh4             0/1     Running   2          62s
sale-789f85978f-sjnh4             1/1     Running   2          13s

```

## CI/CD 설정

각 구현체들은 하나의 source repository 에 구성되었고, kubectl명령을 통하여 수동으로 배포 하였다.

mvn clean install
docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser01-sale:v4 .
docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser01-sale:v4
kubectl apply -f kubernetes/deployment.yml


## Persistence Volum Claim
서비스의 log를 persistence volum을 사용하여 재기동후에도 남아 있을 수 있도록 하였다.
```

# application.yml

:
server:
  tomcat:
    accesslog:
      enabled: true
      pattern:  '%h %l %u %t "%r" %s %bbyte %Dms'
    basedir: /logs/sale

logging:
  path: /logs/sale
  file:
    max-history: 30
  level:
    org.springframework.cloud: debug

# deployment.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: sale
  labels:
    app: sale
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sale
  template:
    metadata:
      labels:
        app: sale
    spec:
      containers:
        - name: sale
          image: 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser01-sale:v4
          ports:
            - containerPort: 8080
          :
          volumeMounts:
          - name: logs 
            mountPath: /logs
      volumes:
      - name: logs 
        persistentVolumeClaim:
          claimName: sale-logs

# pvc.yaml

apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: sale-logs 
spec:
  accessModes:
  - ReadWriteOnce
    #  storageClassName: default
  resources:
    requests:
      storage: 1Gi
```
sale deployment를 삭제하고 재기동해도 log는 삭제되지 않는다.

```
$ kubectl delete -f sale/kubernetes/deployment.yml
deployment.apps "sale" deleted

$ kubectl apply -f sale/kubernetes/deployment.yml
deployment.apps/sale created

$ kubectl exec -it sale-789f85978f-sjnh4 -- /bin/sh
/ # ls -l /logs/sale/
total 5568
drwxr-xr-x    2 root     root          4096 Feb 24 00:00 logs
-rw-r--r--    1 root     root       4626352 Feb 25 14:34 spring.log
-rw-r--r--    1 root     root        245383 Feb 25 13:48 spring.log.2021-02-25.0.gz
drwxr-xr-x    3 root     root          4096 Feb 24 14:34 work

```
