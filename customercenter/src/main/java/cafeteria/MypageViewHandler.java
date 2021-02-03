package cafeteria;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import cafeteria.config.kafka.KafkaProcessor;
import cafeteria.external.KakaoMessage;
import cafeteria.external.KakaoService;

@Service
public class MypageViewHandler {

	@Autowired
	private KakaoService kakaoService;
	
    @Autowired
    private MypageRepository mypageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1 (@Payload Ordered ordered) {
        try {
            if (ordered.isMe()) {
                // view 객체 생성
                Mypage mypage = new Mypage();
                // view 객체에 이벤트의 Value 를 set 함
                mypage.setOrderId(ordered.getId());
                mypage.setProductName(ordered.getProductName());
                mypage.setQty(ordered.getQty());
                mypage.setAmt(ordered.getAmt());
                mypage.setStatus(ordered.getStatus());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
                
                KakaoMessage message = new KakaoMessage();
                message.setPhoneNumber(ordered.getPhoneNumber());
                message.setMessage(new StringBuffer("Your Order is ").append(ordered.getStatus()).toString());
                kakaoService.sendKakao(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_UPDATE_1(@Payload OrderCanceled orderCanceled) {
        try {
            if (orderCanceled.isMe()) {
                // view 객체 조회
                List<Mypage> pagies = mypageRepository.findByOrderId(orderCanceled.getId());
                for(Mypage page  : pagies){
                    page.setStatus(orderCanceled.getStatus());
                	mypageRepository.save(page);
                }
                
                KakaoMessage message = new KakaoMessage();
                message.setPhoneNumber(orderCanceled.getPhoneNumber());
                message.setMessage(new StringBuffer("Your Order is ").append(orderCanceled.getStatus()).toString());
                kakaoService.sendKakao(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReceipted_then_UPDATE_3(@Payload Receipted receipted) {
        try {
            if (receipted.isMe()) {
                // view 객체 조회
            	List<Mypage> pagies = mypageRepository.findByOrderId(receipted.getOrderId());
            	for(Mypage page  : pagies){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
            		page.setStatus(receipted.getStatus());
                    // view 레파지 토리에 save
                	mypageRepository.save(page);
                }
            	
            	KakaoMessage message = new KakaoMessage();
                message.setPhoneNumber(receipted.getPhoneNumber());
                message.setMessage(new StringBuffer("Your Order is ").append(receipted.getStatus()).toString());
                kakaoService.sendKakao(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenMade_then_UPDATE_4(@Payload Made made) {
        try {
            if (made.isMe()) {
                // view 객체 조회
            	List<Mypage> pagies = mypageRepository.findByOrderId(made.getOrderId());
            	for(Mypage page  : pagies){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
            		page.setStatus(made.getStatus());
                    // view 레파지 토리에 save
                	mypageRepository.save(page);
                }
            	
            	KakaoMessage message = new KakaoMessage();
                message.setPhoneNumber(made.getPhoneNumber());
                message.setMessage(new StringBuffer("Your Order is ").append(made.getStatus()).append("\nCome and Take it, Please!").toString());
                kakaoService.sendKakao(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}