// ----------------------------------------------------------
// Enums and struct types
// ----------------------------------------------------------

export enum ConnectionType {
  RELAY, DIRECT
}

export const enum ConnectionEvent {
  CONNECTED,
  CLOSED,
  CHANNEL_CHANGED
}

export const enum MdnsResultAction {
  ADD, UPDATE, REMOVE
}

export enum CoapContentFormat {
  TEXT_PLAIN_UTF8 = 0,
  APPLICATION_LINK_FORMAT = 40,
  XML = 41,
  APPLICATION_OCTET_STREAM = 42,
  APPLICATION_JSON = 50,
  APPLICATION_CBOR = 60
}

export interface ConnectionOptions {
  ProductId?: string,
  DeviceId?: string,
  PrivateKey?: string,
  ServerConnectToken?: string,
  KeepAliveInterval?: number,
  KeepAliveRetryInterval?: number,
  KeepAliveMaxRetries?: number
}

export interface MdnsResult {
  action: MdnsResultAction,
  productId: string,
  deviceId: string,
  serviceInstanceName: string,
  txtItems: Record<string, string>,
}

export interface CoapResult {
  responseStatusCode: number,
  responseContentFormat: CoapContentFormat,
  responsePayload: Uint8Array
}

// ----------------------------------------------------------
// Callback types
// ----------------------------------------------------------
export type OnEventCallback = (event: ConnectionEvent) => void
export type OnMdnsResultCallback = (result: MdnsResult) => void

// ----------------------------------------------------------
// Nabto interface
// ----------------------------------------------------------
export interface NabtoClient {
  version(): Promise<string>
  setLogLevel(level: string): Promise<void>
  createPrivateKey(): Promise<string>
  createConnection(): Promise<Connection>
  createMdnsScanner(): Promise<MdnsScanner>
  createMdnsScanner(subtype: string): Promise<MdnsScanner>
  dispose(): Promise<void>
}

export interface MdnsScanner {
  start(): Promise<void>
  stop(): Promise<void>
  isStarted(): Promise<void>

  addMdnsResultReceiver(listener: OnMdnsResultCallback): void
  removeMdnsResultReceiver(listener: OnMdnsResultCallback): boolean
}

export interface Connection {
  updateOptions(options: ConnectionOptions): Promise<void>
  getOptions(): Promise<ConnectionOptions>

  addConnectionEventsListener(listener: OnEventCallback): void
  removeConnectionEventsListener(listener: OnEventCallback): boolean

  getDeviceFingerprint(): Promise<string>
  getClientFingerprint(): Promise<string>
  getType(): Promise<ConnectionType>

  createStream(): Promise<Stream>
  createCoap(method: string, path: string): Promise<Coap>
  createTcpTunnel(): Promise<TcpTunnel>

  close(): Promise<void>
  dispose(): Promise<void>
  connect(): Promise<void>

  passwordAuthenticate(username: string, password: string): Promise<void>
}

export interface Coap {
  setRequestPayload(contentFormat: CoapContentFormat, payload: Uint8Array): void
  execute(): Promise<CoapResult>
}

export interface TcpTunnel {
  open(service: string, localPort: number): Promise<void>
  getLocalPort(): Promise<number>
  close(): Promise<void>
  dispose(): Promise<void>
}

export interface Stream {
  open(streamPort: number): Promise<void>
  readSome(): Promise<Uint8Array>
  readAll(length: number): Promise<Uint8Array>
  write(bytes: Uint8Array): Promise<void>
  close(): Promise<void>
  dispose(): Promise<void>
}
