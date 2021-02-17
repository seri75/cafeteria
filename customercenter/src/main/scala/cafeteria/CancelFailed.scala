package cafeteria

import scala.beans.BeanProperty

class CancelFailed extends AbstractEvent {
  @BeanProperty
  var orderId :Long = 0L
  
  @BeanProperty
  var phoneNumber :String = null
}
