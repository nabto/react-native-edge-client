import { toBase64 } from "./util";
import type { Coap, CoapContentFormat, CoapResult } from "../interface"
import EdgeClient from "../module"

export class CoapImpl implements Coap {
    private static nextId = 0
    id = CoapImpl.nextId++

    setRequestPayload(contentFormat: CoapContentFormat, payload: Uint8Array): void {
        EdgeClient.coapSetRequestPayload(this.id, contentFormat, toBase64(payload));
    }

    async execute(): Promise<CoapResult> {
        const result = await EdgeClient.coapExecute(this.id)
        return {
            responseContentFormat: result.responseContentFormat,
            responseStatusCode: result.responseStatusCode,
            responsePayload: new Uint8Array(result.responsePayload)
        }
    }
}
