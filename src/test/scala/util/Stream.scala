package util

import java.io.{ByteArrayOutputStream, IOException}
import java.util
import java.util.Objects
import java.util.concurrent.atomic.AtomicLong

import com.google.common.collect.Lists
import com.google.common.primitives.UnsignedLong
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import org.interledger.codecs.stream.StreamCodecContextFactory
import org.interledger.core._
import org.interledger.stream.StreamUtils.generatedFulfillableFulfillment
import org.interledger.stream.crypto.JavaxStreamEncryptionService
import org.interledger.stream.frames.{ConnectionAssetDetailsFrame, ConnectionNewAddressFrame, StreamFrame, StreamMoneyFrame}
import org.interledger.stream.sender.StreamSenderException
import org.interledger.stream.{Denomination, StreamPacket}
import org.slf4j.LoggerFactory

object Stream {

  val logger = LoggerFactory.getLogger(getClass)

  val streamCodecContext = StreamCodecContextFactory.oer

  val streamEncryptionService = new JavaxStreamEncryptionService

  val sequence = new AtomicLong(0L)

  def nextSequence(): UnsignedLong = UnsignedLong.valueOf(sequence.incrementAndGet)

  def preflightCheck(senderAccountName: String, bearer: String, sharedSecret: SharedSecret, destination: InterledgerAddress,
                     senderDenomination: Denomination): HttpRequestBuilder = {
    val frames: util.List[StreamFrame] = Lists.newArrayList(
      StreamMoneyFrame.builder.streamId(UnsignedLong.ONE).shares(UnsignedLong.ONE).build, // This aggregator supports only a simple stream-id, which is one.
      ConnectionNewAddressFrame.builder.sourceAddress(InterledgerAddress.of(s"""${Config.spspRoutePrefix}.${senderAccountName}""")).build,
      ConnectionAssetDetailsFrame.builder.sourceDenomination(senderDenomination).build
    )

    val streamPacket: StreamPacket = StreamPacket.builder
      .interledgerPacketType(InterledgerPacketType.PREPARE)
      .prepareAmount(UnsignedLong.ZERO)
      .sequence(nextSequence)
      .frames(frames)
      .build

    val streamPacketData: Array[Byte] = this.toEncrypted(sharedSecret, streamPacket)
    val executionCondition: InterledgerCondition = generatedFulfillableFulfillment(sharedSecret, streamPacketData).getCondition

    val prepare = InterledgerPreparePacket.builder
      .destination(destination)
      .amount(UnsignedLong.ZERO)
      .executionCondition(executionCondition)
      .expiresAt(DateUtils.now.plusSeconds(30L))
      .data(streamPacketData:_*)
      .build

    logger.trace("[Stream Requests] Sending preflight check {}", prepare.toString)
    val requestName = s"""[Connector] Send Stream preflight from ${senderAccountName} to ${prepare.getDestination.getValue}"""
    http(requestName)
      .post("/accounts/" + senderAccountName + "/ilp")
      .header("accept", "application/octet-stream")
      .header("content-type", "application/octet-stream")
      .header("Authorization", "Bearer " + bearer)
      .body(ByteArrayBody(Prepare.serialize(prepare)))
      .check(status.is(200))
      .check(bodyBytes.exists)
      .transformResponse(Transformers.convertIlpResponseToJson)
  }

  def sendStreamPacket(accountName: String, bearer: String, sharedSecret: SharedSecret, amount: UnsignedLong,
                       destination: InterledgerAddress): HttpRequestBuilder = {
    val frames = Lists.newArrayList(StreamMoneyFrame.builder.streamId // This aggregator supports only a simple stream-id, which is one.
    (UnsignedLong.ONE).shares(UnsignedLong.ONE).build)

    val streamPacket = StreamPacket.builder
      .interledgerPacketType(InterledgerPacketType.PREPARE)
      .prepareAmount(UnsignedLong.ZERO)
      .sequence(nextSequence)
      .frames(frames)
      .build

    // Create the ILP Prepare packet
    val streamPacketData = this.toEncrypted(sharedSecret, streamPacket)
    val executionCondition = generatedFulfillableFulfillment(sharedSecret, streamPacketData).getCondition

    val prepare = InterledgerPreparePacket.builder
      .destination(destination)
      .amount(amount)
      .executionCondition(executionCondition)
      .expiresAt(DateUtils.now.plusSeconds(30L))
      .data(streamPacketData:_*)
      .build

    val requestName = s"""[Connector] Send Stream packet from ${accountName} to ${prepare.getDestination.getValue}"""
    http(requestName)
      .post("/accounts/" + accountName + "/ilp")
      .header("accept", "application/octet-stream")
      .header("content-type", "application/octet-stream")
      .header("Authorization", "Bearer " + bearer)
      .body(ByteArrayBody(Prepare.serialize(prepare)))
      .check(status.is(200))
      .check(bodyBytes.exists)
      .transformResponse(Transformers.convertIlpResponseToJson)
  }

  private def toEncrypted(sharedSecret: SharedSecret, streamPacket: StreamPacket) = {
    Objects.requireNonNull(sharedSecret)
    Objects.requireNonNull(streamPacket)
    try {
      val baos = new ByteArrayOutputStream
      streamCodecContext.write(streamPacket, baos)
      val streamPacketBytes = baos.toByteArray
      streamEncryptionService.encrypt(sharedSecret, streamPacketBytes)
    } catch {
      case e: IOException =>
        throw new StreamSenderException(e.getMessage, e)
    }
  }


}
