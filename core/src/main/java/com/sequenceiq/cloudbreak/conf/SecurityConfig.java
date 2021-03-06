package com.sequenceiq.cloudbreak.conf;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sequenceiq.cloudbreak.domain.CbUser;
import com.sequenceiq.cloudbreak.service.user.UserDetailsService;
import com.sequenceiq.cloudbreak.service.user.UserFilterField;

@Configuration
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private OwnerBasedPermissionEvaluator ownerBasedPermissionEvaluator;

    @Bean MethodSecurityExpressionHandler expressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        ownerBasedPermissionEvaluator.setUserDetailsService(userDetailsService);
        expressionHandler.setPermissionEvaluator(ownerBasedPermissionEvaluator);
        return expressionHandler;
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    protected static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

        @Inject
        private MethodSecurityExpressionHandler expressionHandler;

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            return expressionHandler;
        }
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        private static final String[] BLUEPRINT_URL_PATTERNS = new String[]{"/api/v1/blueprints/**"};
        private static final String[] TEMPLATE_URL_PATTERNS = new String[]{"/api/v1/templates/**"};
        private static final String[] CREDENTIAL_URL_PATTERNS = new String[]{"/api/v1/credentials/**"};
        private static final String[] RECIPE_URL_PATTERNS = new String[]{"/api/v1/recipes/**"};
        private static final String[] NETWORK_URL_PATTERNS = new String[]{"/api/v1/networks/**"};
        private static final String[] SECURITYGROUP_URL_PATTERNS = new String[]{"/api/v1/securitygroups/**"};
        private static final String[] STACK_URL_PATTERNS = new String[]{"/api/v1/stacks/**"};

        @Value("${cb.client.id}")
        private String clientId;

        @Value("${cb.client.secret}")
        private String clientSecret;

        @Inject
        @Named("identityServerUrl")
        private String identityServerUrl;

        @Inject
        private UserDetailsService userDetailsService;

        @Bean RemoteTokenServices remoteTokenServices() {
            RemoteTokenServices rts = new RemoteTokenServices();
            rts.setClientId(clientId);
            rts.setClientSecret(clientSecret);
            rts.setCheckTokenEndpointUrl(identityServerUrl + "/check_token");
            return rts;
        }

        @Bean PBEStringCleanablePasswordEncryptor encryptor() {
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(clientSecret);
            return encryptor;
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("cloudbreak");
            resources.tokenServices(remoteTokenServices());
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.addFilterAfter(new ScimAccountGroupReaderFilter(userDetailsService), AbstractPreAuthenticatedProcessingFilter.class)
                    .authorizeRequests()

                    .antMatchers(HttpMethod.GET, BLUEPRINT_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.blueprints.read') or #oauth2.hasScope('cloudbreak.blueprints')")
                    .antMatchers(HttpMethod.GET, TEMPLATE_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.templates.read') or #oauth2.hasScope('cloudbreak.templates')")
                    .antMatchers(HttpMethod.GET, CREDENTIAL_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.credentials.read') or #oauth2.hasScope('cloudbreak.credentials')")
                    .antMatchers(HttpMethod.GET, RECIPE_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.recipes.read') or #oauth2.hasScope('cloudbreak.recipes')")
                    .antMatchers(HttpMethod.GET, NETWORK_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.networks.read') or #oauth2.hasScope('cloudbreak.networks')")
                    .antMatchers(HttpMethod.GET, SECURITYGROUP_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.securitygroups.read') or #oauth2.hasScope('cloudbreak.securitygroups')")
                    .antMatchers(HttpMethod.GET, STACK_URL_PATTERNS)
                        .access("#oauth2.hasScope('cloudbreak.stacks.read') or #oauth2.hasScope('cloudbreak.stacks')"
                                + " or #oauth2.hasScope('cloudbreak.autoscale')")

                    .antMatchers("/users/**").access("#oauth2.hasScope('openid')")
                    .antMatchers(BLUEPRINT_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.blueprints')")
                    .antMatchers(TEMPLATE_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.templates')")
                    .antMatchers(CREDENTIAL_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.credentials')")
                    .antMatchers(RECIPE_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.recipes')")
                    .antMatchers(NETWORK_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.networks')")
                    .antMatchers(SECURITYGROUP_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.securitygroups')")
                    .antMatchers(STACK_URL_PATTERNS).access("#oauth2.hasScope('cloudbreak.stacks') or #oauth2.hasScope('cloudbreak.autoscale')")
                    .antMatchers("/api/v1/stacks/ambari", "/api/v1/stacks/*/certificate").access("#oauth2.hasScope('cloudbreak.autoscale')")
                    .antMatchers("/api/v1/events").access("#oauth2.hasScope('cloudbreak.events')")
                    .antMatchers("/api/v1/usages/account/**").access("#oauth2.hasScope('cloudbreak.usages.account')")
                    .antMatchers("/api/v1/usages/user/**").access("#oauth2.hasScope('cloudbreak.usages.user')")
                    .antMatchers("/api/v1/usages/**").access("#oauth2.hasScope('cloudbreak.usages.global')")
                    .antMatchers("/api/v1/subscription").access("#oauth2.hasScope('cloudbreak.subscribe')")
                    .antMatchers("/api/v1/accountpreferences/*").access("#oauth2.hasScope('cloudbreak.templates') and #oauth2.hasScope('cloudbreak.stacks')");

                    http.csrf().disable();

                    http.headers().contentTypeOptions();
        }
    }

    private static class ScimAccountGroupReaderFilter extends OncePerRequestFilter {

        private UserDetailsService userDetailsService;

        public ScimAccountGroupReaderFilter(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException,
                IOException {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                OAuth2Authentication oauth = (OAuth2Authentication) authentication;
                if (oauth.getUserAuthentication() != null) {
                    String username = (String) authentication.getPrincipal();
                    CbUser user = userDetailsService.getDetails(username, UserFilterField.USERNAME);
                    request.setAttribute("user", user);
                }
            }
            filterChain.doFilter(request, response);
        }
    }

}
