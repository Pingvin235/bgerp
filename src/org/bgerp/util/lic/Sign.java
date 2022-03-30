package org.bgerp.util.lic;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;

import org.bgerp.util.Log;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

import net.schmizz.sshj.userauth.keyprovider.BaseFileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import ru.bgcrm.util.Utils;

/**
 * Signer supported SSH keys: 'RSA', 'ED25519'
 *
 * @author Shamil Vakhitov
 */
public class Sign {
    private static final Log log = Log.getLog();

    final String id;
    private final Signer sign;

    /**
     * Constructor for sign verification.
     * @param id unique key ID.
     * @param publicKey encoded public key.
     */
    Sign(String id, String publicKey) {
        this.id = id;
        this.sign = initSignerVerify(publicKey);
    }

    /**
     * Constructor for sign generation.
     * @param id unique key ID.
     * @param privateKey encoded private key.
     * @param password for decrypting the private key, or null.
     */
    public Sign(String id, String privateKey, String password) {
        this.id = id;
        this.sign = initSignerGenerate(privateKey, password);
    }

    private Signer initSignerVerify(String publicKey) {
        var keyParameters = OpenSSHPublicKeyUtil.parsePublicKey(Base64.getDecoder().decode(publicKey));
        if (keyParameters.isPrivate())
            throw new IllegalArgumentException("Private key is not allowed");

        return initSigner(keyParameters, false);
    }

    private Signer initSignerGenerate(String privateKey, String password) {
        try {
            AsymmetricKeyParameter keyParameter = null;
            try {
                keyParameter = loadKeyParameters(new OpenSSHKeyV1KeyFile(), privateKey, password);
            } catch (IOException e) {
                if (e.getMessage().equals("This key is not in 'openssh-key-v1' format")) {
                    keyParameter = loadKeyParameters(new OpenSSHKeyFile(), privateKey, password);
                } else throw e;
            }
            return initSigner(keyParameter, true);
        } catch (IOException e) {
            throw new IllegalArgumentException("Parse exception for key: " + id, e);
        }
    }

    private AsymmetricKeyParameter loadKeyParameters(BaseFileKeyProvider keyFile, String privateKey, String password) throws IOException {
        var privateKeyReader = new StringReader(privateKey);
        if (Utils.notBlankString(password))
            keyFile.init(privateKeyReader, PasswordUtils.createOneOff(password.toCharArray()));
        else
            keyFile.init(privateKeyReader);
        var privateKeyJca = keyFile.getPrivate();
        return PrivateKeyFactory.createKey(privateKeyJca.getEncoded());
    }

    private Signer initSigner(AsymmetricKeyParameter keyParameters, boolean signing) {
        try {
            if (keyParameters instanceof RSAKeyParameters) {
                var signer = new RSADigestSigner(new SHA256Digest());
                signer.init(signing, keyParameters);
                return signer;
            }

            if (keyParameters instanceof Ed25519PublicKeyParameters ||
                keyParameters instanceof Ed25519PrivateKeyParameters) {
                var signer = new Ed25519Signer();
                signer.init(signing, keyParameters);
                return signer;
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Parse exception for key: " + id, e);
        }

        throw new IllegalArgumentException("Unsupported key type: " + keyParameters + "; id: " + id);
    }

    /**
     * Verification of signature.
     * @param data raw data.
     * @param signature Base64 encoded signature.
     * @return
     */
    public boolean signatureVerify(byte[] data, String signature) {
        try {
            sign.update(data, 0, data.length);
            return sign.verifySignature(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.info("Error in license signature verification: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Makes a signature.
     * @param data raw.
     * @return Base64 encoded string.
     */
    public String signatureGenerate(byte[] data) throws CryptoException {
        sign.update(data, 0, data.length);
        return Base64.getEncoder().encodeToString(sign.generateSignature());
    }
}