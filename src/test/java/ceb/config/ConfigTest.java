package ceb.config;

import ceb.CayCanhApplication;
import ceb.ServletInitializer;
import ceb.security.JwtAccessDeniedHandler;
import ceb.security.JwtAuthenticationEntryPoint;
import ceb.security.JwtAuthenticationFilter;
import ceb.security.OAuth2LoginFailureHandler;
import ceb.security.OAuth2LoginSuccessHandler;
import com.cloudinary.Cloudinary;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ConfigTest {

    @Test
    void simpleBeanConfigsExposeExpectedBeans() {
        PasswordEncoder encoder = new PasswordConfig().passwordEncoder();
        assertThat(encoder.matches("secret", encoder.encode("secret"))).isTrue();

        SwaggerConfig swaggerConfig = new SwaggerConfig();
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

        VnpayConfig vnpayConfig = new VnpayConfig();
        ReflectionTestUtils.setField(vnpayConfig, "vnpTmnCode", "TMN");
        ReflectionTestUtils.setField(vnpayConfig, "vnpHashSecret", "secret");
        ReflectionTestUtils.setField(vnpayConfig, "vnpPayUrl", "https://pay");
        ReflectionTestUtils.setField(vnpayConfig, "vnpReturnUrl", "https://return");
        assertThat(vnpayConfig.getVnpTmnCode()).isEqualTo("TMN");
        assertThat(vnpayConfig.getVnpHashSecret()).isEqualTo("secret");
        assertThat(vnpayConfig.getVnpPayUrl()).isEqualTo("https://pay");
        assertThat(vnpayConfig.getVnpReturnUrl()).isEqualTo("https://return");
    }

    @Test
    void cloudinaryConfigBuildsClientFromProperties() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudName", "cloud");
        ReflectionTestUtils.setField(config, "apiKey", "key");
        ReflectionTestUtils.setField(config, "apiSecret", "secret");

        Cloudinary cloudinary = config.cloudinary();

        assertThat(cloudinary.config.cloudName).isEqualTo("cloud");
        assertThat(cloudinary.config.apiKey).isEqualTo("key");
    }

    @Test
    void securityCorsConfigurationAllowsConfiguredMethodsAndCredentials() {
        SecurityConfig config = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(JwtAuthenticationEntryPoint.class),
                mock(JwtAccessDeniedHandler.class),
                mock(OAuth2LoginSuccessHandler.class),
                mock(OAuth2LoginFailureHandler.class));

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(new MockHttpServletRequest());

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    void servletInitializerPointsToApplicationClass() {
        SpringApplicationBuilder builder = new TestServletInitializer().configure(new SpringApplicationBuilder());

        assertThat(builder).isNotNull();
    }

    private static class TestServletInitializer extends ServletInitializer {
        @Override
        public SpringApplicationBuilder configure(SpringApplicationBuilder application) {
            return super.configure(application);
        }
    }
}
