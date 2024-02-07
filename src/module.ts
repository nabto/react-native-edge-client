import { NativeModules, Platform } from 'react-native';

interface NativeCoapResult {
    responseStatusCode: number,
    responseContentFormat: number,
    responsePayload: number[]
}

interface EdgeClientNativeModule {
    createNabtoClient(id: number): Promise<void>
    clientGetVersion(clientId: number): string
    clientCreatePrivateKey(clientId: number): string
    clientCreateConnection(clientId: number, connectionId: number): Promise<void>
    clientClose(clientId: number): Promise<void>
  
    connectionUpdateOptions(connectionId: number, options: string): void
    connectionGetOptions(connectionId: number): string
    connectionConnect(connectionId: number): Promise<void>
    connectionCreateCoap(connectionId: number, coapId: number, method: string, path: string): Promise<void>

    coapSetRequestPayload(coapId: number, contentFormat: number, payloadBase64: string): void
    coapExecute(coapId: number): Promise<NativeCoapResult>
}

const LINKING_ERROR =
    `The package 'react-native-edge-client' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n';

if (NativeModules.EdgeClient === null) {
    throw new Error(LINKING_ERROR);
}

const EdgeClient = NativeModules.EdgeClient as EdgeClientNativeModule;
export default EdgeClient;
