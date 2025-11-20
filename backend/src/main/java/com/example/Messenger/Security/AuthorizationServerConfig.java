package com.example.Messenger.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.Messenger.Entity.User;
import com.example.Messenger.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.authentication.*;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//http://localhost:9999/oauth2/authorize?response_type=code&client_id=chau&scope=openid%20profile%20email&redirect_uri=http://localhost:8081/login/oauth2/code/messenger


@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationServerConfig.class);


    @Autowired
    private UserRepository userRepository;

    @Value("${app.auth.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    // ✅ Authorization Server Filter Chain
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        // ⚙️ Gắn custom redirect_uri validator
        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.authenticationProviders(configureAuthenticationValidator())
                );

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // ✅ Đăng ký client
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient writerClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("chau")
                .clientSecret("{noop}123")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8081/login/oauth2/code/messenger")
                .redirectUri("http://localhost:8090/login/oauth2/code/messenger")
                .redirectUri("http://localhost:8070/login/oauth2/code/messenger")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("email")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build();

        RegisteredClient readerClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("reader")
                .clientSecret("{noop}1234")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://localhost:8081/login/oauth2/code/messenger")
                .redirectUri("https://my.redirect.uri")
                .redirectUri("https://localhost:8443/openapi/webjars/swagger-ui/oauth2-redirect.html")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("product:read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(writerClient, readerClient);
    }

    // ✅ Thêm thông tin custom vào JWT (email, username, roles)
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims((claims) -> {
                    Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
                            .stream()
                            .map(c -> c.replaceFirst("^ROLE_", ""))
                            .collect(Collectors.toSet());
                    claims.put("roles", roles);

                    Optional<User> optionalUser = userRepository.findUserByEmail(context.getPrincipal().getName());
                    optionalUser.ifPresentOrElse(user -> {
                        claims.put("email", user.getEmail());
                        claims.put("username", user.getUsername());
                    }, () -> LOG.warn("User not found: {}", context.getPrincipal().getName()));
                });
            }
        };
    }

    // ✅ Cấu hình endpoint
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://" + host + ":" + port)
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .oidcUserInfoEndpoint("/connect/userinfo")
                .oidcClientRegistrationEndpoint("/connect/register")
                .build();
    }

    // ✅ Custom validator kiểm tra redirect_uri
    private Consumer<List<AuthenticationProvider>> configureAuthenticationValidator() {
        return (authenticationProviders) -> authenticationProviders.forEach((authenticationProvider) -> {
            if (authenticationProvider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider provider) {
                Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> authenticationValidator =
                        new CustomRedirectUriValidator()
                                .andThen(OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR);
                provider.setAuthenticationValidator(authenticationValidator);
            }
        });
    }

    // ✅ Custom redirect_uri validator
    static class CustomRedirectUriValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

        @Override
        public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext authenticationContext) {
            OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                    authenticationContext.getAuthentication();
            RegisteredClient registeredClient = authenticationContext.getRegisteredClient();
            String requestedRedirectUri = authorizationCodeRequestAuthentication.getRedirectUri();
            LOG.trace("Validating redirect_uri: {}", requestedRedirectUri);
            if (!registeredClient.getRedirectUris().contains(requestedRedirectUri)) {
                LOG.warn("❌ Invalid redirect_uri: {}", requestedRedirectUri);
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                        "Invalid redirect_uri: " + requestedRedirectUri, null);
                throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
            }

            LOG.info("✅ Redirect URI OK: {}", requestedRedirectUri);
        }
    }
}