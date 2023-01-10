package kz.samat.patientservice.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Security configurations
 *
 * Created by Samat Abibulla on 2022/12/13
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class WebSecurityConfig @Autowired constructor(private val authorizationFilter: AuthorizationFilter) {

    @Bean
    fun getFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors().and().csrf().disable()
        http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}