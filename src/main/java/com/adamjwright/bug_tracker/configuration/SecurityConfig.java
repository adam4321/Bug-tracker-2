/******************************************************************************
**  Spring Boot Security configuration for the application
******************************************************************************/

package com.adamjwright.bug_tracker.configuration;

import com.adamjwright.bug_tracker.MyAuthenticationEntryPoint;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests(a -> a
				.antMatchers("/login", "/error", "/logout", "/unauthorized", 
                "/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
				.anyRequest().authenticated()
			)
            .formLogin(l -> l
                .loginPage("/login")
                .failureUrl("/error")
            )
			.exceptionHandling(e -> e
				.authenticationEntryPoint(new MyAuthenticationEntryPoint())
                .accessDeniedPage("/error")
			)
            .csrf(c -> c
                .ignoringAntMatchers("/login", "/logout")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .logout()
                .and()
			.oauth2Login(o -> o
                .defaultSuccessUrl("/home", true)
                .failureUrl("/error")
            );
                
		// @formatter:on
	}
}
