package cafeteria;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import cafeteria.config.kafka.KafkaProcessor;
import cafeteria.config.shutdown.TomcatGracefulShutdown;
import feign.okhttp.OkHttpClient;


@SpringBootApplication
@EnableBinding(KafkaProcessor.class)
@EnableFeignClients
public class OrderApplication {
    protected static ApplicationContext applicationContext;
    
    @Bean
	public ConfigurableServletWebServerFactory webServerFactory(final TomcatGracefulShutdown gracefulShutdown) {
	    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
	    factory.addConnectorCustomizers(gracefulShutdown);
	    return factory;
	}
    
    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    } 
    
    public static void main(String[] args) {
        applicationContext = SpringApplication.run(OrderApplication.class, args);
    }
}


