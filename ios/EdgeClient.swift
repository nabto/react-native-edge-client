import NabtoEdgeClient

@objc(EdgeClient)
class EdgeClient: NSObject {
    let reject_tag = "nabto_bridge_failure"

    var mNabtoClients: [Int: Client] = [:]
    var mConnections: [Int: Connection] = [:]
    var mCoapObjects: [Int: CoapRequest] = [:]
    var mStreams: [Int: NabtoEdgeClient.Stream] = [:]
    var mTcpTunnels: [Int: TcpTunnel] = [:]
    
    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }

    private func getConnection(_ id: Double) -> Connection {
        return mConnections[Int(id)]!
    }

    private func getStream(_ id: Double) -> NabtoEdgeClient.Stream {
        return mStreams[Int(id)]!
    }

    private func getTunnel(_ id: Double) -> TcpTunnel {
        return mTcpTunnels[Int(id)]!
    }

    // ----------------------------------------------------------
    // Nabto Client methods
    // ----------------------------------------------------------
    @objc(createNabtoClient:withResolver:withRejecter:)
    func createNabtoClient(clientId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        let client = Client()
        mNabtoClients.updateValue(client, forKey: Int(clientId))
        resolve(nil)
    }
    
    @objc(clientGetVersion:withResolver:withRejecter:)
    func clientGetVersion(clientId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        resolve(Client.versionString())
    }
    
    @objc(clientSetLogLevel:withLevel:withResolver:withRejecter:)
    func clientSetLogLevel(clientId: Double, level: NSString, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        do {
            try mNabtoClients[Int(clientId)]?.setLogLevel(level: level as String)
            resolve(nil)
        } catch {
            reject(nil, nil, error)
        }
    }
    
    @objc(clientCreatePrivateKey:withResolver:withRejecter:)
    func clientCreatePrivateKey(clientId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        do {
            let pk = try mNabtoClients[Int(clientId)]?.createPrivateKey()
            if let pk = pk {
                resolve(pk)
            } else {
                reject(reject_tag, "Client with id \(clientId) does not exist.", nil)
            }
        } catch {
            reject(nil, nil, error)
        }
    }

  @objc(clientCreateConnection:withConnectionId:withResolver:withRejecter:)
  func clientCreateConnection(clientId: Double, connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    guard let client = mNabtoClients[Int(clientId)] else {
        reject(reject_tag, "Client with id \(clientId) does not exist.", nil)
        return
    }

    do {
        let connection = try client.createConnection()
        mConnections.updateValue(connection, forKey: Int(connectionId))
        resolve(nil)
    } catch {
        reject(nil, nil, error)
    }
  }

  // ----------------------------------------------------------
  // Connection methods
  // ----------------------------------------------------------

  @objc(connectionCreateStream:withStreamId:withResolver:withRejecter:)
  func connectionCreateStream(connectionId: Double, streamId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let stream = try conn.createStream()
        mStreams.updateValue(stream, forKey: Int(streamId))
        resolve(nil)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionCreateCoap:withCoapId:withMethod:withPath:withResolver:withRejecter:)
  func connectionCreateCoap(connectionId: Double, coapId: Double, method: NSString, path: NSString, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let coap = try conn.createCoapRequest(method: method as String, path: path as String)
        mCoapObjects.updateValue(coap, forKey: Int(coapId))
        resolve(nil)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionCreateTcpTunnel:withTunnelId:withResolver:withRejecter:)
  func connectionCreateTcpTunnel(connectionId: Double, tcpTunnelId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let tunnel = try conn.createTcpTunnel()
        mTcpTunnels.updateValue(tunnel, forKey: Int(tcpTunnelId))
        resolve(nil)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionUpdateOptions:withOptions:withResolver:withRejecter:)
  func connectionUpdateOptions(connectionId: Double, options: NSString, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        try conn.updateOptions(json: options as String)
        resolve(nil)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionGetOptions:withResolver:withRejecter:)
  func connectionGetOptions(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let opts = try conn.getOptions()
        resolve(opts)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionConnect:withResolver:withRejecter:)
  func connectionConnect(connectionId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    conn.connectAsync { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(connectionGetDeviceFingerprint:withResolver:withRejecter:)
  func connectionGetDeviceFingerprint(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let result = try conn.getDeviceFingerprintHex()
        resolve(result)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionGetClientFingerprint:withResolver:withRejecter:)
  func connectionGetClientFingerprint(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let result = try conn.getClientFingerprintHex()
        resolve(result)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionGetType:withResolver:withRejecter:)
  func connectionGetType(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    do {
        let result = try conn.getType()
        resolve(result)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(connectionEnableDirectCandidates:withResolver:withRejecter:)
  func connectionEnableDirectCandidates(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    // @TODO: Warning log
    resolve(nil)
  }

  @objc(connectionAddDirectCandidate:withHost:withPort:withResolver:withRejecter:)
  func connectionAddDirectCandidate(connectionId: Double, host: NSString, port: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    // @TODO: Warning log
    resolve(nil)
  }

  @objc(connectionEndOfDirectCandidates:withResolver:withRejecter:)
  func connectionEndOfDirectCandidates(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    // @TODO: Warning log
    resolve(nil)
  }

  @objc(connectionGetLocalChannelErrorCode:withResolver:withRejecter:)
  func connectionGetLocalChannelErrorCode(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    // @TODO: Deprecate on Android as well?
    resolve(nil)
  }

  @objc(connectionGetRemoteChannelErrorCode:withResolver:withRejecter:)
  func connectionGetRemoteChannelErrorCode(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    // @TODO: Deprecate on Android as well?
    resolve(nil)
  }

  @objc(connectionGetDirectCandidatesChannelErrorCode:withResolver:withRejecter:)
  func connectionGetDirectCandidatesChannelErrorCode(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    // @TODO: Deprecate on Android as well?
    resolve(nil)
  }

  @objc(connectionPasswordAuthenticate:withUsername:withPassword:withResolver:withRejecter:)
  func connectionPasswordAuthenticate(connectionId: Double, username: NSString, password: NSString, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    conn.passwordAuthenticateAsync(username: username as String, password: password as String) { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }  
  }

  @objc(connectionClose:withResolver:withRejecter:)
  func connectionClose(connectionId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let conn = getConnection(connectionId)
    conn.closeAsync { error in 
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(connectionDispose:withResolver:withRejecter:)
  func connectionDispose(connectionId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    mConnections.removeValue(forKey: Int(connectionId))
    resolve(nil)
  }

  // ----------------------------------------------------------
  // Stream methods
  // ----------------------------------------------------------
  @objc(streamOpen:withStreamPort:withResolver:withRejecter:)
  func streamOpen(streamId: Double, streamPort: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let stream = getStream(streamId)
    let port32 = UInt32(streamPort)
    stream.openAsync(streamPort: port32) { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(streamReadSome:withResolver:withRejecter:)
  func streamReadSome(streamId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let stream = getStream(streamId)
    stream.readSomeAsync { error, data in
        if error == NabtoEdgeClientError.OK {
            resolve([UInt8](data!))
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(streamReadAll:withLength:withResolver:withRejecter:)
  func streamReadAll(streamId: Double, length: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let stream = getStream(streamId)
    stream.readAllAsync(length: Int(length)) { error, data in
        if error == NabtoEdgeClientError.OK {
            resolve([UInt8](data!))
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(streamWrite:withBytes:withResolver:withRejecter:)
  func streamWrite(streamId: Double, bytesBase64: NSString, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let stream = getStream(streamId)
    let data = Data(base64Encoded: bytesBase64 as String, options: .ignoreUnknownCharacters)!
    stream.writeAsync(data: data) { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(streamClose:withResolver:withRejecter:)
  func streamClose(streamId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let stream = getStream(streamId)
    stream.closeAsync { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(streamDispose:withResolver:withRejecter:)
  func streamDispose(streamId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    mStreams.removeValue(forKey: Int(streamId))
    resolve(nil)
  }

  // ----------------------------------------------------------
  // Coap methods
  // ----------------------------------------------------------
  @objc(coapSetRequestPayload:withContentFormat:withPayload:withResolver:withRejecter:)
  func coapSetRequestPayload(coapId: Double, contentFormat: Double, payloadBase64: NSString, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    guard let request = mCoapObjects[Int(coapId)] else {
        reject(reject_tag, "Coap object with id \(coapId) not found.", nil)
        return
    }

    guard let data = Data(base64Encoded: payloadBase64 as String, options: .ignoreUnknownCharacters) else {
        reject(reject_tag, "Coap SetRequestPayload failed, invalid payload.", nil)
        return
    }

    do {
        try request.setRequestPayload(contentFormat: UInt16(contentFormat), data: data)
        resolve(nil)
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(coapExecute:withResolver:withRejecter:)
  func coapExecute(coapId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let request = mCoapObjects[Int(coapId)] else {
        reject(reject_tag, "Coap object with id \(coapId) not found.", nil)
        return
    }

    request.executeAsync { error, response in
        self.mCoapObjects.removeValue(forKey: Int(coapId))

        if error == NabtoEdgeClientError.OK {
            let result: NSMutableDictionary = [:]
            result["responseStatusCode"] = response!.status
            result["responseContentFormat"] = response!.contentFormat

            if response!.status == 205 {
                result["responsePayload"] = [UInt8](response!.payload)
            }

            resolve(result)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  // ----------------------------------------------------------
  // Tcp Tunnel methods
  // ----------------------------------------------------------
  @objc(tcpTunnelOpen:withService:withLocalPort:withResolver:withRejecter:)
  func tcpTunnelOpen(tcpTunnelId: Double, service: NSString, localPort: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let tunnel = getTunnel(tcpTunnelId)
    tunnel.openAsync(service: service as String, localPort: UInt16(localPort)) { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(tcpTunnelGetLocalPort:withResolver:withRejecter:)
  func tcpTunnelGetLocalPort(tcpTunnelId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let tunnel = getTunnel(tcpTunnelId)
    do {
        resolve(try tunnel.getLocalPort())
    } catch {
        reject(nil, nil, error)
    }
  }

  @objc(tcpTunnelClose:withResolver:withRejecter:)
  func tcpTunnelClose(tcpTunnelId: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let tunnel = getTunnel(tcpTunnelId)
    tunnel.closeAsync { error in
        if error == NabtoEdgeClientError.OK {
            resolve(nil)
        } else {
            reject(nil, nil, error)
        }
    }
  }

  @objc(tcpTunnelDispose:withResolver:withRejecter:)
  func tcpTunnelDispose(tcpTunnelId: Double, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
    mTcpTunnels.removeValue(forKey: Int(tcpTunnelId))
    resolve(nil)
  }
}
