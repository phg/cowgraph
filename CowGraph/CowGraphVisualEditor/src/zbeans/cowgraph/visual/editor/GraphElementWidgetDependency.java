/*
 * Copyright (C) 2011 Michael M&uuml;hlebach <michael at anduin.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package zbeans.cowgraph.visual.editor;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Widget.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zbeans.cowgraph.model.GraphElement;

/**
 * Verantwortlich für updates im GraphElement bei Änderungen im Widget.
 * 
 * 
 * @author Michael M&uuml;hlebach <michael at anduin.ch>
 */
public class GraphElementWidgetDependency implements Dependency, PropertyChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphElementWidgetDependency.class);
    Widget widget;
    GraphElement node;
    boolean suspend;
    private boolean inRevalidation;

    public GraphElementWidgetDependency(Widget widget, GraphElement node) {
        this.widget = widget;
        this.node = node;

        this.node.addPropertyChangeListener(this);
        this.suspend = false;
        this.inRevalidation = false;
    }

    @Override
    public void revalidateDependency() {
        if (inRevalidation) {
            return;
        }

        this.suspend = true;
        this.node.setX(widget.getPreferredLocation().x);
        this.node.setY(widget.getPreferredLocation().y);
        this.suspend = false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (suspend) {
            return;
        }

        if (GraphElement.PROP_X.equals(evt.getPropertyName()) || GraphElement.PROP_Y.equals(evt.getPropertyName())) {
            inRevalidation = true;
            this.widget.setPreferredLocation(new Point((int) this.node.getX(), (int) this.node.getY()));
            if (SwingUtilities.isEventDispatchThread()) {
                this.widget.getScene().repaint();
                this.widget.getScene().validate();
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        widget.getScene().repaint();
                        widget.getScene().validate();
                    }
                });
            }
            inRevalidation = false;
        }
    }
}
