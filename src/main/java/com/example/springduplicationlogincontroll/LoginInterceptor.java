package com.example.springduplicationlogincontroll;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final HttpSession session = request.getSession();
        String path = request.getRequestURI();
        if(path.contains("/main.do") || path.contains("/login.do")) {
            return true;
        }else if (session.getAttribute("login_id") == null) { // 어떠한 이유로든 해당 세션이 invalidate 되거나 로그인하지 않은 사용자는 메인페이지로 리다이렉트.
            response.sendRedirect("/main.do");
            return false;
        }

        return true;
    }
}
