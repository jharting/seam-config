package org.jboss.seam.config.xml.model;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.seam.config.xml.core.BeanResult;
import org.jboss.seam.config.xml.fieldset.FieldValue;
import org.jboss.seam.config.xml.fieldset.InlineBeanFieldValue;
import org.jboss.seam.config.xml.fieldset.InlineBeanIdCreator;
import org.jboss.seam.config.xml.fieldset.InlineBeanQualifier;
import org.jboss.seam.config.xml.util.TypeOccuranceInformation;
import org.jboss.seam.config.xml.util.XmlConfigurationException;

public class ValuesXmlItem extends AbstractXmlItem {

    private Set<Integer> syntheticQualifierIds = new HashSet<Integer>();

    public ValuesXmlItem(XmlItem parent, String document, int lineno) {
        super(XmlItemType.VALUES, parent, null, null, null, document, lineno);
    }

    @Override
    public Set<TypeOccuranceInformation> getAllowedItem() {
        return Collections.singleton(TypeOccuranceInformation.of(XmlItemType.CLASS, null, null));
    }

    public List<BeanResult<?>> getBeanResults(BeanManager manager) {
        List<BeanResult<?>> results = new ArrayList<BeanResult<?>>();

        List<ClassXmlItem> inlineBeans = getChildrenOfType(ClassXmlItem.class);

        for (ClassXmlItem inlineBean : inlineBeans) {
            for (AnnotationXmlItem i : inlineBean.getChildrenOfType(AnnotationXmlItem.class)) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotation = (Class<? extends Annotation>) i.getJavaClass();

                if (manager.isQualifier(annotation)) {
                    throw new XmlConfigurationException("Cannot define qualifiers on inline beans, Qualifier: "
                            + annotation.getName(), i.getDocument(), i.getLineno());
                } else if (manager.isScope(annotation) && annotation != Dependent.class) {
                    throw new XmlConfigurationException("Inline beans must have @Dependent scope, Scope: "
                            + annotation.getName(), i.getDocument(), i.getLineno());
                }
            }

            int syntheticQualifierId = InlineBeanIdCreator.getId();
            AnnotationXmlItem syntheticQualifier = new AnnotationXmlItem(this, InlineBeanQualifier.class, ""
                    + syntheticQualifierId, Collections.<String, String> emptyMap(), getDocument(), getLineno());
            inlineBean.addChild(syntheticQualifier);

            results.add(inlineBean.createBeanResult(manager));
            syntheticQualifierIds.add(syntheticQualifierId);
        }
        return results;
    }

    public List<FieldValue> getValues() {
        List<FieldValue> values = new ArrayList<FieldValue>();
        for (int syntheticQualifierId : syntheticQualifierIds) {
            values.add(new InlineBeanFieldValue(syntheticQualifierId));
        }
        return values;
    }
}
