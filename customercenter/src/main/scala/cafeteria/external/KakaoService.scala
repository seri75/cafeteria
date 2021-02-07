package cafeteria.external

import com.typesafe.scalalogging.LazyLogging

trait KakaoService extends LazyLogging {
  def sendKakao(message :KakaoMessage)
}
