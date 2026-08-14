package cv.terrasystem.zebratravelb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZebratravelBApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZebratravelBApplication.class, args);
    }

}
