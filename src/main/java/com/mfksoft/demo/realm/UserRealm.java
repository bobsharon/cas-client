package com.mfksoft.demo.realm;

import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import io.buji.pac4j.token.Pac4jToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 * 用户自定义返回信息
 *
 * @author bobsharon
 * @date 19-8-10 下午11:30
 * @since 1.0.0
 */
public class UserRealm extends Pac4jRealm {

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        Pac4jToken token = (Pac4jToken)authenticationToken;
        List<CommonProfile> profiles = token.getProfiles();
        // TODO
        Pac4jPrincipal principal = new Pac4jPrincipal(profiles, this.getPrincipalNameAttribute());
        PrincipalCollection principalCollection = new SimplePrincipalCollection(principal, this.getName());
        return new SimpleAuthenticationInfo(principalCollection, profiles.hashCode());
    }

}