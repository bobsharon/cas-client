package com.mfksoft.demo.controller;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.pac4j.cas.client.rest.CasRestFormClient;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.cas.profile.CasRestProfile;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 获取用户信息, 生成Token
 *
 * @author bobsharon
 * @date 19-8-10 下午6:08
 * @since 1.0.0
 */
@RestController
public class IndexController {

    @Autowired
    private CasRestFormClient casRestFormClient;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Value("${app.serviceUrl}")
    private String serviceUrl;

    @GetMapping("/app/users")
    public Object detail(HttpServletRequest request) {
        return "users:" + request.getUserPrincipal().getName();
    }

    /**
     * 根据ticket获取用户信息, 并返回JWT Token
     * @param request request
     * @param response response
     * @return Object
     */
    @PostMapping("/app/login")
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<>(1);
        J2EContext context = new J2EContext(request, response);
        final ProfileManager<CasRestProfile> manager = new ProfileManager<>(context);
        final Optional<CasRestProfile> profile = manager.get(true);
        TokenCredentials tokenCredentials = casRestFormClient.requestServiceTicket(serviceUrl, profile.get(), context);
        final CasProfile casProfile = casRestFormClient.validateServiceTicket(serviceUrl, tokenCredentials, context);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 5);
        // 设置Token在5分钟后过期
        jwtGenerator.setExpirationTime(cal.getTime());
        String token = jwtGenerator.generate(casProfile);
        model.put("token", token);
        return new HttpEntity<>(model);
    }

}
