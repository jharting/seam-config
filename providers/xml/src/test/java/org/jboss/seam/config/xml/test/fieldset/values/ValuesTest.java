package org.jboss.seam.config.xml.test.fieldset.values;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.config.xml.test.AbstractXMLTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("serial")
public class ValuesTest extends AbstractXMLTest {

    @Override
    protected String getXmlFileName() {
        return "values-beans.xml";
    }

    @Test
    public void testCollection() {

        Set<String> expectedNames = new HashSet<String>() {
            {
                add("Alpha");
                add("Bravo");
                add("Charlie");
            }
        };

        Foo foo = getReference(Foo.class);
        assertTrue(foo.getSetOfBars() != null);
        validateNames(expectedNames, foo.getSetOfBars());
    }

    @Test
    public void testArray() {
        Set<String> expectedNames = new HashSet<String>() {
            {
                add("Delta");
                add("Echo");
                add("Foxtrot");
            }
        };

        Foo foo = getReference(Foo.class);
        assertTrue(foo.getArrayOfBars() != null);
        assertEquals(3, foo.getArrayOfBars().length);
        validateNames(expectedNames, Arrays.asList(foo.getArrayOfBars()));
    }

    private void validateNames(Collection<String> expectedNames, Collection<Bar> bars) {
        assertEquals(expectedNames.size(), bars.size());
        for (Bar bar : bars) {
            expectedNames.remove(bar.getName());
        }
        assertTrue(expectedNames.isEmpty());
    }
}
