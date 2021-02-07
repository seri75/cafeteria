package cafeteria.config.kafka

import org.springframework.messaging.SubscribableChannel
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.Input
import org.springframework.messaging.MessageChannel
import org.springframework.cloud.stream.annotation.EnableBinding

object KafkaProcessor {
  final val INPUT = "event-in"
  final val OUTPUT = "event-out"
}

trait KafkaProcessor {

  @Input(KafkaProcessor.INPUT)
  def inboundTopic() :SubscribableChannel

  @Output(KafkaProcessor.OUTPUT)
  def outboundTopic() :MessageChannel
}