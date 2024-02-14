package com.edgeclient

import android.util.SparseArray
import android.util.Log
import java.util.Base64

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule

import com.nabto.edge.client.Connection
import com.nabto.edge.client.Coap
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.Stream
import com.nabto.edge.client.TcpTunnel
import com.nabto.edge.client.MdnsScanner
import com.nabto.edge.client.ConnectionEventsCallback
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.NabtoEOFException
import com.nabto.edge.client.NabtoNoChannelsException
import com.nabto.edge.client.NabtoRuntimeException

data class ConnectionContext(
  val conn: Connection,
  val listener: ConnectionEventsCallback
)

class EdgeClientModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  private val mNabtoClients = SparseArray<NabtoClient>()
  private val mConnections = SparseArray<ConnectionContext>()
  private val mConnectionEventListeners = SparseArray<ConnectionEventsCallback>()
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

  private fun getConnection(connectionId: Int): Connection {
    return mConnections[connectionId]?.conn ?: throw IllegalArgumentException("Invalid connection!")
  }

  private fun sendEvent(eventName: String, eventParams: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, eventParams)
  }

  // NOTE: addListener and removeListener are here because react-native will log the following warning without (but still works regardless)
  // `new NativeEventEmitter()` was called with a non-null argument without the required `addListener` method. 

  @ReactMethod
  fun addListener(@Suppress("UNUSED_PARAMETER") eventName: String) {
    // Empty on purpose
  }

  @ReactMethod
  fun removeListeners(@Suppress("UNUSED_PARAMETER") count: Int) {
    // Empty on purpose
  }

  // ----------------------------------------------------------
  // Nabto Client methods
  // ----------------------------------------------------------
  @ReactMethod
  fun createNabtoClient(id: Double, promise: Promise) {
    val client = NabtoClient.create(reactContext)
    mNabtoClients.put(id.toInt(), client)
    promise.resolve(null)
  }

  @ReactMethod
  fun clientGetVersion(clientId: Double, promise: Promise) {
    promise.resolve(mNabtoClients[clientId.toInt()].version())
  }

  @ReactMethod
  fun clientSetLogLevel(clientId: Double, level: String, promise: Promise) {
    mNabtoClients[clientId.toInt()].setLogLevel(level)
    promise.resolve(null)
  }

  @ReactMethod
  fun clientCreatePrivateKey(clientId: Double, promise: Promise) {
    promise.resolve(mNabtoClients[clientId.toInt()].createPrivateKey())
  }

  @ReactMethod
  fun clientCreateConnection(clientId: Double, connectionId: Double, promise: Promise) {
    val client = mNabtoClients.get(clientId.toInt())

    val listener = object : ConnectionEventsCallback() {
      override fun onEvent(event: Int) {
        val params = Arguments.createMap().apply {
          putDouble("event", event.toDouble())
        }
        sendEvent("ConnectionOnEvent#${connectionId}", params)
      }
    }
    
    val connection = client.createConnection()
    connection.addConnectionEventsListener(listener)

    mConnections.put(connectionId.toInt(), ConnectionContext(connection, listener))
    promise.resolve(null)
  }

  @ReactMethod
  fun createMdnsScanner(clientId: Double, scannerId: Double, subtype: String, promise: Promise) {
    val client = mNabtoClients[clientId.toInt()]
    val scanner = client.createMdnsScanner(subtype)

    scanner.addMdnsResultReceiver { result ->
      val params = Arguments.createMap().apply {
        putDouble("action", result.action.ordinal.toDouble())
        putString("deviceId", result.deviceId)
        putString("productId", result.productId)
        putString("serviceInstanceName", result.serviceInstanceName)
        
        val txtItems = Arguments.createMap().apply {
          result.txtItems.forEach { putString(it.key, it.value) }
        }

        putMap("txtItems", txtItems)
      }

      sendEvent("ScannerOnResult#${scannerId}", params)
    }

    mMdnsScanners.put(scannerId.toInt(), scanner)
    promise.resolve(null)
  }

  @ReactMethod
  fun clientDispose(clientId: Double, promise: Promise) {
    mNabtoClients[clientId.toInt()].close()
    mNabtoClients.remove(clientId.toInt())
    promise.resolve(null)
  }

  // ----------------------------------------------------------
  // MdnsScanner methods
  // ----------------------------------------------------------

  @ReactMethod
  fun mdnsScannerStart(scannerId: Double, promise: Promise) {
    val scanner = mMdnsScanners[scannerId.toInt()]
    scanner.start()
    promise.resolve(null)
  }

  @ReactMethod
  fun mdnsScannerStop(scannerId: Double, promise: Promise) {
    val scanner = mMdnsScanners[scannerId.toInt()]
    scanner.stop()
    promise.resolve(null)
  }

  @ReactMethod
  fun mdnsScannerIsStarted(scannerId: Double, promise: Promise) {
    val scanner = mMdnsScanners[scannerId.toInt()]
    promise.resolve(scanner.isStarted)
  }

  // ----------------------------------------------------------
  // Connection methods
  // ----------------------------------------------------------
  @ReactMethod
  fun connectionUpdateOptions(connectionId: Double, options: String, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.updateOptions(options)
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionGetOptions(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.options)
  }

  @ReactMethod
  fun connectionConnect(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.connectCallback { error, _ ->
      if (error == ErrorCodes.OK) {
        promise.resolve(null)
      } else {
        promise.reject(exceptionFromStatus(conn, error))
      }
    }
  }

  @ReactMethod
  fun connectionCreateStream(connectionId: Double, streamId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    val stream = conn.createStream()
    mStreams.put(streamId.toInt(), stream)
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionCreateCoap(connectionId: Double, coapId: Double, method: String, path: String, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    val coap = conn.createCoap(method, path)
    mCoapObjects.put(coapId.toInt(), coap)
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionCreateTcpTunnel(connectionId: Double, tcpTunnelId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    val tunnel = conn.createTcpTunnel()
    mTcpTunnels.put(tcpTunnelId.toInt(), tunnel)
    promise.resolve(null)
  }
  
  @ReactMethod
  fun connectionGetDeviceFingerprint(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.deviceFingerprint)
  }

  @ReactMethod
  fun connectionGetClientFingerprint(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.clientFingerprint)
  }

  @ReactMethod
  fun connectionGetType(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.type.ordinal)
  }

  @ReactMethod
  fun connectionEnableDirectCandidates(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.enableDirectCandidates()
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionAddDirectCandidate(connectionId: Double, host: String, port: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.addDirectCandidate(host, port.toUInt().toInt())
    promise.resolve(null) 
  }

  @ReactMethod
  fun connectionEndOfDirectCandidates(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.endOfDirectCandidates()
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionDispose(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    mConnections.remove(connectionId.toInt())
    conn.close()
    promise.resolve(null)
  }

  @ReactMethod
  fun connectionGetLocalChannelErrorCode(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.localChannelErrorCode.errorCode)   
  }

  @ReactMethod
  fun connectionGetRemoteChannelErrorCode(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.remoteChannelErrorCode.errorCode)
  }

  @ReactMethod
  fun connectionGetDirectCandidatesChannelErrorCode(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    promise.resolve(conn.directCandidatesChannelErrorCode.errorCode)
  }

  @ReactMethod
  fun connectionPasswordAuthenticate(connectionId: Double, username: String, password: String, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.passwordAuthenticateCallback(username, password) { error, _ ->
      if (error == ErrorCodes.OK) {
        promise.resolve(null)
      } else {
        promise.reject(exceptionFromStatus(conn, error))
      }
    }  
  }

  @ReactMethod
  fun connectionClose(connectionId: Double, promise: Promise) {
    val conn = getConnection(connectionId.toInt())
    conn.connectionClose()
    promise.resolve(null)
  }

  // ----------------------------------------------------------
  // Stream methods
  // ----------------------------------------------------------
  @ReactMethod
  fun streamOpen(streamId: Double, streamPort: Double, promise: Promise) {
    val stream = mStreams[streamId.toInt()]
    stream.openCallback(streamPort.toUInt().toInt()) { error, _ ->
      if (error == ErrorCodes.OK) {
        promise.resolve(null)
      } else {
        promise.reject(IllegalStateException(ErrorCode(error).description))
      }
    }
  }

  @ReactMethod
  fun streamReadSome(streamId: Double, promise: Promise) {
    val stream = mStreams[streamId.toInt()]
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
  fun streamReadAll(streamId: Double, length: Double, promise: Promise) {
    val stream = mStreams[streamId.toInt()]
    stream.readAllCallback(length.toInt()) { error, maybeBytes ->
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
  fun streamWrite(streamId: Double, bytesBase64: String, promise: Promise) {
    val stream = mStreams[streamId.toInt()]
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
  fun streamClose(streamId: Double, promise: Promise) {
    val stream = mStreams[streamId.toInt()]
    stream.streamClose()
    promise.resolve(null)
  }

  @ReactMethod
  fun streamDispose(streamId: Double, promise: Promise) {
    val stream = mStreams[streamId.toInt()]
    stream.close()
    mStreams.remove(streamId.toInt())
    promise.resolve(null)
  }

  // ----------------------------------------------------------
  // Coap methods
  // ----------------------------------------------------------
  @ReactMethod
  fun coapSetRequestPayload(coapId: Double, contentFormat: Double, payloadBase64: String) {
    mCoapObjects[coapId.toInt()]?.let {
      val payload = Base64.getDecoder().decode(payloadBase64)
      it.setRequestPayload(contentFormat.toInt(), payload)
    }
  }

  private fun getCoapObjectOrThrow(coapId: Double): Coap {
    if (mCoapObjects.contains(coapId.toInt())) {
      return mCoapObjects[coapId.toInt()]
    } else {
      throw IllegalArgumentException("Coap object is invalid.")
    }
  }

  @ReactMethod
  fun coapExecute(coapId: Double, promise: Promise) {
    val coap = getCoapObjectOrThrow(coapId)
    coap.executeCallback { error, _ ->
      mCoapObjects.remove(coapId.toInt())

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
  fun tcpTunnelOpen(tcpTunnelId: Double, service: String, localPort: Double, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId.toInt()]
    tunnel.open(service, localPort.toUInt().toInt())
    promise.resolve(null)
  }

  @ReactMethod
  fun tcpTunnelGetLocalPort(tcpTunnelId: Double, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId.toInt()]
    promise.resolve(tunnel.localPort)
  }

  @ReactMethod
  fun tcpTunnelClose(tcpTunnelId: Double, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId.toInt()]
    tunnel.tunnelClose()
    promise.resolve(null)
  }

  @ReactMethod
  fun tcpTunnelDispose(tcpTunnelId: Double, promise: Promise) {
    val tunnel = mTcpTunnels[tcpTunnelId.toInt()]
    tunnel.close()
    mTcpTunnels.remove(tcpTunnelId.toInt())
    promise.resolve(null)
  }
}
