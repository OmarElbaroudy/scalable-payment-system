package utilities;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.utils.Assertions;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

public class Sign {
    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    static final ECDomainParameters CURVE;
    static final BigInteger HALF_CURVE_ORDER;
    static final String MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    public Sign() {
    }

    static byte[] getEthereumMessagePrefix(int messageLength) {
        return "\u0019Ethereum Signed Message:\n".concat(String.valueOf(messageLength)).getBytes();
    }

    static byte[] getEthereumMessageHash(byte[] message) {
        byte[] prefix = getEthereumMessagePrefix(message.length);
        byte[] result = new byte[prefix.length + message.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(message, 0, result, prefix.length, message.length);
        return Hash.sha3(result);
    }

    public static Sign.SignatureData signPrefixedMessage(byte[] message, ECKeyPair keyPair) {
        return signMessage(getEthereumMessageHash(message), keyPair, false);
    }

    public static Sign.SignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        return signMessage(message, keyPair, true);
    }

    public static Sign.SignatureData signMessage(byte[] message, ECKeyPair keyPair, boolean needToHash) {
        BigInteger publicKey = keyPair.getPublicKey();
        byte[] messageHash;
        if (needToHash) {
            messageHash = Hash.sha3(message);
        } else {
            messageHash = message;
        }

        ECDSASignature sig = keyPair.sign(messageHash);
        int recId = -1;

        int headerByte;
        for (headerByte = 0; headerByte < 4; ++headerByte) {
            BigInteger k = recoverFromSignature(headerByte, sig, messageHash);
            if (k != null && k.equals(publicKey)) {
                recId = headerByte;
                break;
            }
        }

        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. Are your credentials valid?");
        } else {
            headerByte = recId + 27;
            byte[] v = new byte[]{(byte) headerByte};
            byte[] r = Numeric.toBytesPadded(sig.r, 32);
            byte[] s = Numeric.toBytesPadded(sig.s, 32);
            return new Sign.SignatureData(v, r, s);
        }
    }

    public static BigInteger recoverFromSignature(int recId, ECDSASignature sig, byte[] message) {
        Assertions.verifyPrecondition(recId >= 0, "recId must be positive");
        Assertions.verifyPrecondition(sig.r.signum() >= 0, "r must be positive");
        Assertions.verifyPrecondition(sig.s.signum() >= 0, "s must be positive");
        Assertions.verifyPrecondition(message != null, "message cannot be null");
        BigInteger n = CURVE.getN();
        BigInteger i = BigInteger.valueOf((long) recId / 2L);
        BigInteger x = sig.r.add(i.multiply(n));
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            return null;
        } else {
            ECPoint R = decompressKey(x, (recId & 1) == 1);
            if (!R.multiply(n).isInfinity()) {
                return null;
            } else {
                BigInteger e = new BigInteger(1, message);
                BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
                BigInteger rInv = sig.r.modInverse(n);
                BigInteger srInv = rInv.multiply(sig.s).mod(n);
                BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
                ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
                byte[] qBytes = q.getEncoded(false);
                return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
            }
        }
    }

    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 3 : 2);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    public static BigInteger signedMessageToKey(byte[] message, Sign.SignatureData signatureData) throws SignatureException {
        return signedMessageHashToKey(Hash.sha3(message), signatureData);
    }

    public static BigInteger signedPrefixedMessageToKey(byte[] message, Sign.SignatureData signatureData) throws SignatureException {
        return signedMessageHashToKey(getEthereumMessageHash(message), signatureData);
    }

    public static BigInteger signedMessageHashToKey(byte[] messageHash, Sign.SignatureData signatureData) throws SignatureException {
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        Assertions.verifyPrecondition(r != null && r.length == 32, "r must be 32 bytes");
        Assertions.verifyPrecondition(s != null && s.length == 32, "s must be 32 bytes");
        int header = signatureData.getV()[0] & 255;
        if (header >= 27 && header <= 34) {
            ECDSASignature sig = new ECDSASignature(new BigInteger(1, signatureData.getR()), new BigInteger(1, signatureData.getS()));
            int recId = header - 27;
            BigInteger key = recoverFromSignature(recId, sig, messageHash);
            if (key == null) {
                throw new SignatureException("Could not recover public key from signature");
            } else {
                return key;
            }
        } else {
            throw new SignatureException("Header byte out of range: " + header);
        }
    }

    public static BigInteger publicKeyFromPrivate(BigInteger privKey) {
        ECPoint point = publicPointFromPrivate(privKey);
        byte[] encoded = point.getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length));
    }

    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }

        return (new FixedPointCombMultiplier()).multiply(CURVE.getG(), privKey);
    }

    public static BigInteger publicFromPoint(byte[] bits) {
        return new BigInteger(1, Arrays.copyOfRange(bits, 1, bits.length));
    }

    static {
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    }

    public static class SignatureData {
        private byte[] v;
        private byte[] r;
        private byte[] s;

        public SignatureData(byte v, byte[] r, byte[] s) {
            this(new byte[]{v}, r, s);
        }

        public SignatureData(byte[] v, byte[] r, byte[] s) {
            this.v = v;
            this.r = r;
            this.s = s;
        }

        public SignatureData() {
        }

        public byte[] getV() {
            return v;
        }

        public void setV(byte[] v) {
            this.v = v;
        }

        public byte[] getR() {
            return r;
        }

        public void setR(byte[] r) {
            this.r = r;
        }

        public byte[] getS() {
            return s;
        }

        public void setS(byte[] s) {
            this.s = s;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                Sign.SignatureData that = (Sign.SignatureData) o;
                if (!Arrays.equals(this.v, that.v)) {
                    return false;
                } else {
                    return Arrays.equals(this.r, that.r) && Arrays.equals(this.s, that.s);
                }
            } else {
                return false;
            }
        }

        public int hashCode() {
            int result = Arrays.hashCode(this.v);
            result = 31 * result + Arrays.hashCode(this.r);
            result = 31 * result + Arrays.hashCode(this.s);
            return result;
        }
    }
}

