package org.jboss.seam.config.xml.test.fieldset.values;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Foo {
    private Set<Bar> setOfBars;
    private Bar[] arrayOfBars;

    public Set<Bar> getSetOfBars() {
        return setOfBars;
    }

    public Bar[] getArrayOfBars() {
        return arrayOfBars;
    }
}
