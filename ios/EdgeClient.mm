#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(EdgeClient, NSObject)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

// ----------------------------------------------------------
// Nabto Client methods
// ----------------------------------------------------------
RCT_EXTERN_METHOD(createNabtoClient:(double)clientId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(clientGetVersion:(double)clientId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(clientSetLogLevel:(double)clientId withLevel:(NSString)level
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(clientCreatePrivateKey:(double)clientId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(clientCreateConnection:(double)clientId withConnectionId:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

// ----------------------------------------------------------
// Connection methods
// ----------------------------------------------------------

RCT_EXTERN_METHOD(connectionCreateStream:(double)connectionId withStreamId:(double)streamId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionCreateCoap:(double)connectionId
                  withCoapId:(double)coapId withMethod:(NSString)method withPath:(NSString)path
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionCreateTcpTunnel:(double)connectionId withTunnelId:(double)tcpTunnelId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionUpdateOptions:(double)connectionId withOptions:(NSString)options
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionGetOptions:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionConnect:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionGetDeviceFingerprint:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionGetClientFingerprint:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionGetType:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionPasswordAuthenticate:(double)connectionId
                  withUsername:(NSString)username withPassword:(NSString)password
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionClose:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(connectionDispose:(double)connectionId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

// ----------------------------------------------------------
// Stream methods
// ----------------------------------------------------------

RCT_EXTERN_METHOD(streamOpen:(double)streamId withStreamPort:(double)streamPort
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(streamReadSome:(double)streamId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(streamReadAll:(double)streamId withLength:(double)length
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(streamWrite:(double)streamId withBytes:(NSString)bytesBase64
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(streamClose:(double)streamId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(streamDispose:(double)streamId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)


// ----------------------------------------------------------
// Coap methods
// ----------------------------------------------------------

RCT_EXTERN_METHOD(coapSetRequestPayload:(double)coapId withContentFormat:(double)contentFormat withPayload:(NSString)payloadBase64
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(coapExecute:(double)coapId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

// ----------------------------------------------------------
// Tcp Tunnel methods
// ----------------------------------------------------------

RCT_EXTERN_METHOD(tcpTunnelOpen:(double)tcpTunnelId withService:(NSString)service withLocalPort:(double)localPort
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
                  
RCT_EXTERN_METHOD(tcpTunnelGetLocalPort:(double)tcpTunnelId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
                  
RCT_EXTERN_METHOD(tcpTunnelClose:(double)tcpTunnelId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
                  
RCT_EXTERN_METHOD(tcpTunnelDispose:(double)tcpTunnelId
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
                  


+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
