package org.bouncycastle.tls.crypto.impl.jcajce;

import java.io.IOException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.pqc.crypto.crystals.kyber.KyberPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.crystals.kyber.KyberPublicKeyParameters;
import org.bouncycastle.tls.crypto.TlsAgreement;
import org.bouncycastle.util.Arrays;

public class JceTlsMlKem implements TlsAgreement
{
    protected final JceTlsMlKemDomain domain;

    protected AsymmetricCipherKeyPair localKeyPair;
    protected KyberPublicKeyParameters peerPublicKey;
    protected byte[] ciphertext;
    protected byte[] secret;

    public JceTlsMlKem(JceTlsMlKemDomain domain)
    {
        this.domain = domain;
    }

    public byte[] generateEphemeral() throws IOException
    {
        if (domain.getTlsKEMConfig().isServer())
        {
            return Arrays.clone(ciphertext);
        }
        else
        {
            this.localKeyPair = domain.generateKeyPair();
            return domain.encodePublicKey((KyberPublicKeyParameters)localKeyPair.getPublic());
        }
    }

    public void receivePeerValue(byte[] peerValue) throws IOException
    {
        if (domain.getTlsKEMConfig().isServer())
        {
            this.peerPublicKey = domain.decodePublicKey(peerValue);
            SecretWithEncapsulation encap = domain.encap(peerPublicKey);
            ciphertext = encap.getEncapsulation();
            secret = encap.getSecret();
        }
        else
        {
            this.ciphertext = Arrays.clone(peerValue);
        }
    }

    public JceTlsSecret calculateSecret() throws IOException
    {
        if (domain.getTlsKEMConfig().isServer())
        {
            return domain.adoptLocalSecret(secret);
        }
        else
        {
            return domain.adoptLocalSecret(domain.decap((KyberPrivateKeyParameters)localKeyPair.getPrivate(), ciphertext));
        }
    }
}
