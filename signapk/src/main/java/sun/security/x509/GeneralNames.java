package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class GeneralNames {
   private final List<GeneralName> names;

   public GeneralNames(DerValue var1) throws IOException {
      this();
      if (var1.tag != 48) {
         throw new IOException("Invalid encoding for GeneralNames.");
      } else if (var1.data.available() == 0) {
         throw new IOException("No data available in passed DER encoded value.");
      } else {
         while(var1.data.available() != 0) {
            DerValue var2 = var1.data.getDerValue();
            GeneralName var3 = new GeneralName(var2);
            this.add(var3);
         }

      }
   }

   public GeneralNames() {
      this.names = new ArrayList();
   }

   public GeneralNames add(GeneralName var1) {
      if (var1 == null) {
         throw new NullPointerException();
      } else {
         this.names.add(var1);
         return this;
      }
   }

   public GeneralName get(int var1) {
      return (GeneralName)this.names.get(var1);
   }

   public boolean isEmpty() {
      return this.names.isEmpty();
   }

   public int size() {
      return this.names.size();
   }

   public Iterator<GeneralName> iterator() {
      return this.names.iterator();
   }

   public List<GeneralName> names() {
      return this.names;
   }

   public void encode(DerOutputStream var1) throws IOException {
      if (!this.isEmpty()) {
         DerOutputStream var2 = new DerOutputStream();
         Iterator var3 = this.names.iterator();

         while(var3.hasNext()) {
            GeneralName var4 = (GeneralName)var3.next();
            var4.encode(var2);
         }

         var1.write((byte)48, (DerOutputStream)var2);
      }
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof GeneralNames)) {
         return false;
      } else {
         GeneralNames var2 = (GeneralNames)var1;
         return this.names.equals(var2.names);
      }
   }

   public int hashCode() {
      return this.names.hashCode();
   }

   public String toString() {
      return this.names.toString();
   }
}
