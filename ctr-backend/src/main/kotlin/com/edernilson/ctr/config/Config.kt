package com.edernilson.ctr.config

import com.edernilson.ctr.domain.User
import com.edernilson.ctr.domain.UserRepository
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.annotation.PostConstruct
import javax.sql.DataSource


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
class SecurityConfig {

    @Value("\${cors.originPatterns:default}")
    private val corsOriginPatterns: String = ""

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
        http.cors()
        http.headers().frameOptions().sameOrigin()
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        http
            .authorizeRequests()
            .antMatchers("/h2-console/**").permitAll()
            .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()


        return http.build()
    }

    @Bean
    fun userDetailsService(dataSource: DataSource, bCryptPasswordEncoder: BCryptPasswordEncoder): UserDetailsService {

        val queryUsers = "select username, password, enabled from users where username=?"
        val queryRoles = "select u.username, r.roles from user_roles r, users u where r.user_id = u.id and u.username=?"

        return JdbcUserDetailsManagerConfigurer()
            .dataSource(dataSource)
            .withDefaultSchema()
            .passwordEncoder(bCryptPasswordEncoder)
            .usersByUsernameQuery(queryUsers)
            .authoritiesByUsernameQuery(queryRoles)
            .userDetailsService
    }

    @Bean
    fun authManager(
        http: HttpSecurity,
        dataSource: DataSource,
        bCryptPasswordEncoder: BCryptPasswordEncoder
    ): AuthenticationManager? {
        return http.getSharedObject(AuthenticationManagerBuilder::class.java)
            .userDetailsService(userDetailsService(dataSource, bCryptPasswordEncoder))
            .passwordEncoder(bCryptPasswordEncoder)
            .and()
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = corsOriginPatterns.split(",")
//        configuration.allowedOrigins = listOf("https://localhost:8080")
        configuration.allowedMethods =
            listOf("GET", "POST", "PUT", "DELETE", "PATCH")
        configuration.addAllowedHeader("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

@Configuration
class LoadUserConfig(
    val passwordEncoder: PasswordEncoder,
    val userRepository: UserRepository
) {

    @PostConstruct
    fun init() {
        val admin = User(
            username = "admin",
            password = passwordEncoder.encode("password"),
            roles = mutableListOf("ROLE_ADMIN")
        )
        userRepository.findByUsername("admin") ?: userRepository.save(admin)
    }
}

@Configuration
class AppConfig {

    @Bean
    fun messageSource() = ReloadableResourceBundleMessageSource().apply {
        setBasename("classpath:/i18n/messages")
    }
}

@Configuration
class OpenAPIConfig {

    @Bean
    fun openAPIDocumentation(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("C.T.R. - Car Travel Route API")
                    .description("API do sistema C.T.R., de planejamento de viagens intermunicipal de transportes alternativos")
                    .version("v1.0")
                    .contact(
                        Contact()
                            .name("Eder Nilson")
                            .email("eder.nilson@gmail.com")
                    )
            )
    }
}
