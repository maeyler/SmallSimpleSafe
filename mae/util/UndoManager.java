// Author: Eyler -- 11.8.2004  from Fide

package mae.util;

import java.lang.reflect.Field;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.Element;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.lang.reflect.Constructor;

public class UndoManager extends javax.swing.undo.UndoManager {
   JTextComponent src;  //Document doc;
   public static final int MAX = 10;
   public static final char MORE = '~';
   public static final String 
      UNDO = "Undo", REDO = "Redo";
   public static final EventType 
      CHANGE = EventType.CHANGE,
      REMOVE = EventType.REMOVE,
      INSERT = EventType.INSERT,
      MOVE = newEventType("MOVE");
   public static EventType newEventType(String s) {
      Object[] arg = { s };
      Class[] typ = { s.getClass() };
      String t = "javax.swing.event.DocumentEvent$EventType";
      try {  //never say never: use private Constructor
         Class cls = Class.forName(t);
         Constructor c = cls.getDeclaredConstructor(typ);
         //Constructor c = cls.getDeclaredConstructors()[0];
         c.setAccessible(true);
         return (EventType)c.newInstance(arg);
      } catch (Exception x) {
         System.err.println(x);
         return null;
      }
   }

   public UndoManager(JTextComponent c) { 
      src = c; 
      src.getDocument().addUndoableEditListener(this); 
      src.getInputMap().put(
         KeyStroke.getKeyStroke("control Z"), UNDO
      ); 
      src.getActionMap().put(UNDO, new Act(UNDO));
      src.getInputMap().put(
         KeyStroke.getKeyStroke("control Y"), REDO
      ); 
      src.getActionMap().put(REDO, new Act(REDO));
   }
   public boolean addEdit(UndoableEdit u2) {
      if (!(u2 instanceof DocumentEvent)) try {
          u2 = getEventFromWrapper(u2); //V2.08  by B E Harmansa
      } catch (Exception e) {
          System.err.println("Wrong event type "+u2); 
          return false;
	  }
      UndoableEdit u1 = editToBeUndone();
      boolean added;
      if (u1 == null) added = false;
      else {
         try {
            added = u1.addEdit(u2);
         } catch (UndoMoveException ume) {
            added = replaceEdit(ume.getCompound());
         }
      }
      //if (!added) super.addEdit(makeCompound(u2));
      added = added || super.addEdit(makeCompound(u2));
      adjustActions(); return added;
   }

	static DefaultDocumentEvent getEventFromWrapper(UndoableEdit u2) 
            throws Exception {   //Contribution by B E Harmansa
		Field f = u2.getClass().getDeclaredField("dde");
		f.setAccessible(true);
		return (DefaultDocumentEvent)f.get(u2);
	}
   static Compound makeCompound(UndoableEdit u) {
      DocumentEvent d = (DocumentEvent)u;
      if (d.getType() == REMOVE) 
         return new Del(d);
      else //d.getType() == INSERT
         return new Add(d);
   }
   /*mpound combine(UndoableEdit u1, UndoableEdit u2) {
      DocumentEvent d2 = (DocumentEvent)u2;
      Compound cmp = makeCompound(d2);
      if (u1 == null) return cmp;
      EventType t2 = d2.getType();
      int p2 = d2.getOffset();
      int n2 = d2.getLength();
      DocumentEvent d1 = (DocumentEvent)u1;
      EventType t1 = d1.getType();
      int p1 = d1.getOffset();
      int n1 = d1.getLength();
      if (t2==REMOVE) {
         if (!(t1==INSERT && n1==n2 && p1!=p2))
            return cmp;
         //edits.remove(d1); 
         edits.removeElementAt(edits.size()-1);
         return new Move(d1, d2);
      } else {//t2==INSERT
         final char CR = '\n';
         final Document doc = src.getDocument();
         if (u1 instanceof Add && n2==1 && p2==p1+n1 
          && charAt(doc, p2)!=CR && u1.addEdit(u2)) 
              return (Add)u1;
         else return cmp;
      }
   }
   public static char charAt(Document d, int p) {
      return getText(d, p, 1).charAt(0);
   }*/
   public static String getText(Document d, int p, int n) {
      try {
         return d.getText(p, n);
      } catch (Exception x) {
         return "";
      }
   }
   public boolean replaceEdit(UndoableEdit u2) {
      UndoableEdit u1 = editToBeUndone();
      int k = edits.lastIndexOf(u1);
      if (k < 0) return false;
      edits.set(k, u2); 
      //u1.die();   kills the event within u1
      return true;
   }
   public void undo() {
      UndoableEdit u = editToBeUndone();
      if (u == null) return;
      super.undo();
      adjustActions();
      DocumentEvent d = (DocumentEvent)u;
      int i = d.getOffset(), j = i;
      if (d.getType() != EventType.INSERT) j += d.getLength();
      src.select(i, j);
   }
   public void redo() {
      UndoableEdit u = editToBeRedone();
      if (u == null) return;
      super.redo();
      adjustActions();
      DocumentEvent d = (DocumentEvent)u;
      int i = d.getOffset(), j = i;
      if (d.getType() != EventType.REMOVE) j += d.getLength();
      src.select(i, j);
   }
   public void discardAllEdits() {
      super.discardAllEdits();
      adjustActions();
   }
   public Action getUndoAction() {
      return src.getActionMap().get(UNDO);
   }
   public Action getRedoAction() {
      return src.getActionMap().get(REDO);
   }
   void adjustActions() {
      final String desc = Action.SHORT_DESCRIPTION;
      Action un = getUndoAction();
      if (un != null) {
         un.setEnabled(canUndo());
         un.putValue(desc, getUndoPresentationName());
      }
      Action re = getRedoAction();
      if (re != null) {
         re.setEnabled(canRedo());
         re.putValue(desc, getRedoPresentationName());
      }
   }
   public UndoableEdit lastEdit() {
      return super.lastEdit();
   }
   public UndoableEdit editToBeUndone() {
      return super.editToBeUndone();
   }
   public UndoableEdit get(int i) {
      return (UndoableEdit)edits.get(i);
   }
   public UndoableEdit[] edits() {
      UndoableEdit[] a = new UndoableEdit[edits.size()];
      edits.toArray(a); return a;
   }
   public String toString() {
      int n = edits.size();
      //UndoableEdit u = editToBeUndone();
      return "["+n+"] "+editToBeUndone();
      //(u==null? "" : u.getPresentationName());
   }

   class Act extends javax.swing.AbstractAction {
      public Act(String s) { super(s); }
      public void actionPerformed(ActionEvent e) {
         String cmd = (String)getValue(NAME);
         //System.out.println("Action: "+cmd);
         if (cmd.equals(UNDO)) undo();
         else if (cmd.equals(REDO)) redo();
         src.requestFocus(); 
      }
   }

   public static class Add extends Compound {
      String str;
      public Add(DocumentEvent d) {
         super(d); typ = INSERT;
         str = getText(doc, off, len);
         if (str.length() > MAX) 
            str = str.substring(0, MAX)+MORE;
      }
      public boolean addEdit(UndoableEdit u) {
         //if (!isInProgress()) return false;
         DocumentEvent d2 = (DocumentEvent)u;
         EventType t2 = d2.getType();
         int p2 = d2.getOffset();
         int n2 = d2.getLength();
         final char cc = getText(doc, p2, 1).charAt(0);
         final char CR = '\n';
         if (t2==REMOVE && n2==len && p2!=off && size()==1) {
            DocumentEvent d1 = (DocumentEvent)Add.this.get(0);
            throw new UndoMoveException(new Move(d1, d2));
         }
         if (t2==INSERT && n2==1 && p2==off+len && cc!=CR) {
            addChar(cc); 
            //Add.this.edits.add(d2);
            return super.addEdit((UndoableEdit)d2); 
         } 
         //if u is not added, nothing will be any more
         end(); return false;
      }
      void addChar(char c) {
         len++; 
         int n = str.length();
         if (n <= MAX) str = str + (n==MAX? MORE : c);
      }
      public String getPresentationName() {
         return "addition";
      }
      public String toString() {
         return "Add "+off+"/"+len+": "+str;
      }
   }
   public static class Del extends Compound {
      public Del(DocumentEvent d) {
         super(d); typ = REMOVE; end();
      }
      public String getPresentationName() {
         return "deletion";
      }
      public String toString() {
         return "Delete "+off+"/"+len;
      }
   }
   public static class Move extends Compound {
      int p1, p2;
      public Move(DocumentEvent d1, DocumentEvent d2) {
         super(d1); typ = MOVE;
         //Move.this.edits.add(d2); 
         addEdit((UndoableEdit)d2); end();  
         p1 = d1.getOffset(); 
         p2 = d2.getOffset();
         if (p1 == p2) throw new 
            RuntimeException("Move: same location");
         if (p1 < p2) p2 -= len; 
         else p1 -= len; 
      }
      public void undo() {
         super.undo(); off = p2; 
      }
      public void redo() {
         super.redo(); off = p1;
      }
      public String getPresentationName() {
         return "movement";
      }
      public String toString() {
         return "Move "+p2+"/"+len+" to "+p1;
      }
   }
   public static abstract class Compound 
      extends CompoundEdit implements DocumentEvent {

      int off; int len; Document doc; EventType typ; 
      public Compound(DocumentEvent d) {
         off = d.getOffset(); 
         len = d.getLength();
         doc = d.getDocument();
         super.addEdit((UndoableEdit)d);
         //Compound.this.edits.add(d); 
      }
      // --- List methods (incomplete) ---------------------
      public int size() {
         return Compound.this.edits.size();
      }
      public UndoableEdit get(int i) {
         return (UndoableEdit)Compound.this.edits.get(i);
      }
      public Object[] edits() {
         return Compound.this.edits.toArray();
      }
      // --- CompoundEdit methods --------------------------
      public boolean canUndo() {
         List lst = Compound.this.edits;
         UndoableEdit u = (UndoableEdit)lst.get(lst.size()-1);
         return u.canUndo();
      }
      public boolean canRedo() {
         List lst = Compound.this.edits;
         UndoableEdit u = (UndoableEdit)lst.get(0);
         return u.canRedo();
      }
      public void undo() {
         end(); super.undo();
      }
      String description() {
         return getPresentationName()+" of "+len+" chars";
      }
      public String getUndoPresentationName() {
         return UNDO +" "+ description();
      }
      public String getRedoPresentationName() {
         return REDO +" "+ description();
      }
      // --- DocumentEvent methods --------------------------
      public EventType getType() {
         return typ;
      }
      public int getOffset() {
         return off;
      }
      public int getLength() {
         return len;
      }
      public Document getDocument() {
         return doc;
      }
      public DocumentEvent.ElementChange getChange(Element e) {
         return null;
      }
   }
}

class UndoMoveException extends RuntimeException {
   UndoManager.Move mov;
   UndoMoveException(UndoManager.Move m) { mov = m; }
   UndoManager.Compound getCompound() { return mov; }
}
