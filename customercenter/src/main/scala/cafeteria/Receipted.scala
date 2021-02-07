package cafeteria

import scala.beans.BeanProperty

class Receipted extends AbstractEvent {
  @BeanProperty
  var id :Long = 0L
  @BeanProperty
  var orderId :Long = 0L
  @BeanProperty
  var phoneNumber :String = null
  @BeanProperty
  var status :String = null
}