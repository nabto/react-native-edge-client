import EdgeClient from "../module";
import type { Coap, Connection, ConnectionOptions, ConnectionType, ErrorCode, OnEventCallback, Stream, TcpTunnel } from "../interface";
import { CoapImpl } from "./CoapImpl";
import { TcpTunnelImpl } from "./TcpTunnelImpl";
import { StreamImpl } from "./StreamImpl";
import { NativeEventEmitter } from "react-native";
import type { EmitterSubscription } from "react-native";

export class ConnectionImpl implements Connection {
    private static nextId = 0
    id = ConnectionImpl.nextId++
    listeners: OnEventCallback[] = []
    nativeEventListener?: EmitterSubscription

    constructor() {
        const emitter = new NativeEventEmitter(EdgeClient);
        this.nativeEventListener = emitter.addListener(`ConnectionOnEvent#${this.id}`, (event: {event: number}) => {
            this.listeners.forEach(func => func(event.event));
        })
    }

    private cleanup() {
        this.listeners = [];
        this.nativeEventListener?.remove();
        this.nativeEventListener = undefined;
    }

    connect(): Promise<void> {
        return EdgeClient.connectionConnect(this.id);
    }

    addConnectionEventsListener(listener: OnEventCallback): void {
        this.listeners.push(listener);
    }

    removeConnectionEventsListener(listener: OnEventCallback): boolean {
        const index = this.listeners.indexOf(listener);
        if (index >= 0) {
            this.listeners.splice(index, 1);
            return true;
        }
        return false;
    }

    async updateOptions(options: ConnectionOptions) {
        return EdgeClient.connectionUpdateOptions(this.id, JSON.stringify(options));
    }

    async getOptions() {
        return JSON.parse(await EdgeClient.connectionGetOptions(this.id));
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
        const tunnel = new TcpTunnelImpl();
        await EdgeClient.connectionCreateTcpTunnel(this.id, tunnel.id);
        return tunnel;
    }

    getDeviceFingerprint(): Promise<string> {
        return EdgeClient.connectionGetDeviceFingerprint(this.id);
    }

    getClientFingerprint(): Promise<string> {
        return EdgeClient.connectionGetClientFingerprint(this.id);
    }

    getType(): Promise<ConnectionType> {
        return EdgeClient.connectionGetType(this.id);
    }

    enableDirectCandidates(): Promise<void> {
        return EdgeClient.connectionEnableDirectCandidates(this.id);
    }

    addDirectCandidate(host: string, port: number): Promise<void> {
        return EdgeClient.connectionAddDirectCandidate(this.id, host, port);
    }

    endOfDirectCandidates(): Promise<void> {
        return EdgeClient.connectionEndOfDirectCandidates(this.id);
    }

    close(): Promise<void> {
        this.cleanup();
        return EdgeClient.connectionClose(this.id);
    }

    dispose(): Promise<void> {
        this.cleanup();
        return EdgeClient.connectionDispose(this.id);
    }

    getLocalChannelErrorCode(): Promise<ErrorCode> {
        return EdgeClient.connectionGetLocalChannelErrorCode(this.id);
    }

    getRemoteChannelErrorCode(): Promise<ErrorCode> {
        return EdgeClient.connectionGetRemoteChannelErrorCode(this.id);
    }

    getDirectCandidatesChannelErrorCode(): Promise<ErrorCode> {
        return EdgeClient.connectionGetDirectCandidatesChannelErrorCode(this.id);
    }

    passwordAuthenticate(username: string, password: string): Promise<void> {
        return EdgeClient.connectionPasswordAuthenticate(this.id, username, password);
    }

}

