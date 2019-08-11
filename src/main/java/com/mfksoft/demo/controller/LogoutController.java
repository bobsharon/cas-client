package com.mfksoft.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * 应用自定义单点登出控制器
 *
 * @author bobsharon
 * @date 19-8-11 下午11:46
 * @since 1.0.0
 */
@Controller
public class LogoutController {

    /**
     * 单点登出
     *
     * @param session session
     * @return String
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:logout";
    }
}
