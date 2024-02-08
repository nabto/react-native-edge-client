export interface NabtoClient {
  version(): string
  setLogLevel(level: string): Promise<void>
  createPrivateKey(): string
  createConnection(): Promise<Connection>
  createMdnsScanner(): Promise<MdnsScanner>
  createMdnsScanner(subtype: string): Promise<MdnsScanner>
  dispose(): Promise<void>
}

export interface MdnsScanner {
  // @TODO
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

export enum ConnectionType {
  RELAY, DIRECT
}

// @TODO
export enum ErrorCode {
  OK
}

export enum CoapContentFormat {
  TEXT_PLAIN_UTF8 = 0,
  APPLICATION_LINK_FORMAT = 40,
  XML = 41,
  APPLICATION_OCTET_STREAM = 42,
  APPLICATION_JSON = 50,
  APPLICATION_CBOR = 60
}

export interface CoapResult {
  responseStatusCode: number,
  responseContentFormat: CoapContentFormat,
  responsePayload: Uint8Array
}

export interface Connection {
  updateOptions(options: ConnectionOptions): void
  getOptions(): ConnectionOptions

  getDeviceFingerprint(): Promise<string>
  getClientFingerprint(): Promise<string>
  getType(): Promise<ConnectionType>
  enableDirectCandidates(): Promise<void>
  addDirectCandidate(host: string, port: number): Promise<void>
  endOfDirectCandidates(): Promise<void>

  createStream(): Promise<Stream>
  createCoap(method: string, path: string): Promise<Coap>
  createTcpTunnel(): Promise<TcpTunnel>

  close(): Promise<void>
  dispose(): Promise<void>
  connect(): Promise<void>

  getLocalChannelErrorCode(): Promise<ErrorCode>
  getRemoteChannelErrorCode(): Promise<ErrorCode>
  getDirectCandidatesChannelErrorCode(): Promise<ErrorCode>

  passwordAuthenticate(username: string, password: string): Promise<void>

  // @TODO: ConnectionEventsListener
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
