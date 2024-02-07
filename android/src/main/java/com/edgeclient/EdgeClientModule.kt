package com.edgeclient

import android.util.SparseArray
import java.util.Base64

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap

import com.nabto.edge.client.Connection
import com.nabto.edge.client.Coap
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.NabtoEOFException
import com.nabto.edge.client.NabtoNoChannelsException
import com.nabto.edge.client.NabtoRuntimeException

class EdgeClientModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  private val mNabtoClients = SparseArray<NabtoClient>()
  private val mConnections = SparseArray<Connection>()
  private val mCoapObjects = SparseArray<Coap>()

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "EdgeClient"
  }

  // ----------------------------------------------------------
  // Util functions
  // ----------------------------------------------------------
  private fun exceptionFromStatus(connection: Connection, ec: Int): Throwable {
    val swigException = com.nabto.edge.client.swig.NabtoException(ec)

    val result =
      when (ec) {
        ErrorCodes.END_OF_FILE -> NabtoEOFException(swigException)
        ErrorCodes.NO_CHANNELS ->
          NabtoNoChannelsException(
            connection.localChannelErrorCode.errorCode,
            connection.remoteChannelErrorCode.errorCode,
            connection.directCandidatesChannelErrorCode.errorCode
          )
        else -> NabtoRuntimeException(swigException)
      }

    return result
  }

  // ----------------------------------------------------------
  // Nabto Client methods
  // ----------------------------------------------------------
  @ReactMethod
  fun createNabtoClient(id: Int, promise: Promise) {
    val client = NabtoClient.create(reactContext)
    mNabtoClients.put(id, client)
    promise.resolve(null)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun clientGetVersion(clientId: Int): String {
    return mNabtoClients.get(clientId).version()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun clientCreatePrivateKey(clientId: Int): String {
    return mNabtoClients.get(clientId).createPrivateKey()
  }

  @ReactMethod
  fun clientCreateConnection(clientId: Int, connectionId: Int, promise: Promise) {
    val client = mNabtoClients.get(clientId)
    val connection = client.createConnection()
    mConnections.put(connectionId, connection)
    promise.resolve(null)
  }

  @ReactMethod
  fun clientClose(clientId: Int, promise: Promise) {
    mNabtoClients.remove(clientId)
    promise.resolve(null)
  }

  // ----------------------------------------------------------
  // Connection methods
  // ----------------------------------------------------------
  @ReactMethod(isBlockingSynchronousMethod = true)
  fun connectionUpdateOptions(connectionId: Int, options: String) {
    val conn = mConnections.get(connectionId)
    conn.updateOptions(options)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun connectionGetOptions(connectionId: Int): String {
    val conn = mConnections.get(connectionId)
    return conn.options
  }

  @ReactMethod
  fun connectionConnect(connectionId: Int, promise: Promise) {
    val conn = mConnections.get(connectionId)
    conn.connectCallback { error, _ ->
      if (error == ErrorCodes.OK) {
        promise.resolve(null)
      } else {
        promise.reject(exceptionFromStatus(conn, error))
      }
    }
  }

  @ReactMethod
  fun connectionClose(connectionId: Int, promise: Promise) {
    val conn = mConnections.get(connectionId)
    conn.close()
    mConnections.remove(connectionId)
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionCreateCoap(connectionId: Int, coapId: Int, method: String, path: String promise: Promise) {
    val conn = mConnections.get(connectionId)
    val coap = conn.createCoap(method, path)
    mCoapObjects.put(coapId, coap)
    promise.resolve(null)
  }

  // ----------------------------------------------------------
  // Coap methods
  // ----------------------------------------------------------
  @ReactMethod
  fun coapSetRequestPayload(coapId: Int, contentFormat: Int, payloadBase64: String) {
    mCoapObjects[coapId]?.let {
      val payload = Base64.getDecoder().decode(payloadBase64)
      it.setRequestPayload(contentFormat, payload)
    }
  }

  private fun getCoapObjectOrThrow(coapId: Int): Coap {
    if (mCoapObjects.contains(coapId)) {
      return mCoapObjects[coapId]
    } else {
      throw IllegalArgumentException("Coap object is invalid.")
    }
  }

  @ReactMethod
  fun coapExecute(coapId: Int, promise: Promise) {
    val coap = getCoapObjectOrThrow(coapId)
    coap.executeCallback { error, _ ->
      mCoapObjects.remove(coapId)

      if (error == ErrorCodes.OK) {
        val result = WritableNativeMap()
        result.putInt("responseStatusCode", coap.responseStatusCode)
        result.putInt("responseContentFormat", coap.responseContentFormat)

        if (coap.responsePayload != null) {
          val responsePayload = WritableNativeArray()
          coap.responsePayload.forEach { responsePayload.pushInt(it.toInt()) }
          result.putArray("responsePayload", responsePayload)
        }

        promise.resolve(result)
      } else {
        promise.reject(IllegalStateException(ErrorCode(error).description))
      }
    }
  }
}