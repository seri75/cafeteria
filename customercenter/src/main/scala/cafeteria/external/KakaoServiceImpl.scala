package cafeteria.external

import org.springframework.stereotype.Component

@Component
class KakaoServiceImpl extends KakaoService {
  
	override def sendKakao(message :KakaoMessage) {
		logger.info(s"\nTo. ${message.phoneNumber}\n${message.message}\n")
	}
}
