package cafeteria

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding
import cafeteria.config.kafka.KafkaProcessor

@SpringBootApplication
@EnableFeignClients
@EnableBinding(Array(classOf[KafkaProcessor]))
class SpringBootConfig {
}