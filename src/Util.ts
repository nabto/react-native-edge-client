import { Buffer } from "buffer"

export function toBase64(input: Uint8Array) {
    return Buffer.from(input).toString("base64")
}
