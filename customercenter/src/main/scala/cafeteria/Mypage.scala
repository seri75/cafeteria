package cafeteria

import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient

object Mypage {
  val SEQUENCE_NAME :String = "mypage_sequence"  
}

@Document
class Mypage {
  
  @Id
  @BeanProperty
  var id :Long = 0L
  
  @BeanProperty
  var orderId :Long = 0L
  
  @BeanProperty
  var productName :String = null
  
  @BeanProperty
  var qty :Integer = 0
  
  @BeanProperty
  var amt :Integer = 0
  
  @BeanProperty
  var phoneNumber :String = null
  
  @BeanProperty
  var status :String = null
}
