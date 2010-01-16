/*
 * Distributed under the LGPL License
 * 
 */
package org.jboss.seam.xml.parser.namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.xml.model.XmlItem;

/**
 * Namespace resolver that searches through a list of packages
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class CompositeNamespaceElementResolver implements NamespaceElementResolver
{

   Set<String> notFound = new HashSet<String>();
   List<PackageNamespaceElementResolver> resolvers = new ArrayList<PackageNamespaceElementResolver>();

   public CompositeNamespaceElementResolver(Collection<String> packages)
   {
      for (String s : packages)
      {
         resolvers.add(new PackageNamespaceElementResolver(s));
      }
   }

   public CompositeNamespaceElementResolver(String[] packages)
   {
      for (String s : packages)
      {
         resolvers.add(new PackageNamespaceElementResolver(s));
      }
   }

   public XmlItem getItemForNamespace(String item, XmlItem parent, String innerText, Map<String, String> attributes) throws InvalidElementException
   {
      if (notFound.contains(item))
      {
         return null;
      }

      for (PackageNamespaceElementResolver p : resolvers)
      {
         XmlItem xi = p.getItemForNamespace(item, parent, innerText, attributes);
         if (xi != null)
         {
            return xi;
         }
      }
      notFound.add(item);
      return null;
   }

}
