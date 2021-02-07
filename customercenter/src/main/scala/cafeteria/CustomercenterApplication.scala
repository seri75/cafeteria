package cafeteria

import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext

object CustomercenterApplication extends App {
  val applicationContext :ApplicationContext = SpringApplication.run(classOf[SpringBootConfig])
}