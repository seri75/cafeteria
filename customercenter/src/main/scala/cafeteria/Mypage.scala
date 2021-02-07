package cafeteria

import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.GenerationType
import scala.beans.BeanProperty

@Entity
@Table(name="Mypage")
class Mypage {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
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
