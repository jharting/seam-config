/*
 * Distributed under the LGPL License
 * 
 */
package org.jboss.seam.xml.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;

/**
 * Document Provider that loads XML documents from the classpath
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class ClassPathXmlDocumentProvider implements XmlDocumentProvider
{

   static final String[] DEFAULT_RESOURCES = { "seam-beans.xml", "META-INF/seam-beans.xml", "META-INF/beans.xml"};

   final String[] resources;

   InputStream stream;

   public ClassPathXmlDocumentProvider()
   {
      resources = DEFAULT_RESOURCES;
   }

   public ClassPathXmlDocumentProvider(String[] resources)
   {
      this.resources = resources;
   }

   List<URL> docs;

   ListIterator<URL> iterator;

   DocumentBuilderFactory factory;
   DocumentBuilder builder;

   public void open()
   {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(true);
      try
      {
         builder = factory.newDocumentBuilder();
      }
      catch (ParserConfigurationException e1)
      {
         throw new RuntimeException(e1);
      }
      docs = new ArrayList<URL>();
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null)
      {
         cl = getClass().getClassLoader();
      }
      for (String i : resources)
      {
         try
         {
            Enumeration<URL> e = cl.getResources(i);
            while (e.hasMoreElements())
            {
               docs.add(e.nextElement());
            }
            iterator = docs.listIterator();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public void close()
   {
      if (stream != null)
      {
         try
         {
            stream.close();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   public XmlDocument getNextDocument()
   {
      if (stream != null)
      {
         try
         {
            stream.close();
            stream = null;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      
      try
      {
         while(iterator.hasNext())
         {
            final URL url = iterator.next();
            //ignore empty files
            InputStream test = null;
            try
            {
                test = url.openStream();
                if(test.available() == 0)
                {
                   continue;
                }
            }
            finally
            {
               if(test != null)
               {
                  test.close();
               }
            }
            
            return new XmlDocument()
            {
   
               public InputSource getInputSource()
               {
                  try
                  {
                     stream = url.openStream();
                     return new InputSource(stream);
                  }
                  catch (IOException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
   
               public String getFileUrl()
               {
                  return url.toString();
               }
            };
         }
         return null;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
