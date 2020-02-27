package sun.security.timestamp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class TimestampToken {
   private int version;
   private ObjectIdentifier policy;
   private BigInteger serialNumber;
   private AlgorithmId hashAlgorithm;
   private byte[] hashedMessage;
   private Date genTime;
   private BigInteger nonce;

   public TimestampToken(byte[] var1) throws IOException {
      if (var1 == null) {
         throw new IOException("No timestamp token info");
      } else {
         this.parse(var1);
      }
   }

   public Date getDate() {
      return this.genTime;
   }

   public AlgorithmId getHashAlgorithm() {
      return this.hashAlgorithm;
   }

   public byte[] getHashedMessage() {
      return this.hashedMessage;
   }

   public BigInteger getNonce() {
      return this.nonce;
   }

   public String getPolicyID() {
      return this.policy.toString();
   }

   public BigInteger getSerialNumber() {
      return this.serialNumber;
   }

   private void parse(byte[] var1) throws IOException {
      DerValue var2 = new DerValue(var1);
      if (var2.tag != 48) {
         throw new IOException("Bad encoding for timestamp token info");
      } else {
         this.version = var2.data.getInteger();
         this.policy = var2.data.getOID();
         DerValue var3 = var2.data.getDerValue();
         this.hashAlgorithm = AlgorithmId.parse(var3.data.getDerValue());
         this.hashedMessage = var3.data.getOctetString();
         this.serialNumber = var2.data.getBigInteger();
         this.genTime = var2.data.getGeneralizedTime();

         while(var2.data.available() > 0) {
            DerValue var4 = var2.data.getDerValue();
            if (var4.tag == 2) {
               this.nonce = var4.getBigInteger();
               break;
            }
         }

      }
   }
}
