package org.jboss.seam.config.xml.fieldset;

import java.lang.annotation.Annotation;
import java.util.Collections;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.seam.config.xml.model.AnnotationXmlItem;
import org.jboss.seam.config.xml.model.ClassXmlItem;
import org.jboss.seam.config.xml.util.XmlConfigurationException;

/**
 * 
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 *
 */
public class InlineBeanFactory {
    
    public static int applySyntheticQualifierToInlineBean(ClassXmlItem beanDefinition, BeanManager manager)
    {
        for (AnnotationXmlItem i : beanDefinition.getChildrenOfType(AnnotationXmlItem.class))
        {
           @SuppressWarnings("unchecked")
           Class<? extends Annotation> annotation = (Class<? extends Annotation>) i.getJavaClass();
           if (manager.isQualifier(annotation))
           {
              throw new XmlConfigurationException("Cannot define qualifiers on inline beans, Qualifier: " + annotation.getName(), i.getDocument(), i.getLineno());
           }
           else if (manager.isScope(annotation) && annotation != Dependent.class)
           {
              throw new XmlConfigurationException("Inline beans must have @Dependent scope, Scope: " + annotation.getName(), i.getDocument(), i.getLineno());
           }
        }
        int syntheticQualifierId = InlineBeanIdCreator.getId();
        AnnotationXmlItem syntheticQualifier = new AnnotationXmlItem(beanDefinition, InlineBeanQualifier.class, "" + syntheticQualifierId, Collections.<String, String>emptyMap(), beanDefinition.getDocument(), beanDefinition.getLineno());
        beanDefinition.addChild(syntheticQualifier);
        return syntheticQualifierId;
    }
}
