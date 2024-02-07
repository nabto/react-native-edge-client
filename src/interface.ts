export interface NabtoClient {
  version(): string
  createPrivateKey(): string
  createConnection(): Promise<Connection>
}

export interface ConnectionOptions {
  ProductId: string,
  DeviceId: string,
  PrivateKey: string,
  ServerConnectToken: string,
  KeepAliveInterval?: number,
  KeepAliveRetryInterval?: number,
  KeepAliveMaxRetries?: number
}

export interface Connection {
  updateOptions(options: ConnectionOptions): void
  getOptions(): ConnectionOptions
  connect(): Promise<void>
  createCoap(method: string, path: string): Promise<Coap>
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

export interface Coap {
  setRequestPayload(contentFormat: CoapContentFormat, payload: Uint8Array): void
  execute(): Promise<CoapResult>
}
