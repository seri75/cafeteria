package cafeteria

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import cafeteria.external.KakaoService
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Payload
import cafeteria.config.kafka.KafkaProcessor
import cafeteria.external.KakaoMessage
import scala.collection.JavaConverters._
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.FindAndModifyOptions

@Service
class MypageViewHandler {
  
  @Autowired
	private val kakaoService :KakaoService = null
	
  @Autowired
  private val mypageRepository :MypageRepository = null
  
  @Autowired
  private val mongoOperations :MongoOperations = null
  
  def generateSequence (seqName :String) :Long = {
    val query :Query = new Query(Criteria.where("_id").is(seqName));
    val options :FindAndModifyOptions = new FindAndModifyOptions().returnNew(true).upsert(true)
    val update :Update = new Update().inc("seq",1)
    
    val sequence :Option[DatabaseSequence] = Option(mongoOperations.findAndModify(query, update, options, classOf[DatabaseSequence]))
    sequence.getOrElse(InitialSequence(1L)).seq
}
  
  @StreamListener(KafkaProcessor.INPUT)
  def whenOrdered_then_CREATE_1(@Payload ordered :Ordered) {
    try {
      if (ordered.isMe()) {
        
        val mypage :Mypage = new Mypage()
        mypage.id = generateSequence(Mypage.SEQUENCE_NAME)
        mypage.orderId = ordered.id
        mypage.productName = ordered.productName
        mypage.qty = ordered.qty
        mypage.amt = ordered.amt
        mypage.phoneNumber = ordered.phoneNumber
        mypage.status = ordered.status
        
        mypageRepository.save(mypage)
        
        val message :KakaoMessage = new KakaoMessage()
        message.phoneNumber = ordered.phoneNumber
        message.message = s"""Your Order is ${ordered.status}"""
        kakaoService.sendKakao(message)
      }
    } catch {
      case e :Exception => e.printStackTrace()
    }
  }

  @StreamListener(KafkaProcessor.INPUT)
  def whenOrderCanceled_then_UPDATE_1(@Payload orderCanceled :OrderCanceled) {
    try {
      if (orderCanceled.isMe()) {
        mypageRepository.findByOrderId(orderCanceled.id).asScala.foreach(page => {
          page.status = orderCanceled.status
          mypageRepository.save(page)
        })
      
        val message :KakaoMessage = new KakaoMessage()
        message.phoneNumber = orderCanceled.phoneNumber
        message.message = s"""Your Order is ${orderCanceled.status}"""
        kakaoService.sendKakao(message)
      }
    } catch {
      case e :Exception => e.printStackTrace()
    }
  }
  
  @StreamListener(KafkaProcessor.INPUT)
  def whenReceipted_then_UPDATE_2(@Payload receipted :Receipted) {
    try {
      if (receipted.isMe()) {
        mypageRepository.findByOrderId(receipted.orderId).asScala.foreach(page => {
          page.status = receipted.status
          mypageRepository.save(page)
        })
      
        val message :KakaoMessage = new KakaoMessage()
        message.phoneNumber = receipted.phoneNumber
        message.message = s"""Your Order is ${receipted.status}"""
        kakaoService.sendKakao(message)
      }
    } catch {
      case e :Exception => e.printStackTrace()
    }
  }
  
  @StreamListener(KafkaProcessor.INPUT)
  def whenReceipted_then_UPDATE_3(@Payload made :Made) {
    try {
      if (made.isMe()) {
        mypageRepository.findByOrderId(made.orderId).asScala.foreach(page => {
          page.status = made.status
          mypageRepository.save(page)
        })
      
        val message :KakaoMessage = new KakaoMessage()
        message.phoneNumber = made.phoneNumber
        message.message = s"""Your Order is ${made.status}\nCome and Take it, Please!"""
        kakaoService.sendKakao(message)
      }
    } catch {
      case e :Exception => e.printStackTrace()
    }
  }
   
}