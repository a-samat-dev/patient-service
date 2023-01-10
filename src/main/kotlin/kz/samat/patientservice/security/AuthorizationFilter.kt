package kz.samat.patientservice.security

import lombok.extern.slf4j.Slf4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HTTP filter for each request
 *
 * Created by Samat Abibulla on 2022/12/13
 */
@Component
@Slf4j
class AuthorizationFilter : OncePerRequestFilter() {

    /**
     * Gets user details from request headers and sets security context
     *
     * @param request incoming http request
     * @param response outgoing http response
     * @param filterChain list of filters that applies for request&response
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = request.getHeader("userId")
        val role = request.getHeader("role")

        if (userId.isNotBlank()) {
            val userDetails = User(userId, "", emptyList())
            val authentication =
                UsernamePasswordAuthenticationToken(userDetails, null, listOf(SimpleGrantedAuthority(role)))
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        }
    }
}