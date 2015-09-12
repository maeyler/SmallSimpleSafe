//from DefaultListModel  12.2.2003

package mae.util;
import java.util.*;
import javax.swing.AbstractListModel;


public class ArrayListModel 
     extends AbstractListModel implements List {

    List data;

    public ArrayListModel() {
      data = new ArrayList();
    }
    public ArrayListModel(Collection c) {
      data = new ArrayList(c.size()); addAll(c);
    }
    public ArrayListModel(Object[] a) {
      data = new ArrayList(a.length); addAll(a);
    }
    public int getSize() {
   	return data.size();
    }
    public Object getElementAt(int i) {
      return data.get(i);
    }
    public int size() {
   	return data.size();
    }
    public boolean isEmpty() {
   	return data.isEmpty();
    }
    public Iterator iterator() {
      return data.iterator();
    }
    public ListIterator listIterator() {
      return data.listIterator();
    }
    public ListIterator listIterator(int i) {
      return data.listIterator(i);
    }
    public boolean contains(Object x) {
   	return data.contains(x);
    }
    public boolean containsAll(Collection c) {
   	return data.containsAll(c);
    }
    public int indexOf(Object x) {
   	return data.indexOf(x);
    }
    public int lastIndexOf(Object x) {
   	return data.lastIndexOf(x);
    }
    public String toString() {
   	return data.toString();
    }
    public Object[] toArray() {
      return data.toArray();
    }
    public Object[] toArray(Object[] a) {
   	return data.toArray(a);
    }
    public Object get(int i) {
   	return data.get(i);
    }
    public Object set(int i, Object x) {
   	Object rv = data.get(i);
   	data.set(i, x);
   	fireContentsChanged(this, i, i);
   	return rv;
    }
    public boolean add(Object x) {
      add(data.size(), x); 
      return true;
    }
    public void add(int i, Object x) {
      data.add(i, x);
      fireIntervalAdded(this, i, i);
    }
    public boolean remove(Object x) {
      int i = data.indexOf(x);
      if (i < 0) return false;
      remove(i);
      return true;
    }
    public Object remove(int i) {
   	Object rv = data.get(i);
   	data.remove(i);
   	fireIntervalRemoved(this, i, i);
   	return rv;
    }
    public void removeRange(int i, int j) {
      if (i>j) return;
      for (int k=j; i<=k; k--) data.remove(k);
//   	data.removeAll(data.subList(i, j+1));
   	fireIntervalRemoved(this, i, j);
    }
    public void clear() {
   	int n = data.size();
   	data.clear();
   	if (n > 0) fireIntervalRemoved(this, 0, n-1);
    }
    public boolean addAll(Object[] a) {
      int n = data.size();
      boolean modified = false;
      for (int i=0; i<a.length; i++) 
         if (data.add(a[i])) modified = true;
      if (!modified) return false;
      int k = data.size()-1;
      fireIntervalAdded(this, n, k);
      return true;
    }
    public boolean addAll(Collection c) {
      return addAll(data.size(), c);
    }
    public boolean addAll(int i, Collection c) {
      int n = c.size();
      boolean modified = data.addAll(i, c);
      if (!modified) return false;
      fireIntervalAdded(this, i, i+n-1);
      return true;
    }
    public boolean removeAll(Collection c) {
      int n = data.size();
      boolean modified = data.removeAll(c);
      if (!modified) return false;
      int k = data.size();
   	fireIntervalRemoved(this, k, n-1);      
   	fireContentsChanged(this, 0, k-1);
      return true;
    }
    public boolean retainAll(Collection c) {
      int n = data.size();
      boolean modified = data.retainAll(c);
      if (!modified) return false;
      int k = data.size();
   	fireIntervalRemoved(this, k, n-1);      
   	fireContentsChanged(this, 0, k-1);
      return true;
    }
    public List subList(int fromIndex, int toIndex) {
      return data.subList(fromIndex, toIndex);
    }
}
