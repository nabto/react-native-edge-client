import EdgeClient from "../module";
import type { NabtoClient, Connection, MdnsScanner } from "../interface";
import { ConnectionImpl } from "./ConnectionImpl";
import { MdnsScannerImpl } from "./MdnsScannerImpl";

export async function createNabtoClient(): Promise<NabtoClient> {
    const client = new NabtoClientImpl();
    await EdgeClient.createNabtoClient(client.id);
    return client;
}

class NabtoClientImpl implements NabtoClient {
    private static nextId = 0
    id = NabtoClientImpl.nextId++

    version() {
        return EdgeClient.clientGetVersion(this.id);
    }

    createPrivateKey() {
        return EdgeClient.clientCreatePrivateKey(this.id);
    }

    async createConnection(): Promise<Connection> {
        const connection = new ConnectionImpl();
        await EdgeClient.clientCreateConnection(this.id, connection.id);
        return connection;
    }

    setLogLevel(level: string): Promise<void> {
        return EdgeClient.clientSetLogLevel(this.id, level);
    }

    async createMdnsScanner(subtype?: string): Promise<MdnsScanner> {
        const scanner = new MdnsScannerImpl();
        await EdgeClient.createMdnsScanner(this.id, scanner.id, subtype ?? "");
        return scanner;
    }

    close(): Promise<void> {
        throw new Error("Method not implemented.");
    }
    
    dispose(): Promise<void> {
        throw new Error("Method not implemented.");
    }
}
