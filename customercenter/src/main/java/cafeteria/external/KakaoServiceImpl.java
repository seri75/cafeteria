package cafeteria.external;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoServiceImpl implements KakaoService {

	@Override
	public void sendKakao(KakaoMessage message) {
		log.info("\nTo. {}\n{}\n", message.getPhoneNumber(), message.getMessage());
	}

}
