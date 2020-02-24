package sun.security.x509;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class URIName implements GeneralNameInterface {
   private URI uri;
   private String host;
   private DNSName hostDNS;
   private IPAddressName hostIP;

   public URIName(DerValue var1) throws IOException {
      this(var1.getIA5String());
   }

   public URIName(String var1) throws IOException {
      try {
         this.uri = new URI(var1);
      } catch (URISyntaxException var7) {
         throw new IOException("invalid URI name:" + var1, var7);
      }

      if (this.uri.getScheme() == null) {
         throw new IOException("URI name must include scheme:" + var1);
      } else {
         this.host = this.uri.getHost();
         if (this.host != null) {
            if (this.host.charAt(0) == '[') {
               String var2 = this.host.substring(1, this.host.length() - 1);

               try {
                  this.hostIP = new IPAddressName(var2);
               } catch (IOException var6) {
                  throw new IOException("invalid URI name (host portion is not a valid IPv6 address):" + var1);
               }
            } else {
               try {
                  this.hostDNS = new DNSName(this.host);
               } catch (IOException var5) {
                  try {
                     this.hostIP = new IPAddressName(this.host);
                  } catch (Exception var4) {
                     throw new IOException("invalid URI name (host portion is not a valid DNS name, IPv4 address, or IPv6 address):" + var1);
                  }
               }
            }
         }

      }
   }

   public static URIName nameConstraint(DerValue var0) throws IOException {
      String var2 = var0.getIA5String();

      URI var1;
      try {
         var1 = new URI(var2);
      } catch (URISyntaxException var6) {
         throw new IOException("invalid URI name constraint:" + var2, var6);
      }

      if (var1.getScheme() == null) {
         String var3 = var1.getSchemeSpecificPart();

         try {
            DNSName var4;
            if (var3.startsWith(".")) {
               var4 = new DNSName(var3.substring(1));
            } else {
               var4 = new DNSName(var3);
            }

            return new URIName(var1, var3, var4);
         } catch (IOException var5) {
            throw new IOException("invalid URI name constraint:" + var2, var5);
         }
      } else {
         throw new IOException("invalid URI name constraint (should not include scheme):" + var2);
      }
   }

   URIName(URI var1, String var2, DNSName var3) {
      this.uri = var1;
      this.host = var2;
      this.hostDNS = var3;
   }

   public int getType() {
      return 6;
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putIA5String(this.uri.toASCIIString());
   }

   public String toString() {
      return "URIName: " + this.uri.toString();
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof URIName)) {
         return false;
      } else {
         URIName var2 = (URIName)var1;
         return this.uri.equals(var2.getURI());
      }
   }

   public URI getURI() {
      return this.uri;
   }

   public String getName() {
      return this.uri.toString();
   }

   public String getScheme() {
      return this.uri.getScheme();
   }

   public String getHost() {
      return this.host;
   }

   public Object getHostObject() {
      return this.hostIP != null ? this.hostIP : this.hostDNS;
   }

   public int hashCode() {
      return this.uri.hashCode();
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      int var2;
      if (var1 == null) {
         var2 = -1;
      } else if (var1.getType() != 6) {
         var2 = -1;
      } else {
         String var3 = ((URIName)var1).getHost();
         if (var3.equalsIgnoreCase(this.host)) {
            var2 = 0;
         } else {
            Object var4 = ((URIName)var1).getHostObject();
            if (this.hostDNS != null && var4 instanceof DNSName) {
               boolean var5 = this.host.charAt(0) == '.';
               boolean var6 = var3.charAt(0) == '.';
               DNSName var7 = (DNSName)var4;
               var2 = this.hostDNS.constrains(var7);
               if (!var5 && !var6 && (var2 == 2 || var2 == 1)) {
                  var2 = 3;
               }

               if (var5 != var6 && var2 == 0) {
                  if (var5) {
                     var2 = 2;
                  } else {
                     var2 = 1;
                  }
               }
            } else {
               var2 = 3;
            }
         }
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      DNSName var1 = null;

      try {
         var1 = new DNSName(this.host);
      } catch (IOException var3) {
         throw new UnsupportedOperationException(var3.getMessage());
      }

      return var1.subtreeDepth();
   }
}
