package cafeteria

import org.springframework.cloud.stream.annotation.StreamListener
import cafeteria.external.KakaoMessage
import org.springframework.stereotype.Service
import org.springframework.messaging.handler.annotation.Payload
import cafeteria.config.kafka.KafkaProcessor
import org.springframework.beans.factory.annotation.Autowired
import cafeteria.external.KakaoService

@Service
class PolicyHandler {
  
  @Autowired
	private val kakaoService :KakaoService = null
	
  @StreamListener(KafkaProcessor.INPUT)
  def whenCanceleFailed_(@Payload cancelFailed :CancelFailed) {
    try {
      if (cancelFailed.isMe()) {
      
        val message :KakaoMessage = new KakaoMessage()
        message.phoneNumber = cancelFailed.phoneNumber
        message.message = s"""Your Order is already started. You cannot cancel!!"""
        kakaoService.sendKakao(message)
      }
    } catch {
      case e :Exception => e.printStackTrace()
    }
  }
}
