package com.mfksoft.demo.config;

import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.LogoutFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.AbstractShiroWebFilterConfiguration;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.rest.CasRestFormClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * 对shiro的安全配置，是对cas的登录策略进行配置
 *
 * @author bobsharon
 * @date 19-8-10 上午10:41
 * @since 1.0.0
 */
@Configuration
public class ShiroConfiguration extends AbstractShiroWebFilterConfiguration {

    @Value("#{@environment['app.prefixUrl']?:null}")
    private String prefixUrl;

    @Value("#{@environment['app.casLoginUrl']?:null }")
    private String casLoginUrl;

    @Value("#{@environment['app.callbackUrl']?:null }")
    private String callbackUrl;

    @Value("${app.jwt.salt}")
    private String salt;

    /**
     * JWT生成器, 对CommonProfile生成JWT Token, 每次访问应用需携带Token
     * @return JwtGenerator
     */
    @Bean
    protected JwtGenerator jwtGenerator() {
        return new JwtGenerator(
                new SecretSignatureConfiguration(salt),
                new SecretEncryptionConfiguration(salt)
        );
    }

    /**
     * JWT校验器, 是前后端分离的核心校验器
     * @return JwtAuthenticator
     */
    @Bean
    protected JwtAuthenticator jwtAuthenticator() {
        JwtAuthenticator jwtAuthenticator = new JwtAuthenticator();
        jwtAuthenticator.addSignatureConfiguration(new SecretSignatureConfiguration(salt));
        jwtAuthenticator.addEncryptionConfiguration(new SecretEncryptionConfiguration(salt));
        return jwtAuthenticator;
    }

    /**
     * 通过REST接口访问的客户端
     * @return CasRestFormClient
     */
    @Bean
    protected CasRestFormClient casRestFormClient() {
        CasRestFormClient casRestFormClient = new CasRestFormClient();
        casRestFormClient.setConfiguration(casConfiguration());
        casRestFormClient.setName("rest");
        return casRestFormClient;
    }

    /**
     * 通过浏览器访问的CAS客户端
     * @return CasClient
     */
    @Bean
    protected CasClient casClient() {
        CasClient casClient = new CasClient();
        casClient.setConfiguration(casConfiguration());
        casClient.setCallbackUrl(callbackUrl);
        return casClient;
    }

    /**
     * 通过JWT Token访问的客户端
     * @return HeaderClient
     */
    @Bean
    protected ParameterClient parameterClient() {
        ParameterClient parameterClient = new ParameterClient("token", jwtAuthenticator());
        parameterClient.setSupportGetRequest(true);
        parameterClient.setName("jwt");
        return parameterClient;
    }

    /**
     * 加入所有支持的CAS客户端
     * @return Clients
     */
    @Bean
    protected Clients clients() {
        Clients clients = new Clients();
        clients.setClients(casClient(), casRestFormClient(), parameterClient());
        return clients;
    }

    /**
     * 配置pac4j
     * @return Config
     */
    @Bean
    protected Config casConfig() {
        Config config = new Config();
        config.setClients(clients());
        return config;
    }

    /**
     * CAS的基本协议包括认证服务器Url和REST调用协议
     * @return CasConfiguration
     */
    @Bean
    protected CasConfiguration casConfiguration() {
        CasConfiguration casConfiguration = new CasConfiguration();
        casConfiguration.setProtocol(CasProtocol.CAS30);
        casConfiguration.setPrefixUrl(prefixUrl);
        return casConfiguration;
    }

    /**
     * 定义Shiro Filter过滤链
     * @return ShiroFilterChainDefinition
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition definition = new DefaultShiroFilterChainDefinition();
        definition.addPathDefinition("/callback","callbackFilter");
        definition.addPathDefinition("/logout","logoutFilter");
        definition.addPathDefinition("/**","casSecurityFilter");
        return definition;
    }

    /**
     * CAS核心过滤器, 所有的客户端必须在pac4j的Config中注册
     * @return Filter
     */
    @Bean
    public Filter casSecurityFilter() {
        SecurityFilter securityFilter = new SecurityFilter();
        securityFilter.setClients("CasClient,rest,jwt");
        securityFilter.setConfig(casConfig());
        return securityFilter;
    }

    /**
     * Shiro Filter实例
     * @return Map<String, Filter>
     */
    @Bean
    protected Map<String, Filter> filters() {
        Map<String, Filter> filters = new HashMap<>(3);
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(casConfig());
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setConfig(casConfig());
        logoutFilter.setCentralLogout(true);
        logoutFilter.setLocalLogout(true);

        filters.put("callbackFilter", callbackFilter);
        filters.put("logoutFilter", logoutFilter);
        filters.put("casSecurityFilter", casSecurityFilter());
        return filters;
    }

    @Bean
    protected SubjectFactory subjectFactory() {
        return new Pac4jSubjectFactory();
    }

    @Bean
    public FilterRegistrationBean delegatingFilterProxy(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName("shiroFilter");
        filterRegistrationBean.setFilter(proxy);
        return filterRegistrationBean;
    }

    @Bean
    protected WebSecurityManager securityManager() {
        return new DefaultWebSecurityManager(pac4jRealm());
    }

    @Bean("shiroFilter")
    protected ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ((DefaultSecurityManager)securityManager).setSubjectFactory(subjectFactory());
        ShiroFilterFactoryBean shiroFilterFactoryBean = super.shiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilters(filters());
        return shiroFilterFactoryBean;
    }

    /**
     * pac4j鉴权的Realm
     * @return Realm
     */
    @Bean
    public Realm pac4jRealm() {
        return new Pac4jRealm();
    }

}
