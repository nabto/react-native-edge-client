import EdgeClient from "./module";
import type { Coap, Connection, ConnectionOptions } from "./interface";
import { CoapImpl } from "./CoapImpl";

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

    async createCoap(method: string, path: string): Promise<Coap> {
        const coap = new CoapImpl();
        await EdgeClient.connectionCreateCoap(this.id, coap.id, method, path);
        return coap;
    }
}

