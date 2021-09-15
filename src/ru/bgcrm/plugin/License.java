package ru.bgcrm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;

import org.apache.commons.io.IOUtils;
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
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

/**
 * License for plugins.
 *
 * @author Shamil Vakhitov
 */
public class License {
    private static final Log log = Log.getLog();

    public static final String FILE_NAME = "lic.data";
    private static License instance;

    public static void init() {
        var file = new File(FILE_NAME);
        var data = "";
        if (file.exists() && file.canRead()) {
            log.info("Loading license from: %s", file);
            try {
                data = IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error(e);
            }
        }
        instance = new License(data);
    }

    public static License getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    /**
     * Appends author and signature to a license file. 
     * @param filePath
     * @param personId
     * @param keyFilePath
     * @param keyFilePswd
     * @throws Exception
     */
    public static void sign(String filePath, String personId, String keyFilePath, String keyFilePswd) throws Exception {
        var file = new File(filePath);
        if (!file.exists())
            throw new FileNotFoundException("Not found license file: " + filePath);
        var data = IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8).trim();

        var key = PUBLIC_KEYS.get(personId);
        if (key == null)
            throw new IllegalArgumentException("Unsupported person: " + personId);

        var lic = new License(data);
        if (lic.isSigned())
            throw new IllegalArgumentException("The license was already signed");
        
        key = new Key(key.id, IOUtils.toString(new FileInputStream(keyFilePath), StandardCharsets.UTF_8), keyFilePswd);

        data += "\n" + KEY_LIC_SIGN_PERSON + "=" + key.id + "\n";
        data += KEY_LIC_SIGN_SIGNATURE + "=" + key.signatureGenerate(new License(data).getDigest()) + "\n";

        IOUtils.write(data, new FileOutputStream(file), StandardCharsets.UTF_8);

        System.out.println("File was updated: " + file);
    }

    /** Entry point for external calls. */
    public static void main(String[] args) throws Exception {
        sign(args[0], args[1], args[2], args[3]);
    }

    /**
     * Accepted SSH public keys.
     */
    private static final Map<String, Key> PUBLIC_KEYS = List.of(
        new Key("Andrey Zuzenkov <andrey@bgerp.org>", "AAAAC3NzaC1lZDI1NTE5AAAAIKtWFBFnQn9fgfyVxalQfzrVMi6BvbW5eXzgDJA7nWR6"),
        new Key("Shamil Vakhitov <shamil@bgerp.org>", "AAAAB3NzaC1yc2EAAAADAQABAAABAQDcxIbqm9nyzkV4eduEwjc1om36taX4vy2n6DFHBM1VtM6k2Lr2dFdAjFHf6p4DEykvp/gfZMQqB8I5YdzPZld/glZ6uHAEAHyRngJvua+8437KeL6pOp8HrEop+iIo8+whkfDxqGDQjHcjkM+KlJr8Lgk/62BKaL7TlHRQJ5VDnVvfiAivcv413Hdx5GQoYCQbLX4Ckr7n0BmoVK3bBGxyrPav9ihhdNe1Vpqq1IKhuYRwVYYl9yU5E7x3eu4wIi4R9LcrSre+oY0AL0rM4Nn4kOocecM0q+LC3lsNebXcvVPVdO2+TZBWI9flIodM3Jh4/3KJFOYxBzivupCbNZAr")
    ).stream().collect(Collectors.toMap(e -> e.id, e -> e));

    /** 
     * Signer supported SSH keys: 'RSA', 'ED25519'
     */
    public static class Key {
        private final String id;
        private final Signer sign;

        /**
         * Constructor for sign verification.
         * @param id unique key ID.
         * @param publicKey encoded public key.
         */
        private Key(String id, String publicKey) {
            this.id = id;
            this.sign = initSignerVerify(publicKey);
        }

        /**
         * Constructor for sign generation.
         * @param id unique key ID.
         * @param privateKey encoded private key.
         * @param password for decrypting the private key, or null.
         */
        public Key(String id, String privateKey, String password) {
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
         * @throws SignatureException
         */
        public boolean signatureVerify(byte[] data, String signature) {
            sign.update(data, 0, data.length);
            return sign.verifySignature(Base64.getDecoder().decode(signature));
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

    private static final String KEY_LIC = "lic.";
    private static final String KEY_LIC_PLUGIN = KEY_LIC + "plugin.";
    private static final int KEY_LIC_PLUGIN_LENGTH = KEY_LIC_PLUGIN.length();
    private static final String KEY_LIC_SIGN = KEY_LIC + "sign.";
    private static final String KEY_LIC_SIGN_PERSON = KEY_LIC_SIGN + "person";
    private static final String KEY_LIC_SIGN_SIGNATURE = KEY_LIC_SIGN + "signature";
    
    private final String data;
    private final ParameterMap config;
    private final byte[] digest;
    private final String error;
    private final Set<String> plugins;

    public License(String data) {
        this.data = data;
        this.config = new Preferences(data);
        this.digest = digest();
        this.error = check();
        this.plugins = plugins();
    }

    /**
     * Open content of license.
     * @return
     */
    public String getData() {
        return data;
    }

    /**
     * Digest for all the license's lines before lic.sign.signature.
     * @return
     */
    public byte[] getDigest() {
        return digest;
    }

    /**
     * License check result.
     * @return null on correct result, or error text.
     */
    public String getError() {
        return error;
    }

    /**
     * Enabled plugin IDs.
     * @return
     */
    public Set<String> getPlugins() {
        return plugins;
    }

    public boolean isSigned() {
        return config.containsKey(KEY_LIC_SIGN_PERSON) || config.containsKey(KEY_LIC_SIGN_SIGNATURE);
    }

    private byte[] digest() {
        var buffer = new StringBuilder(1000);
        
        try (var scanner = new Scanner(data)) {
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine();
                if (line.startsWith(KEY_LIC_SIGN_SIGNATURE))
                    break;
                buffer.append(line);
            }
        }

        try {
            var digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(buffer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Digest exception", e);
        }
    }

    private String check() {
        var person = config.get(KEY_LIC_SIGN_PERSON);
        if (Utils.isEmptyString(person))
            return "Signing person is undefined";
        
        var key = PUBLIC_KEYS.get(person);
        if (key == null)
            return "Not found key for person: " + person;

        var signature = config.get(KEY_LIC_SIGN_SIGNATURE);
        if (Utils.isEmptyString(signature))
            return "Signature is undefined";

        if (!key.signatureVerify(digest, signature))
            return "Signature is not correct";
        
        return null;
    }

    private Set<String> plugins() {
        var result = new HashSet<String>();
        for (var me : config.entrySet()) {
            var key = me.getKey();
            if (!key.startsWith(KEY_LIC_PLUGIN) || !Utils.parseBoolean(me.getValue(), false))
                continue;
            
            result.add(key.substring(KEY_LIC_PLUGIN_LENGTH));
        }

        return Collections.unmodifiableSet(result);
    }
}
