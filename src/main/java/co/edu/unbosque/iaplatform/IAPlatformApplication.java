package co.edu.unbosque.iaplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class IAPlatformApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(IAPlatformApplication.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(IAPlatformApplication.class);
    }
    
    
}
