package cafeteria

import scala.beans.BeanProperty

class Ordered extends AbstractEvent {
  @BeanProperty
  var id :Long = 0L
  @BeanProperty
  var phoneNumber :String = null
  @BeanProperty
  var productName :String = null
  @BeanProperty
  var qty :Integer = 0
  @BeanProperty
  var amt :Integer = 0
  @BeanProperty
  var status :String = null
}