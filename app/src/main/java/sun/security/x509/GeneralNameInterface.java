package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerOutputStream;

public interface GeneralNameInterface {
   int NAME_ANY = 0;
   int NAME_RFC822 = 1;
   int NAME_DNS = 2;
   int NAME_X400 = 3;
   int NAME_DIRECTORY = 4;
   int NAME_EDI = 5;
   int NAME_URI = 6;
   int NAME_IP = 7;
   int NAME_OID = 8;
   int NAME_DIFF_TYPE = -1;
   int NAME_MATCH = 0;
   int NAME_NARROWS = 1;
   int NAME_WIDENS = 2;
   int NAME_SAME_TYPE = 3;

   int getType();

   void encode(DerOutputStream var1) throws IOException;

   int constrains(GeneralNameInterface var1) throws UnsupportedOperationException;

   int subtreeDepth() throws UnsupportedOperationException;
}
