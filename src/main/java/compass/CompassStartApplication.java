package compass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class CompassStartApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompassStartApplication.class, args);
	}

	@Configuration
	public class CorsConfig {
		private CorsConfiguration buildConfig() {
			CorsConfiguration corsConfiguration = new CorsConfiguration();
			corsConfiguration.addAllowedOrigin("*"); // 1
			corsConfiguration.addAllowedHeader("*"); // 2
			corsConfiguration.addAllowedMethod("*"); // 3
			return corsConfiguration;
		}

		@Bean
		public CorsFilter corsFilter() {
			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", buildConfig()); // 4
			return new CorsFilter(source);
		}
	}
}
