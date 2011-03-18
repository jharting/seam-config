/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.config.xml.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.seam.config.xml.core.BeanResult;
import org.jboss.seam.config.xml.fieldset.ArrayFieldSet;
import org.jboss.seam.config.xml.fieldset.CollectionFieldSet;
import org.jboss.seam.config.xml.fieldset.ConstantFieldValue;
import org.jboss.seam.config.xml.fieldset.ELFieldValue;
import org.jboss.seam.config.xml.fieldset.FieldValue;
import org.jboss.seam.config.xml.fieldset.FieldValueObject;
import org.jboss.seam.config.xml.fieldset.InlineBeanFactory;
import org.jboss.seam.config.xml.fieldset.InlineBeanFieldValue;
import org.jboss.seam.config.xml.fieldset.MapFieldSet;
import org.jboss.seam.config.xml.fieldset.SimpleFieldValue;
import org.jboss.seam.config.xml.util.TypeOccuranceInformation;
import org.jboss.seam.config.xml.util.XmlConfigurationException;
import org.jboss.seam.solder.properties.Property;

public class PropertyXmlItem extends AbstractXmlItem
{
   private final Property<Object> property;
   private final HashSet<TypeOccuranceInformation> allowed = new HashSet<TypeOccuranceInformation>();
   private final Class<?> fieldType;
   private final List<BeanResult<?>> inlineBeans = new ArrayList<BeanResult<?>>();

   private FieldValueObject fieldValue;

   public PropertyXmlItem(XmlItem parent, Property<Object> property, String innerText, String document, int lineno)
   {
      this(parent, property, innerText, null, document, lineno);
   }

   public PropertyXmlItem(XmlItem parent, Property<Object> property, String innerText, Class<?> overridenFieldType, String document, int lineno)
   {
      super(XmlItemType.FIELD, parent, parent.getJavaClass(), innerText, null, document, lineno);
      this.property = property;
      if (overridenFieldType == null)
      {
         this.fieldType = property.getJavaClass();
      }
      else
      {
         this.fieldType = overridenFieldType;
      }
      if (innerText != null && innerText.length() > 0)
      {
         FieldValue fv;
         if (innerText.matches("^#\\{.*\\}$"))
         {
            fv = new ELFieldValue(innerText);
         }
         else
         {
            fv = new ConstantFieldValue(innerText);
         }
         fieldValue = new SimpleFieldValue(parent.getJavaClass(), property, fv, fieldType);
      }
      allowed.add(new TypeOccuranceInformation(XmlItemType.VALUE, null, null));
      allowed.add(new TypeOccuranceInformation(XmlItemType.ANNOTATION, null, null));
      allowed.add(new TypeOccuranceInformation(XmlItemType.ENTRY, null, null));
      allowed.add(new TypeOccuranceInformation(XmlItemType.CLASS, null, null));
   }

   public FieldValueObject getFieldValue()
   {
      return fieldValue;
   }

   @Override
   public boolean resolveChildren(BeanManager manager)
   {
      List<EntryXmlItem> mapEntries = new ArrayList<EntryXmlItem>();
      List<ValueXmlItem> valueEntries = new ArrayList<ValueXmlItem>();
      List<ClassXmlItem> classEntries = new ArrayList<ClassXmlItem>();
      
      if (fieldValue == null)
      {
         for (XmlItem i : children)
         {
            if (i.getType() == XmlItemType.VALUE)
            {
               valueEntries.add((ValueXmlItem) i);
            }
            else if (i.getType() == XmlItemType.ENTRY)
            {
               mapEntries.add((EntryXmlItem) i);
            }
            else if (i.getType() == XmlItemType.CLASS)
            {
                classEntries.add((ClassXmlItem) i);
            }

         }
      }
      if (!mapEntries.isEmpty() || !valueEntries.isEmpty() || !classEntries.isEmpty())
      {
         if (Map.class.isAssignableFrom(getFieldType()))
         {
            if (!valueEntries.isEmpty() || !classEntries.isEmpty())
            {
               throw new XmlConfigurationException("Map fields can only contain <entry> elements Field:" + getDeclaringClass().getName() + '.' + getFieldName(), getDocument(), getLineno());
            }
            if (!mapEntries.isEmpty())
            {
               for (EntryXmlItem entry : mapEntries)
               {
                  // resolve inline beans if nessesary
                  Set<BeanResult<?>> beans = entry.getBeanResults(manager);
                  inlineBeans.addAll(beans);

               }
               fieldValue = new MapFieldSet(property, mapEntries);
            }
         }
         else if (Collection.class.isAssignableFrom(getFieldType()) || getFieldType().isArray())
         {
            List<FieldValue> fieldValues = new ArrayList<FieldValue>();
             
            if (!mapEntries.isEmpty())
            {
               throw new XmlConfigurationException("Collection fields must be set using <value> not <entry> Field:" + getDeclaringClass().getName() + '.' + getFieldName(), getDocument(), getLineno());
            }
            if (!valueEntries.isEmpty())
            {
               for (ValueXmlItem value : valueEntries)
               {
                  // resolve inline beans if nessesary
                  BeanResult<?> result = value.getBeanResult(manager);
                  if (result != null)
                  {
                     inlineBeans.add(result);
                  }
                  fieldValues.add(value.getValue());
               }
            }
            if (!classEntries.isEmpty())
            {
                for (ClassXmlItem item : classEntries)
                {
                    int inlineBeanId = InlineBeanFactory.applySyntheticQualifierToInlineBean(item, manager);
                    inlineBeans.add(item.createBeanResult(manager));
                    fieldValues.add(new InlineBeanFieldValue(inlineBeanId));
                }
            }
            if (getFieldType().isArray())
            {
               fieldValue = new ArrayFieldSet(property, fieldValues);
            }
            else
            {
               fieldValue = new CollectionFieldSet(property, fieldValues);
            }
         }
         else
         {
            if (!mapEntries.isEmpty())
            {
               throw new XmlConfigurationException("Only Map fields can be set using <entry> Field:" + getDeclaringClass().getName() + '.' + getFieldName(), getDocument(), getLineno());
            }
            if (!(valueEntries.size() == 1 ^ classEntries.size() == 1))
            {
               throw new XmlConfigurationException("Non collection fields can have at most a single <value> element Field:" + getDeclaringClass().getName() + '.' + getFieldName(), getDocument(), getLineno());
            }
            
            FieldValue fieldValue = null;
            BeanResult<?> beanResult = null;
            
            if (valueEntries.size() == 1)
            {
                ValueXmlItem value = valueEntries.get(0);
                beanResult = value.getBeanResult(manager);
                fieldValue = value.getValue();
            }
            else
            {
                ClassXmlItem classItem = classEntries.get(0);
                int inlineBeanId = InlineBeanFactory.applySyntheticQualifierToInlineBean(classItem, manager);
                beanResult = classItem.createBeanResult(manager);
                fieldValue = new InlineBeanFieldValue(inlineBeanId);
            }
            
            this.fieldValue = new SimpleFieldValue(parent.getJavaClass(), property, fieldValue, fieldType);
            if (beanResult != null)
            {
               inlineBeans.add(beanResult);
            }
         }
      }
      return true;
   }

   /**
    * Returns the field that corresponds to the property, or null if it does not
    * exist
    * 
    * @return
    */
   public Field getField()
   {
      if (property.getMember() instanceof Field)
      {
         return (Field) property.getMember();
      }
      return org.jboss.seam.config.xml.util.Reflections.getField(parent.getJavaClass(), property.getName());
   }

   public Set<TypeOccuranceInformation> getAllowedItem()
   {
      return allowed;
   }

   public Collection<? extends BeanResult<?>> getInlineBeans()
   {
      return inlineBeans;
   }

   public Class<?> getDeclaringClass()
   {
      return property.getDeclaringClass();
   }

   public String getFieldName()
   {
      return property.getName();
   }

   public Class<?> getFieldType()
   {
      return fieldType;
   }

   public Property<?> getProperty()
   {
      return property;
   }

}
