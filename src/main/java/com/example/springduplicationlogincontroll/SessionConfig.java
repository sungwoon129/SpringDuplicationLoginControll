package com.example.springduplicationlogincontroll;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 처음에는 webListener 어노테이션을 붙여도 sessionCreated 이벤트가 동작하지 않았음. 이후 프로젝트 Main 클래스에 @ServletComponentScan를 붙여주자 정상적으로 동작.
 * WebListener 어노테이션을 뜯어보면 스프링 bean으로 등록하는 부분이 존재하지 않음. 그래서 별도로 등록을 해야함. 이 때, 스프링 빈으로 등록해주는 어노테이션이 ServletComponentScan.
 * 다만 이 어노테이션은 springboot 내장톰캣을 사용하는 경우에만 동작. springboot 내장톰캣을 사용하지 않는 경우에는 web.xml에 리스너로 등록해줘야함.
 */
@WebListener
public class SessionConfig implements HttpSessionListener {

    /*
        SessionConfig 객체를 생성할 때마다 생성되는 것이 아니라 어플리케이션 최초 실행 시 생성되는 하나의 객체로 관리해야 하므로 static 필드로 설정.
        HashMap 자료구조로 key 값으로는 세션id, value값으로는 세션을 등록. ConcurrentHashMap은 일반 hashmap과 달리 멀티쓰레드 작업환경에서 Thread safe한 특징을 가짐.
        즉, 중복로그인을 처리할 때 여러 클라이언트에서 요청을 보내면 여러개의 쓰레드에서 요청을 처리할텐데, 어떤 쓰레드에서 해당 변수에 접근하더라도 동일한 값을 가지고 있어야함.
        또한, ConcurrentHashMap은 HashTable과 달리 읽기 작업에 대해서는 내부 메서드에 synchronized가 선언되어 있지 않음. 여러 쓰레드에서 동시에 접근하여 값을 읽을 수 있다는 뜻.
        이러한 특징은 멀티쓰레드 환경에서 HashTable보다 좋은 성능을 보임. HashTable은 모든 메서드에 synchronized키워드가 선언되어 있어 쓰레드 작업간의 순서를 보장하기 위해 특정
        쓰레드에서 작업을 처리할 때 lock이 걸리므로 여러 쓰레드가 동시에 해당 변수에 접근할 경우 속도가 느려질 수 있고 심한 경우 타임아웃 발생가능성 존재.
     */
    private static final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();


    /*
        이 메서드에 synchronized가 붙어있는 이유는 여러 쓰레드에서 짧은 간격으로 로그인 요청이 들어와서 특정 쓰레드에서 작업하는 도중에 다른 쓰레드에서 로그인 요청이 들어올 경우
        정확하게 세션 id를 체크하지 못할 수 있음. 마치 DB의 데드락과 같은 이유로. 예를 들어, 1번 쓰레드에서 tester란 아이디로 로그인한 유저가 서비스를 이용하고 있는 상태에서
        2번 쓰레드에서 tester라는 아이디로 로그인요청을 하고 거의 동시에 3번 쓰레드에서 동일한 아이디로 로그인 요청이 올 경우 2번쓰레드에서 해당 메서드를 끝까지 완료하지 못한
        상태에서 3번쓰레드가 접근하게 되면, 중복 로그인이지만 sessions 변수에서 2번 쓰레드가 아직 정상적으로 세션을 invalidate하지 못한상황에서 3번 쓰레드가 중복으로 invalidate를
        하게 되는 경우가 존재할 수 있다. 이 경우 이외에도 개발자가 컨트롤하기 어려운 예외상황이 발생할 가능성이 높으므로, 쓰레들간의 작업을 동기적으로 처리하기 위해  synchronized
        키워드 선언. static인 이유는 모든 쓰레드에서 로그인 요청을 할 때마다 이 메서드를 참조할텐데, 그때마다 SessionConfig 클래스 객체를 생성해서 작업할 경우 여러 쓰레드에서
        동시에 접근할 경우, 내부에서 값이 변하는 변수들이 안전하지 못할 수 있다. 그래서 static을 선언하여 메서드를 쓰레드간에 공유할 수 있도록 한다.
        static이 없다면 객체별로 잠금처리되므로, 컨트롤러에서 매번 다른 객체를 생성하게되면 접근이 가능해서 동시에 해당 메서드를 참조할 수 있게되므로, 쓰레드 동기화와 순차적인 처리가
        보장되어야 하는 중복로그인 기능에는 적합하지 않다.
        => synchronized static 2개의 키워드를 조합하면 SessionConfig 객체 생성없이 해당 메서드를 호출하므로(단 하나의 메서드이므로) 모든 쓰레드에서 동시에 실행되지 않는다.
     */
    public synchronized static String getSessionIdCheck(String type, String compareId) {
        String result = "";
        for(String key : sessions.keySet()) {
            HttpSession session = sessions.get(key);
            if(session != null && session.getAttribute(type) != null && session.getAttribute(type).toString().equals(compareId)) {
                result = key;
            }
        }
        removeSessionForDoubleLogin(result);
        return result;
    }

    private static void removeSessionForDoubleLogin(String userid) {
        System.out.println("remove id : " + userid);
        if(userid != null && userid.length() > 0) {
            sessions.get(userid).invalidate();
            sessions.remove(userid);
        }
    }

    /*
        세션 생성은 session.setAttribute 하거나 getSession 하는 경우 생성되는 것이 아니라, 클라이언트가 최초로 서버에 접근할 때 생성된다.
        => 즉, 로그인할 때 세션이 생성되는 것이 아니라는 점을 유의해야 한다.
    */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("session created.");
        System.out.println(se);
        sessions.put(se.getSession().getId(),se.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("session destroyed.");
        //removeSessionForDoubleLogin에서 remove한 내용을 여기서 다시 하는 이유는 시간이 지나서 세션이 파괴되었을 때에도 sessions에서 해당 세션을 지워줘야하기때문.
        if(sessions.get(se.getSession().getId()) != null) {
            sessions.get(se.getSession().getId()).invalidate();
            sessions.remove(se.getSession().getId());
        }
    }
}
