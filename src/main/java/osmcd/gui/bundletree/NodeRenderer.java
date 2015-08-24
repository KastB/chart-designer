/*******************************************************************************
 * Copyright (c) OSMCB developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package osmcd.gui.bundletree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import osmcb.program.interfaces.IfBundle;
import osmcb.program.interfaces.IfLayer;
import osmcb.utilities.OSMCBUtilities;

public class NodeRenderer implements TreeCellRenderer
{

	private static ImageIcon atlasIcon = new ImageIcon();
	private static ImageIcon layerIcon = new ImageIcon();
	private static ImageIcon mapIcon = new ImageIcon();

	static
	{
		atlasIcon = OSMCBUtilities.loadResourceImageIcon("atlas.png");
		layerIcon = OSMCBUtilities.loadResourceImageIcon("layer.png");
		mapIcon = OSMCBUtilities.loadResourceImageIcon("map.png");
	}

	DefaultTreeCellRenderer atlasRenderer;
	DefaultTreeCellRenderer layerRenderer;
	DefaultTreeCellRenderer mapRenderer;

	public NodeRenderer() {
		atlasRenderer = new SimpleTreeCellRenderer(atlasIcon);
		layerRenderer = new SimpleTreeCellRenderer(layerIcon);
		mapRenderer = new SimpleTreeCellRenderer(mapIcon);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		TreeCellRenderer tcr;
		if (value instanceof IfBundle)
		{
			tcr = atlasRenderer;
		}
		else if (value instanceof IfLayer)
			tcr = layerRenderer;
		else
			tcr = mapRenderer;
		return tcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	}

	protected static class SimpleTreeCellRenderer extends DefaultTreeCellRenderer
	{

		private static final long serialVersionUID = 1L;

		public SimpleTreeCellRenderer(Icon icon) {
			super();
			setIcon(icon);
			setOpenIcon(icon);
			setClosedIcon(icon);
			setLeafIcon(icon);
		}
	}
}
