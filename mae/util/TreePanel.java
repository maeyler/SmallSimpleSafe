// from TreeDemo 24.9.2002
// simplify & generalize 21.11.02
// add String for TBS  11.1.03
// use Swing 16.2.03

package mae.util;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import javax.swing.*;
import javax.swing.tree.*;

/** Makes a tree structure and displays it as a JTree */
public class TreePanel extends JScrollPane  {
   
   TreeNode root;
   JTree tree; 
   List list = new ArrayList();
   static final int GAP = Scaler.scaledInt(5);
   static int count;
   
   public TreePanel(File f) { show(makeTree(f), true); }
   public TreePanel(String s) { show(makeTree(s), true); }
   public TreePanel(ZipFile f) { show(makeTree(f), true); }
   public TreePanel(Component c) { show(makeTree(c), false); }
   public TreePanel(TreeNode n) { show(n, false); }
   void show(TreeNode n, boolean withIcons) {
//      System.err.println("Constructor"); 
      root = n;
      if (n instanceof DefaultMutableTreeNode) {
         DefaultMutableTreeNode d = (DefaultMutableTreeNode)n;
         String s = "Leaves="+d.getLeafCount()+"  Depth="+d.getDepth();
         d.setUserObject(d.getUserObject()+"  <"+s+">");
      }
      tree = new JTree(n);
      tree.getSelectionModel().setSelectionMode
             (TreeSelectionModel.SINGLE_TREE_SELECTION);
      if (!withIcons) {
         DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
         r.setOpenIcon(null);
         r.setClosedIcon(null);
         r.setLeafIcon(null);
         tree.setCellRenderer(r);
      }
      tree.putClientProperty("JTree.lineStyle", "Angled");
      expandAll(tree);  //, new TreePath(root), root);
      tree.setFont(Scaler.scaledFont("SansSerif", 0, 11));
        //System.err.println(f);
      
      setBackground(Color.lightGray);
      setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP),
         getBorder()));
//      setPreferredSize(new Dimension(300, 358));
      getViewport().setView(tree);
   }
   public TreeNode getRoot() { return root; }
   public JTree getTree() { return tree; }
   public List getList() { return list; }
   static int getLevel(String s) {
      int k = 0;
      while (k<s.length() && s.charAt(k)==' ') k++;
      return k;
   }
   public MutableTreeNode makeTree(String s) {
      final int M = 20; //levels maximum
      DefaultMutableTreeNode[] node
        = new DefaultMutableTreeNode[M];
      try {
         BufferedReader in
            = new BufferedReader(new StringReader(s));
         String t = in.readLine(); 
         list.add(t);
         node[0] = new DefaultMutableTreeNode(t);
         t = in.readLine();
         int prev = 0;
         while (t != null) {
            int k = getLevel(t);
            if (k >= M) 
               throw new RuntimeException("too many levels");
            DefaultMutableTreeNode n 
            = new DefaultMutableTreeNode(t.substring(k));
            node[k-1].add(n); list.add(t.substring(k));
            node[k] = n; prev = k;
            t = in.readLine();
         }
         in.close();
      }
      catch (IOException x) {
         System.err.println(x.getMessage());
         return null;
      }
      return node[0];
   }
   public MutableTreeNode makeTree(File f) {
      list.add(f);
      DefaultMutableTreeNode node;
      String name = f.getName();
      boolean isDir = f.isDirectory();
      node = new DefaultMutableTreeNode(null, isDir);
      if (!isDir) {
         name += ": "+f.length();
      } else {
         File[] A = f.listFiles();
         name += " ("+A.length+")";
         for (int i=0; i<A.length; i++)
            node.add(makeTree(A[i]));
      }
      node.setUserObject(name);
      return node;
   }
   public MutableTreeNode makeTree(Component c) {
      list.add(c);
      DefaultMutableTreeNode node;
      String name = getClassName(c);
      if (c.getName() != null) 
         name += ": "+c.getName();
      boolean isContainer = (c instanceof Container);
      node = new DefaultMutableTreeNode(name, isContainer);
      if (!isContainer) return node;
      Container x = (Container)c;
      if (x.getLayout() != null) 
         name += " ("+getClassName(x.getLayout())+")";
      node.setUserObject(name);
      for (int i=0; i<x.getComponentCount(); i++)
         node.add(makeTree(x.getComponent(i)));
      return node;
   }
   static void insert(DefaultMutableTreeNode n, String s) {
      int k = s.indexOf('/');
      if (k == s.length()-1) return;
      if (k<0) {
         n.add(new DefaultMutableTreeNode(s, false));
         return;
      } 
      String dir = s.substring(0, k);
      DefaultMutableTreeNode node = null;
      for (int i=0; i<n.getChildCount(); i++) {
         node = (DefaultMutableTreeNode)n.getChildAt(i);
         if (node.getUserObject().equals(dir)) break;
         node = null;
      }
      if (node == null) {
         node = new DefaultMutableTreeNode(dir, true);
         n.add(node);
      }
      insert(node, s.substring(k+1));
   }
   public MutableTreeNode makeTree(ZipFile f) {
      DefaultMutableTreeNode root;
      String name = f.getName();
      root = new DefaultMutableTreeNode(name, true);
      Enumeration e = f.entries();
      for (int i=0; e.hasMoreElements(); i++) {
         ZipEntry z = (ZipEntry)e.nextElement();
         if (z.isDirectory()) continue;
         insert(root, z.getName()+": "+z.getSize());
         list.add(z);
      }
      return root;
   }
   public static void expandAll(JTree t) {
      TreeNode n = (TreeNode)t.getModel().getRoot();
      expandAll(t, new TreePath(n), n);
   }
   static void expandAll(JTree t, TreePath p, TreeNode n) {
      t.expandPath(p);
      for (int i=0; i<n.getChildCount(); i++) {
         TreeNode c = n.getChildAt(i);
         int k = c.getChildCount();
         final String SCROLL = "JScrollPane$"; //don't expand JScrollPane
//            "JScrollPane (ScrollPaneLayout$UIResource)";
         if (!c.toString().startsWith(SCROLL)
            && k>0 && k<10)    //don't expand large nodes
            expandAll(t, p.pathByAddingChild(c), c);
      }
   }
   static String getClassName(Object x) {
      String s = x.getClass().getName();
      int k = s.length();
      k = s.lastIndexOf('.', k-1);
      return s.substring(k+1);
   }
   public static JTree display(File f) {
      return show(new TreePanel(f));
   }
   public static JTree display(String s) {
      return show(new TreePanel(s));
   }
   public static JTree display(ZipFile f) {
      return show(new TreePanel(f));
   }
   public static JTree display(Component c) {
      return show(new TreePanel(c));
   }
   public static JTree display(TreeNode n) {
      return show(new TreePanel(n));
   }
   public static JTree show(TreePanel p) {
      int d = (JFrame.getFrames().length == 0)?
         JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE;
      count++;
      JFrame f = new JFrame("TreePanel "+count);
      f.setDefaultCloseOperation(d);
      f.setContentPane(p);
      f.pack(); f.setVisible(true); 
      return p.getTree();
   }

   Object[] toArray() {//NOT USED
      List L = new ArrayList();
      L.add(root);  addTo(L, root);
      return L.toArray();
   }
   static void addTo(List L, TreeNode c) {
       for (int i=0; i<c.getChildCount(); i++) {
           TreeNode ci = c.getChildAt(i);
           L.add(ci);
           if (!ci.isLeaf()) addTo(L, ci);
       }
   }

   public static String fileContents(File f) {
      try {
         return streamContents(new FileInputStream(f));
      } catch (Exception x) {
         System.err.println(x); 
         return null;
      }
   }
   public static String streamContents(InputStream in) 
         throws IOException {
      int n = in.available();
      byte[] buf = new byte[n];
      in.read(buf);
      in.close();
      return new String(buf);
   }
   public static void main(String[] args) {
      System.out.println("Display current directory");
      display(new File("."));
      System.out.println("Display JFrame");
      display(JFrame.getFrames()[0]);
      File f = new File("Sample.txt");
      if (!f.exists()) return;
      System.out.println("Display "+f);
      display(fileContents(f));
   }
}
