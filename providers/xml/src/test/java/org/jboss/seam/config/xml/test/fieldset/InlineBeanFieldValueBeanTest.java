/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.config.xml.test.fieldset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.seam.config.xml.test.AbstractXMLTest;
import org.junit.Test;

public class InlineBeanFieldValueBeanTest extends AbstractXMLTest
{

   @Override
   protected String getXmlFileName()
   {
      return "inline-bean-field-value-beans.xml";
   }

   @Test
   public void simpleInlineBeanTest()
   {
      Knight knight = getReference(Knight.class, org.jboss.weld.literal.DefaultLiteral.INSTANCE);
      Assert.assertTrue(knight.getSword().getType().equals("sharp"));
      Assert.assertTrue(knight.getHorse().getShoe() != null);
      Assert.assertTrue(knight.getHorse().getName().equals("billy"));
   }
   
   @Test
   public void testInlineCollectionWithoutValueElements()
   {
       @SuppressWarnings("serial")
       Map<String, String> expectedValues = new HashMap<String, String>()
       {
           {
               put("Ace", "sharp");
               put("Apples", "blunt");
               put("Polly", "blunt");
           }
       };
       
       Cavalry cavalry = getReference(Cavalry.class);
       validateKnights(expectedValues, cavalry.getKnights());
   }
   
   private void validateKnights(Map<String, String> expectedValues, List<Knight> knights)
   {
       Assert.assertEquals(expectedValues.size(), knights.size());
       for (Knight knight : knights)
       {
           String expectedSwordType = expectedValues.get(knight.getHorse().getName());
           if (expectedSwordType != null && expectedSwordType.equals(knight.getSword().getType()))
           {
               expectedValues.remove(knight.getHorse().getName());
           }
       }
       Assert.assertTrue(expectedValues.isEmpty());
   }
}
