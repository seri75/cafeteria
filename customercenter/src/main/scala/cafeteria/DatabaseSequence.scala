package cafeteria

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import scala.beans.BeanProperty

abstract class Sequence {
  var seq :Long
}
case class InitialSequence(var seq : Long) extends Sequence

@Document(collection = "database_sequences")
class DatabaseSequence extends Sequence {

  
  @BeanProperty
  @Id
  var id: String = null
  
  @BeanProperty
  var seq :Long = 0L

}