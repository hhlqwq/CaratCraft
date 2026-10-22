/** 保持后端历史AES-CBC零填充协议, 固定公开密钥不能替代HTTPS传输保护. */
export async function Encrypt(word, keyStr = '_aes_secret_key_', ivStr = '_aes_secret_iv__') {
    if (!globalThis.crypto || !globalThis.crypto.subtle) {
        const error = new Error('请使用HTTPS或本机安全环境登录');
        error.code = 'SECURE_CONTEXT_REQUIRED';
        throw error;
    }
    const encoder = new TextEncoder();
    const bytes = encoder.encode(word);
    const padded = new Uint8Array(Math.ceil(bytes.length / 16) * 16);
    padded.set(bytes);
    const key = await crypto.subtle.importKey('raw', encoder.encode(keyStr), 'AES-CBC', false, [
        'encrypt',
    ]);
    // Web Crypto会追加一个完整PKCS7块, 去掉该块可保持既有零填充密文协议.
    const encrypted = new Uint8Array(
        await crypto.subtle.encrypt({name: 'AES-CBC', iv: encoder.encode(ivStr)}, key, padded)
    );
    return btoa(String.fromCharCode(...encrypted.subarray(0, encrypted.length - 16)));
}
