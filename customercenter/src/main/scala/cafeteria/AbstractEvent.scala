package cafeteria

import java.text.SimpleDateFormat
import java.util.Date

import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.MimeTypeUtils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

import cafeteria.config.kafka.KafkaProcessor
import scala.beans.BeanProperty

class AbstractEvent {

  @BeanProperty
  val eventType: String = this.getClass().getSimpleName()
  
  @BeanProperty
  val timestamp: String = new SimpleDateFormat("YYYYMMddHHmmss").format(new Date());

  def toJson(): String = {

    val objectMapper = new ObjectMapper()

    try {
      val json = objectMapper.writeValueAsString(this)
      json
    } catch {
      case e: JsonProcessingException => throw new RuntimeException("JSON format exception", e)
    }
  }

  def publish(json: String) {
    if (json != null) {

      val processor: KafkaProcessor = CustomercenterApplication.applicationContext.getBean(classOf[KafkaProcessor])
      val outputChannel: MessageChannel = processor.outboundTopic()
      outputChannel.send(MessageBuilder
        .withPayload(json)
        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
        .build());
    }
  }

  def publish() {
    this.publish(this.toJson())
  }

  def publishAfterCommit() {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      
      override def afterCompletion(status: Int) {
        AbstractEvent.this.publish();
      }
    });
  }

  def isMe(): Boolean = {
    this.eventType.equals(getClass().getSimpleName())
  }

}