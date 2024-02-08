import type { TcpTunnel } from "../interface";
import EdgeClient from "../module";

export class TcpTunnelImpl implements TcpTunnel {
    private static nextId = 0
    id = TcpTunnelImpl.nextId++

    open(service: string, localPort: number): Promise<void> {
        return EdgeClient.tcpTunnelOpen(this.id, service, localPort);
    }

    getLocalPort(): Promise<number> {
        return EdgeClient.tcpTunnelGetLocalPort(this.id);
    }

    close(): Promise<void> {
        return EdgeClient.tcpTunnelClose(this.id);
    }
    
    dispose(): Promise<void> {
        return EdgeClient.tcpTunnelDispose(this.id);
    }
}
