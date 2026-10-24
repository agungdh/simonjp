package id.my.agungdh.simonjp;

import id.my.agungdh.simonjp.config.PageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PageProperties.class)
public class SimonjpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimonjpApplication.class, args);
	}

}
