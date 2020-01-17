package util

import java.io.ByteArrayOutputStream
import java.time.temporal.ChronoUnit

import com.google.common.primitives.UnsignedLong
import org.interledger.codecs.ilp.InterledgerCodecContextFactory
import org.interledger.core.{DateUtils, InterledgerAddress, InterledgerConstants, InterledgerPreparePacket}
import org.slf4j.LoggerFactory

object Prepare {

  val logger = LoggerFactory.getLogger(getClass)

  def create(amount: UnsignedLong, destination: String): InterledgerPreparePacket = {
    val prepare = InterledgerPreparePacket.builder()
      .amount(amount)
      .destination(InterledgerAddress.of(destination))
      .executionCondition(InterledgerConstants.ALL_ZEROS_CONDITION)
      .expiresAt(DateUtils.now.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS))
      .build()
    logger.trace("[Prepare] Created prepare packet: {}", prepare)
    prepare
  }

  def createAndSerialize(amount: UnsignedLong, destination: String): Array[Byte] = {
    serialize(create(amount, destination))
  }

  def serialize(prepare: InterledgerPreparePacket): Array[Byte] = {
    val context = InterledgerCodecContextFactory.oer
    val outputStream = new ByteArrayOutputStream
    context.write(prepare, outputStream)
    outputStream.toByteArray
  }


}
