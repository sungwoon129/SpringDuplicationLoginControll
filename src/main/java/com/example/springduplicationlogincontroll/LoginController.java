package com.example.springduplicationlogincontroll;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @PostMapping("/login.do")
    public String login(HttpServletRequest request, HttpSession session ) {
        String id = request.getParameter("id");
        if(id != null) {
            String userid = SessionConfig.getSessionIdCheck("login_id",id);
            System.out.println("userid : " + userid);
            session.setMaxInactiveInterval(60 * 60);
            session.setAttribute("login_id", id);
            return  "redirect:/home.do";
        }
        return "redirect:/main.do";
    }

    @GetMapping("/main.do")
    public String index() {
        return "login";
    }

    @GetMapping("/home.do")
    public String home() {
        return "home";
    }

}
