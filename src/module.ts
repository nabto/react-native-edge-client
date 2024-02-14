import type { NativeModule } from 'react-native';
import { NativeModules, Platform } from 'react-native';

interface NativeCoapResult {
    responseStatusCode: number,
    responseContentFormat: number,
    responsePayload: number[]
}

interface EdgeClientNativeModule {
    createNabtoClient(id: number): Promise<void>
    clientGetVersion(clientId: number): Promise<string>
    clientSetLogLevel(clientId: number, level: string): Promise<void>
    clientCreatePrivateKey(clientId: number): Promise<string>
    createMdnsScanner(clientId: number, scannerId: number, subtype: string): Promise<void>
    clientCreateConnection(clientId: number, connectionId: number): Promise<void>
    clientDispose(clientId: number): Promise<void>

    mdnsScannerStart(scannerId: number): Promise<void>
    mdnsScannerStop(scannerId: number): Promise<void>
    mdnsScannerIsStarted(scannerId: number): Promise<boolean>
  
    connectionUpdateOptions(connectionId: number, options: string): Promise<void>
    connectionGetOptions(connectionId: number): Promise<string>
    connectionConnect(connectionId: number): Promise<void>
    connectionCreateStream(connectionId: number, streamId: number): Promise<void>
    connectionCreateCoap(connectionId: number, coapId: number, method: string, path: string): Promise<void>
    connectionGetDeviceFingerprint(connectionId: number): Promise<string>
    connectionGetClientFingerprint(connectionId: number): Promise<string>
    connectionGetType(connectionId: number): Promise<number>
    connectionEnableDirectCandidates(connectionId: number): Promise<void>
    connectionAddDirectCandidate(connectionId: number, host: string, port: number): Promise<void>
    connectionEndOfDirectCandidates(connectionId: number): Promise<void>
    connectionCreateTcpTunnel(connectionId: number, tcpTunnelId: number): Promise<void>
    connectionDispose(connectionId: number): Promise<void>
    connectionClose(connectionId: number): Promise<void>
    connectionGetLocalChannelErrorCode(connectionId: number): Promise<number>
    connectionGetRemoteChannelErrorCode(connectionId: number): Promise<number>
    connectionGetDirectCandidatesChannelErrorCode(connectionId: number): Promise<number>
    connectionPasswordAuthenticate(connectionId: number, username: string, password: string): Promise<void>
    
    streamOpen(streamId: number, streamPort: number): Promise<void>
    streamReadSome(streamId: number): Promise<number[]>
    streamReadAll(streamId: number, length: number): Promise<number[]>
    streamWrite(streamId: number, bytesBase64: string): Promise<void>
    streamClose(streamId: number): Promise<void>
    streamDispose(streamId: number): Promise<void>

    coapSetRequestPayload(coapId: number, contentFormat: number, payloadBase64: string): void
    coapExecute(coapId: number): Promise<NativeCoapResult>

    tcpTunnelOpen(tcpTunnelId: number, service: string, localPort: number): Promise<void>
    tcpTunnelGetLocalPort(tcpTunnelId: number): Promise<number>
    tcpTunnelClose(tcpTunnelId: number): Promise<void>
    tcpTunnelDispose(tcpTunnelId: number): Promise<void>
}

const LINKING_ERROR =
    `The package 'react-native-edge-client' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n';

if (NativeModules.EdgeClient === null) {
    throw new Error(LINKING_ERROR);
}

const EdgeClient = NativeModules.EdgeClient as (EdgeClientNativeModule & NativeModule);
export default EdgeClient;
