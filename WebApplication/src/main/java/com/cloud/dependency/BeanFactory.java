package com.cloud.dependency;


import com.cloud.validator.UserValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class BeanFactory {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserValidator userValidator(){
        return new UserValidator();
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint(){
        return new BasicAuthenticationEntryPoint() {
            @Override
            public void afterPropertiesSet() throws Exception {
                setRealmName("User_System");
                super.afterPropertiesSet();
            }

            @Override
            public void commence(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException authException) throws IOException {
                response.addHeader("WWW-Authenticate", "Basic realm = "+getRealmName());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println("Access denied");
            }
        };
    }
}
