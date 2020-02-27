package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class GeneralSubtrees implements Cloneable {
   private final List<GeneralSubtree> trees;
   private static final int NAME_DIFF_TYPE = -1;
   private static final int NAME_MATCH = 0;
   private static final int NAME_NARROWS = 1;
   private static final int NAME_WIDENS = 2;
   private static final int NAME_SAME_TYPE = 3;

   public GeneralSubtrees() {
      this.trees = new ArrayList();
   }

   private GeneralSubtrees(GeneralSubtrees var1) {
      this.trees = new ArrayList(var1.trees);
   }

   public GeneralSubtrees(DerValue var1) throws IOException {
      this();
      if (var1.tag != 48) {
         throw new IOException("Invalid encoding of GeneralSubtrees.");
      } else {
         while(var1.data.available() != 0) {
            DerValue var2 = var1.data.getDerValue();
            GeneralSubtree var3 = new GeneralSubtree(var2);
            this.add(var3);
         }

      }
   }

   public GeneralSubtree get(int var1) {
      return (GeneralSubtree)this.trees.get(var1);
   }

   public void remove(int var1) {
      this.trees.remove(var1);
   }

   public void add(GeneralSubtree var1) {
      if (var1 == null) {
         throw new NullPointerException();
      } else {
         this.trees.add(var1);
      }
   }

   public boolean contains(GeneralSubtree var1) {
      if (var1 == null) {
         throw new NullPointerException();
      } else {
         return this.trees.contains(var1);
      }
   }

   public int size() {
      return this.trees.size();
   }

   public Iterator<GeneralSubtree> iterator() {
      return this.trees.iterator();
   }

   public List<GeneralSubtree> trees() {
      return this.trees;
   }

   public Object clone() {
      return new GeneralSubtrees(this);
   }

   public String toString() {
      String var1 = "   GeneralSubtrees:\n" + this.trees.toString() + "\n";
      return var1;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      int var3 = 0;

      for(int var4 = this.size(); var3 < var4; ++var3) {
         this.get(var3).encode(var2);
      }

      var1.write((byte)48, (DerOutputStream)var2);
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof GeneralSubtrees)) {
         return false;
      } else {
         GeneralSubtrees var2 = (GeneralSubtrees)var1;
         return this.trees.equals(var2.trees);
      }
   }

   public int hashCode() {
      return this.trees.hashCode();
   }

   private GeneralNameInterface getGeneralNameInterface(int var1) {
      return getGeneralNameInterface(this.get(var1));
   }

   private static GeneralNameInterface getGeneralNameInterface(GeneralSubtree var0) {
      GeneralName var1 = var0.getName();
      GeneralNameInterface var2 = var1.getName();
      return var2;
   }

   private void minimize() {
      for(int var1 = 0; var1 < this.size() - 1; ++var1) {
         GeneralNameInterface var2 = this.getGeneralNameInterface(var1);
         boolean var3 = false;
         int var4 = var1 + 1;

         label30:
         while(var4 < this.size()) {
            GeneralNameInterface var5 = this.getGeneralNameInterface(var4);
            switch(var2.constrains(var5)) {
            case 0:
               var3 = true;
               break label30;
            case 1:
               this.remove(var4);
               --var4;
            case -1:
            case 3:
               ++var4;
               break;
            case 2:
               var3 = true;
            default:
               break label30;
            }
         }

         if (var3) {
            this.remove(var1);
            --var1;
         }
      }

   }

   private GeneralSubtree createWidestSubtree(GeneralNameInterface var1) {
      try {
         GeneralName var2;
         switch(var1.getType()) {
         case 0:
            ObjectIdentifier var3 = ((OtherName)var1).getOID();
            var2 = new GeneralName(new OtherName(var3, (byte[])null));
            break;
         case 1:
            var2 = new GeneralName(new RFC822Name(""));
            break;
         case 2:
            var2 = new GeneralName(new DNSName(""));
            break;
         case 3:
            var2 = new GeneralName(new X400Address((byte[])null));
            break;
         case 4:
            var2 = new GeneralName(new X500Name(""));
            break;
         case 5:
            var2 = new GeneralName(new EDIPartyName(""));
            break;
         case 6:
            var2 = new GeneralName(new URIName(""));
            break;
         case 7:
            var2 = new GeneralName(new IPAddressName((byte[])null));
            break;
         case 8:
            var2 = new GeneralName(new OIDName(new ObjectIdentifier((int[])null)));
            break;
         default:
            throw new IOException("Unsupported GeneralNameInterface type: " + var1.getType());
         }

         return new GeneralSubtree(var2, 0, -1);
      } catch (IOException var4) {
         throw new RuntimeException("Unexpected error: " + var4, var4);
      }
   }

   public GeneralSubtrees intersect(GeneralSubtrees var1) {
      if (var1 == null) {
         throw new NullPointerException("other GeneralSubtrees must not be null");
      } else {
         GeneralSubtrees var2 = new GeneralSubtrees();
         GeneralSubtrees var3 = null;
         if (this.size() == 0) {
            this.union(var1);
            return null;
         } else {
            this.minimize();
            var1.minimize();

            int var4;
            boolean var7;
            int var8;
            for(var4 = 0; var4 < this.size(); ++var4) {
               GeneralNameInterface var5 = this.getGeneralNameInterface(var4);
               boolean var6 = false;
               var7 = false;
               var8 = 0;

               GeneralSubtree var9;
               GeneralNameInterface var10;
               label82:
               while(var8 < var1.size()) {
                  var9 = var1.get(var8);
                  var10 = getGeneralNameInterface(var9);
                  switch(var5.constrains(var10)) {
                  case 0:
                  case 2:
                     var7 = false;
                     break label82;
                  case 1:
                     this.remove(var4);
                     --var4;
                     var2.add(var9);
                     var7 = false;
                     break label82;
                  case 3:
                     var7 = true;
                  case -1:
                  default:
                     ++var8;
                  }
               }

               if (var7) {
                  boolean var16 = false;

                  for(int var17 = 0; var17 < this.size(); ++var17) {
                     var10 = this.getGeneralNameInterface(var17);
                     if (var10.getType() == var5.getType()) {
                        for(int var11 = 0; var11 < var1.size(); ++var11) {
                           GeneralNameInterface var12 = var1.getGeneralNameInterface(var11);
                           int var13 = var10.constrains(var12);
                           if (var13 == 0 || var13 == 2 || var13 == 1) {
                              var16 = true;
                              break;
                           }
                        }
                     }
                  }

                  if (!var16) {
                     if (var3 == null) {
                        var3 = new GeneralSubtrees();
                     }

                     var9 = this.createWidestSubtree(var5);
                     if (!var3.contains(var9)) {
                        var3.add(var9);
                     }
                  }

                  this.remove(var4);
                  --var4;
               }
            }

            if (var2.size() > 0) {
               this.union(var2);
            }

            for(var4 = 0; var4 < var1.size(); ++var4) {
               GeneralSubtree var14 = var1.get(var4);
               GeneralNameInterface var15 = getGeneralNameInterface(var14);
               var7 = false;
               var8 = 0;

               label71:
               while(var8 < this.size()) {
                  GeneralNameInterface var18 = this.getGeneralNameInterface(var8);
                  switch(var18.constrains(var15)) {
                  case -1:
                     var7 = true;
                  default:
                     ++var8;
                     break;
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                     var7 = false;
                     break label71;
                  }
               }

               if (var7) {
                  this.add(var14);
               }
            }

            return var3;
         }
      }
   }

   public void union(GeneralSubtrees var1) {
      if (var1 != null) {
         int var2 = 0;

         for(int var3 = var1.size(); var2 < var3; ++var2) {
            this.add(var1.get(var2));
         }

         this.minimize();
      }

   }

   public void reduce(GeneralSubtrees var1) {
      if (var1 != null) {
         int var2 = 0;

         for(int var3 = var1.size(); var2 < var3; ++var2) {
            GeneralNameInterface var4 = var1.getGeneralNameInterface(var2);

            for(int var5 = 0; var5 < this.size(); ++var5) {
               GeneralNameInterface var6 = this.getGeneralNameInterface(var5);
               switch(var4.constrains(var6)) {
               case -1:
               case 2:
               case 3:
               default:
                  break;
               case 0:
                  this.remove(var5);
                  --var5;
                  break;
               case 1:
                  this.remove(var5);
                  --var5;
               }
            }
         }

      }
   }
}
