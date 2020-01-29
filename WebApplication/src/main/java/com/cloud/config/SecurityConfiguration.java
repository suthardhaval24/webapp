package com.cloud.config;

import com.cloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationEntryPoint basicAuthenticationEntryPoint;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.httpBasic().authenticationEntryPoint(basicAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //public api
        http.authorizeRequests().antMatchers(HttpMethod.POST).permitAll();

        //authenticated api
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET,"v1/user/self").fullyAuthenticated()
                .anyRequest().permitAll();

        //authenticated api
        http.authorizeRequests()
                .antMatchers(HttpMethod.PUT,"v1/user/self").fullyAuthenticated()
                .anyRequest().permitAll();

        //authenticated api
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST,"/v1/bill").fullyAuthenticated()
                .anyRequest().permitAll();

        //authenticated api
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET,"/v1/bills").fullyAuthenticated()
                .anyRequest().permitAll();

        //authenticated api
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET,"/v1/bill/{id}").fullyAuthenticated()
                .anyRequest().permitAll();

        //authenticated api
        http.authorizeRequests()
                .antMatchers(HttpMethod.DELETE,"/v1/bill/{id}").fullyAuthenticated()
                .anyRequest().permitAll();
    }
}
