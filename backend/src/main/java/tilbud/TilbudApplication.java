package tilbud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TilbudApplication {

    public static void main(String[] args) {
        SpringApplication.run(TilbudApplication.class, args);
    }
}
