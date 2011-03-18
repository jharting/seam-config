package org.jboss.seam.config.xml.test.fieldset;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.solder.core.Veto;

@Veto
public class Cavalry {

    private List<Knight> knights = new ArrayList<Knight>();

    public List<Knight> getKnights() {
        return knights;
    }
}
