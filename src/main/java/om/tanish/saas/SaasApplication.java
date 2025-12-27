package om.tanish.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaasApplication {

	public static void main(String[] args) {
		System.out.println("Starting SaaS Application...");
		SpringApplication.run(SaasApplication.class, args);
	}

}
