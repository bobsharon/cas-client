package com.mfksoft.demo.config;

import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.config.AbstractShiroWebFilterConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;


public class ShiroConfiguration extends AbstractShiroWebFilterConfiguration {
    @Value("#{ @environment['cas.server-url-prefix'] ?: null }")
    private String prefixUrl;
    @Value("#{ @environment['cas.server-login-url'] ?: null }")
    private String casLoginUrl;
    @Value("#{ @environment['cas.client-host-url'] ?: null }")
    private String callbackUrl;

    //jwt秘钥
    @Value("${jwt.salt}")
    private String salt;

    @Bean
    public Realm pac4jRealm() {
        return new Pac4jRealm();
    }

    @Bean
    public Filter casSecurityFilter() {
        SecurityFilter filter = new SecurityFilter();
        filter.setClients("CasClient,rest,jwt");
        //filter.setConfig(casConfig());
        return filter;
    }
}
