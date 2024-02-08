package com.edgeclient

import android.util.SparseArray
import java.util.Base64

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.bridge.ReadableArray

import com.nabto.edge.client.Connection
import com.nabto.edge.client.Coap
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.Stream
import com.nabto.edge.client.TcpTunnel
import com.nabto.edge.client.MdnsScanner
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.NabtoEOFException
import com.nabto.edge.client.NabtoNoChannelsException
import com.nabto.edge.client.NabtoRuntimeException

class EdgeClientModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  private val mNabtoClients = SparseArray<NabtoClient>()
  private val mConnections = SparseArray<Connection>()
  private val mStreams = SparseArray<Stream>()
  private val mCoapObjects = SparseArray<Coap>()
  private val mTcpTunnels = SparseArray<TcpTunnel>()
  private val mMdnsScanners = SparseArray<MdnsScanner>()

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

  @ReactMethod
  fun clientSetLogLevel(clientId: Int, level: String, promise: Promise) {
    mNabtoClients[clientId].setLogLevel(level)
    promise.resolve(null)
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
  fun createMdnsScanner(clientId: Int, scannerId: Int, subtype: String, promise: Promise) {
    val client = mNabtoClients[clientId]
    val scanner = client.createMdnsScanner(subtype)
    mMdnsScanners.put(scannerId, scanner)
    promise.resolve(null)
  }

  @ReactMethod
  fun clientDispose(clientId: Int, promise: Promise) {
    mNabtoClients[clientId].close()
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
  fun connectionCreateStream(connectionId: Int, streamId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    val stream = conn.createStream()
    mStreams.put(streamId, stream)
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionCreateCoap(connectionId: Int, coapId: Int, method: String, path: String, promise: Promise) {
    val conn = mConnections.get(connectionId)
    val coap = conn.createCoap(method, path)
    mCoapObjects.put(coapId, coap)
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionCreateTcpTunnel(connectionId: Int, tcpTunnelId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    val tunnel = conn.createTcpTunnel()
    mTcpTunnels.put(tcpTunnelId, tunnel)
    promise.resolve(null)
  }
  
  @ReactMethod
  fun connectionGetDeviceFingerprint(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    promise.resolve(conn.deviceFingerprint)
  }

  @ReactMethod
  fun connectionGetClientFingerprint(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    promise.resolve(conn.clientFingerprint)
  }

  @ReactMethod
  fun connectionGetType(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    promise.resolve(conn.type.ordinal)
  }

  @ReactMethod
  fun connectionEnableDirectCandidates(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    conn.enableDirectCandidates()
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionAddDirectCandidate(connectionId: Int, host: String, port: Int, promise: Promise) {
    val conn = mConnections[connectionId]     
    conn.addDirectCandidate(host, port)
    promise.resolve(null) 
  }

  @ReactMethod
  fun connectionEndOfDirectCandidates(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    conn.endOfDirectCandidates()
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionDispose(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    mConnections.remove(connectionId)
    conn.close()
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionGetLocalChannelErrorCode(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]   
    promise.resolve(conn.localChannelErrorCode.errorCode)   
  }

  @ReactMethod
  fun connectionGetRemoteChannelErrorCode(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    promise.resolve(conn.remoteChannelErrorCode.errorCode)
  }

  @ReactMethod
  fun connectionGetDirectCandidatesChannelErrorCode(connectionId: Int, promise: Promise) {
    val conn = mConnections[connectionId]
    promise.resolve(conn.directCandidatesChannelErrorCode.errorCode)
  }

  @ReactMethod
  fun connectionPasswordAuthenticate(connectionId: Int, username: String, password: String, promise: Promise) {
    val conn = mConnections[connectionId]
    conn.passwordAuthenticateCallback(username, password) { error, _ ->
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
    conn.connectionClose()
    promise.resolve(null)
  }

  // ----------------------------------------------------------
  // Stream methods
  // ----------------------------------------------------------
  @ReactMethod
  fun streamOpen(streamId: Int, streamPort: Int, promise: Promise) {
    val stream = mStreams[streamId]
    stream.openCallback(streamPort) { error, _ ->
      if (error == ErrorCodes.OK) {
        promise.resolve(null)
      } else {
        promise.reject(IllegalStateException(ErrorCode(error).description))
      }
    }
  }

  @ReactMethod
  fun streamReadSome(streamId: Int, promise: Promise) {
    val stream = mStreams[streamId]
    stream.readSomeCallback { error, maybeBytes ->
      if (error == ErrorCodes.OK && maybeBytes.isPresent()) {
        val bytes = maybeBytes.get()
        val result = WritableNativeArray()
        bytes.forEach { result.pushInt(it.toInt()) }
        promise.resolve(result)
      } else {
        promise.reject(IllegalStateException(ErrorCode(error).description))
      }
    }
  }

  @ReactMethod
  fun streamReadAll(streamId: Int, length: Int, promise: Promise) {
    val stream = mStreams[streamId]
    stream.readAllCallback(length) { error, maybeBytes ->
      if (error == ErrorCodes.OK && maybeBytes.isPresent()) {
        val bytes = maybeBytes.get()
        val result = WritableNativeArray()
        bytes.forEach { result.pushInt(it.toInt()) }
        promise.resolve(result)
      } else {
        promise.reject(IllegalStateException(ErrorCode(error).description))
      }
    }
  }

  @ReactMethod
  fun streamWrite(streamId: Int, bytesBase64: String, promise: Promise) {
    val stream = mStreams[streamId]
    val bytes = Base64.getDecoder().decode(bytesBase64)
    stream.writeCallback(bytes) { error, _ ->
      if (error == ErrorCodes.OK) {
        promise.resolve(null)
      } else {
        promise.reject(IllegalStateException(ErrorCode(error).description))
      }
    }
  }

  @ReactMethod
  fun streamClose(streamId: Int, promise: Promise) {
    val stream = mStreams[streamId]
    stream.streamClose()
    promise.resolve(null)
  }

  @ReactMethod
  fun streamDispose(streamId: Int, promise: Promise) {
    val stream = mStreams[streamId]
    stream.close()
    mStreams.remove(streamId)
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

  // ----------------------------------------------------------
  // Tcp Tunnel methods
  // ----------------------------------------------------------
  @ReactMethod
  fun tcpTunnelOpen(tcpTunnelId: Int, service: String, localPort: Int, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId]
    tunnel.open(service, localPort)
    promise.resolve(null)
  }

  @ReactMethod
  fun tcpTunnelGetLocalPort(tcpTunnelId: Int, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId]
    promise.resolve(tunnel.localPort)
  }

  @ReactMethod
  fun tcpTunnelClose(tcpTunnelId: Int, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId]
    tunnel.tunnelClose()
    promise.resolve(null)
  }

  @ReactMethod
  fun tcpTunnelDispose(tcpTunnelId: Int, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId]
    tunnel.close()
    mTcpTunnels.remove(tcpTunnelId)
    promise.resolve(null)
  }
}
