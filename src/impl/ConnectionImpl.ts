import EdgeClient from "../module";
import type { Coap, Connection, ConnectionOptions, ConnectionType, ErrorCode, Stream, TcpTunnel } from "../interface";
import { CoapImpl } from "./CoapImpl";
import { TcpTunnelImpl } from "./TcpTunnelImpl";
import { StreamImpl } from "./StreamImpl";

export class ConnectionImpl implements Connection {
    private static nextId = 0
    id = ConnectionImpl.nextId++

    updateOptions(options: ConnectionOptions): void {
        EdgeClient.connectionUpdateOptions(this.id, JSON.stringify(options));
    }

    getOptions(): ConnectionOptions {
        // @TODO: Validation?
        return JSON.parse(EdgeClient.connectionGetOptions(this.id));
    }

    connect(): Promise<void> {
        return EdgeClient.connectionConnect(this.id);
    }

    async createStream(): Promise<Stream> {
        const stream = new StreamImpl();
        await EdgeClient.connectionCreateStream(this.id, stream.id);
        return stream;
    }

    async createCoap(method: string, path: string): Promise<Coap> {
        const coap = new CoapImpl();
        await EdgeClient.connectionCreateCoap(this.id, coap.id, method, path);
        return coap;
    }

    async createTcpTunnel(): Promise<TcpTunnel> {
        const tunnel = new TcpTunnelImpl()
        EdgeClient.connectionCreateTcpTunnel(this.id, tunnel.id);
        return tunnel;
    }

    async getDeviceFingerprint(): Promise<string> {
        return EdgeClient.connectionGetDeviceFingerprint(this.id);
    }

    async getClientFingerprint(): Promise<string> {
        return EdgeClient.connectionGetClientFingerprint(this.id);
    }

    async getType(): Promise<ConnectionType> {
        return EdgeClient.connectionGetType(this.id);
    }

    async enableDirectCandidates(): Promise<void> {
        return EdgeClient.connectionEnableDirectCandidates(this.id);
    }

    async addDirectCandidate(host: string, port: number): Promise<void> {
        return EdgeClient.connectionAddDirectCandidate(this.id, host, port);
    }

    async endOfDirectCandidates(): Promise<void> {
        return EdgeClient.connectionEndOfDirectCandidates(this.id);
    }

    async close(): Promise<void> {
        return EdgeClient.connectionClose(this.id);
    }

    async dispose(): Promise<void> {
        return EdgeClient.connectionDispose(this.id);
    }

    async getLocalChannelErrorCode(): Promise<ErrorCode> {
        return EdgeClient.connectionGetLocalChannelErrorCode(this.id);
    }

    async getRemoteChannelErrorCode(): Promise<ErrorCode> {
        return EdgeClient.connectionGetRemoteChannelErrorCode(this.id);
    }

    async getDirectCandidatesChannelErrorCode(): Promise<ErrorCode> {
        return EdgeClient.connectionGetDirectCandidatesChannelErrorCode(this.id);
    }

    async passwordAuthenticate(username: string, password: string): Promise<void> {
        return EdgeClient.connectionPasswordAuthenticate(this.id, username, password);
    }

}

