package util

import java.io.ByteArrayOutputStream
import java.time.temporal.ChronoUnit
import java.util.Random

import com.google.common.primitives.UnsignedLong
import org.interledger.codecs.ilp.InterledgerCodecContextFactory
import org.interledger.core.{DateUtils, InterledgerAddress, InterledgerCondition, InterledgerConstants, InterledgerPreparePacket}

object Prepare {

  def create(amount: UnsignedLong, destination: String): InterledgerPreparePacket = {
    
    InterledgerPreparePacket.builder()
      .amount(amount)
      .destination(InterledgerAddress.of(destination))
      .executionCondition(InterledgerConstants.ALL_ZEROS_CONDITION)
      .expiresAt(DateUtils.now.plusSeconds(30)truncatedTo(ChronoUnit.MILLIS))
      .build()
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
