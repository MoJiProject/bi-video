const API_CRYPTO_KEY = 'bi-video-api-key-32-byte-value!!';
const API_CRYPTO_IV = 'bi-video-api-iv!';

const encoder = new TextEncoder();
const decoder = new TextDecoder();

let cryptoKeyPromise;

function getCryptoKey() {
  if (!cryptoKeyPromise) {
    cryptoKeyPromise = window.crypto.subtle.importKey(
      'raw',
      encoder.encode(API_CRYPTO_KEY),
      { name: 'AES-CBC' },
      false,
      ['encrypt', 'decrypt']
    );
  }
  return cryptoKeyPromise;
}

function toBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';

  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });

  return btoa(binary);
}

function fromBase64(base64) {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);

  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }

  return bytes;
}

export async function encryptPayload(value) {
  const key = await getCryptoKey();
  const encrypted = await window.crypto.subtle.encrypt(
    {
      name: 'AES-CBC',
      iv: encoder.encode(API_CRYPTO_IV),
    },
    key,
    encoder.encode(JSON.stringify(value))
  );

  return toBase64(encrypted);
}

export async function decryptPayload(payload) {
  const key = await getCryptoKey();
  const decrypted = await window.crypto.subtle.decrypt(
    {
      name: 'AES-CBC',
      iv: encoder.encode(API_CRYPTO_IV),
    },
    key,
    fromBase64(payload)
  );

  return JSON.parse(decoder.decode(decrypted));
}