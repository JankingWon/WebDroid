package sun.security.action;

import java.security.PrivilegedAction;

public class GetBooleanAction implements PrivilegedAction<Boolean> {
   private String theProp;

   public GetBooleanAction(String var1) {
      this.theProp = var1;
   }

   public Boolean run() {
      return Boolean.getBoolean(this.theProp);
   }
}
