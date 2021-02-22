package cafeteria.external

//import com.typesafe.scalalogging.LazyLogging
import com.typesafe.scalalogging.slf4j.LazyLogging

trait KakaoService extends LazyLogging {
  def sendKakao(message :KakaoMessage)
}
