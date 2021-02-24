package cafeteria;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import cafeteria.config.kafka.KafkaProcessor;

@Service
public class SalePageViewHandler {

    @Autowired
    private SalePageRepository salePageRepository;
    
    @Autowired
    private SaleRepository saleRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_CREATE_1 (@Payload PaymentApproved paymentApproved) {
        try {
            if (paymentApproved.isMe()) {
                // view 객체 생성
                SalePage page  = new SalePage();
                // view 객체에 이벤트의 Value 를 set 함
                page.setOrderId(paymentApproved.getOrderId());
                page.setPhoneNumber(paymentApproved.getPhoneNumber());
                page.setAmt(paymentApproved.getAmt());
                
                SimpleDateFormat yyyyMMFormat = new SimpleDateFormat("yyyyMM");

                String yyyymm = yyyyMMFormat.format(paymentApproved.getCreateTime());
                
                page.setYyyymm(yyyymm);

                List<Sale> sales = saleRepository.findByPhoneNumberAndYyyymm(paymentApproved.getPhoneNumber(), yyyymm);
                Sale sale = sales.get(0);
                
                page.setSumAmt(sale.getSumAmt());
                
                // view 레파지 토리에 save
                salePageRepository.save(page);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_UPDATE_1(@Payload Ordered ordered) {
        try {
            if (ordered.isMe()) {
                // view 객체 조회
                List<SalePage> pages = salePageRepository.findByOrderId(ordered.getId());
                for(SalePage page : pages){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                	page.setProductName(ordered.getProductName());
                    // view 레파지 토리에 save
                	salePageRepository.save(page);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_2(@Payload PaymentCanceled paymentCanceled) {
        try {
            if (paymentCanceled.isMe()) {
            	
            	String yyyymm = paymentCanceled.getTimestamp().substring(0, 6);
            	List<Sale> sales = saleRepository.findByPhoneNumberAndYyyymm(paymentCanceled.getPhoneNumber(), yyyymm);
                
            	if(sales.size() !=  1) throw new RuntimeException("There is not exacted[" + yyyymm + " / " + paymentCanceled.getPhoneNumber() + "]");
            	Sale sale = sales.get(0);
            	sale.setSumAmt(sale.getSumAmt() - paymentCanceled.getAmt());
            	
            	saleRepository.save(sale);
                
                // view 객체 조회
            	List<SalePage> pages = salePageRepository.findByOrderId(paymentCanceled.getOrderId());
            	
            	SalePage beforePage = pages.get(0);
            	
            	SalePage page = new SalePage();
            	page.setOrderId(paymentCanceled.getOrderId());
            	page.setPhoneNumber(paymentCanceled.getPhoneNumber());
            	page.setProductName(beforePage.getProductName());
            	
                page.setYyyymm(paymentCanceled.getTimestamp().substring(0, 6));

                page.setSumAmt(sale.getSumAmt());
                
                salePageRepository.save(page);
            	
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}