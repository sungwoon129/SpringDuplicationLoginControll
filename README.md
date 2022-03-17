# SpringDuplicationLoginControll
스프링부트 중복 로그인 방지

1. ConcurrentHashMap

ConcurrentHashMap은 일반 hashmap과 달리 멀티쓰레드 작업환경에서 Thread safe한 특징을 가짐.
즉, 중복로그인을 처리할 때 여러 클라이언트에서 요청을 보내면 여러개의 쓰레드에서 요청을 처리할텐데, 어떤 쓰레드에서 해당 변수에 접근하더라도 동일한 값을 가지고 있어야함.

ConcurrentHashMap은 HashTable과 달리 읽기 작업에 대해서는 내부 메서드에 synchronized가 선언되어 있지 않음. 여러 쓰레드에서 동시에 접근하여 값을 읽을 수 있다는 뜻.
이러한 특징은 멀티쓰레드 환경에서 HashTable보다 좋은 성능을 보임. HashTable은 모든 메서드에 synchronized키워드가 선언되어 있어 쓰레드 작업간의 순서를 보장하기 위해 특정
쓰레드에서 작업을 처리할 때 lock이 걸리므로 여러 쓰레드가 동시에 해당 변수에 접근할 경우 속도가 느려질 수 있고 심한 경우 타임아웃 발생가능성 존재.

2. synchronized static

쓰레들간의 작업을 동기적으로 처리하기 위해 synchronized 키워드 선언. 
메소드에 붙은 static은 모든 쓰레드에서 로그인 요청을 할 때마다 해당 메서드를 참조할텐데, 그때마다 클래스 객체를 생성해서 작업할 경우 여러 쓰레드에서
동시에 접근할 경우, 내부에서 값이 변하는 변수들이 안전하지 못할 수 있다. 그래서 static을 선언하여 메서드를 쓰레드간에 공유할 수 있도록 한다.
static이 없다면 객체별로 잠금처리되므로, 컨트롤러에서 매번 다른 객체를 생성하게되면 접근이 가능해서 동시에 해당 메서드를 참조할 수 있게되므로, 쓰레드 동기화와 순차적인 처리가
보장되어야 하는 중복로그인 기능에는 적합하지 않다.

=> synchronized static 2개의 키워드를 조합하면 SessionConfig 객체 생성없이 해당 메서드를 호출하므로(단 하나의 메서드이므로) 모든 쓰레드에서 동시에 실행되지 않는다.

3. @ServletComponentScan

SpringBoot 환경에서 basePackages 하위 서블릿컴포넌트(필터, 서블릿, 리스너)를 스캔해서 빈으로 등록.
서블릿 컴포넌트에 필터는 @WebFilter, 서블릿은 @WebServlet, 리스너는 @WebListener 어노테이션 있는 경우에만
컴포넌트를 빈으로 등록.
SpringBoot의 내장톰캣을 사용하는 경우에만 동작. Spring 혹은 내장톰캣을 사용하지 않는 경우에는 web.xml에 추가해야한다.
