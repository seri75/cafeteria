package cafeteria

import scala.beans.BeanProperty

class PaymentCanceled extends AbstractEvent {
  @BeanProperty
  var id: Long = 0L
  @BeanProperty
  var orderId :Long = 0L
  @BeanProperty
  var phoneNumber :String = null
  @BeanProperty
  var amt :Int = 0
  @BeanProperty
  var status: String = null
  
}
