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
package osmcd.gui.components;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

<<<<<<< HEAD
import osmb.program.map.IfMapSpace;
import osmb.utilities.UnitSystem;
=======
import osmcb.program.interfaces.IfMapSpace;
import osmcb.program.model.UnitSystem;

public class JDistanceSlider extends JSlider {
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318

public class JDistanceSlider extends JSlider
{
	private static final long serialVersionUID = 1L;

	private final Hashtable<Integer, JLabel> labelTable;

<<<<<<< HEAD
	public JDistanceSlider(IfMapSpace mapSpace, int zoom, int y, UnitSystem unit, int pixelMin, int pixelMax)
	{
=======
	public JDistanceSlider(IfMapSpace mapSpace, int zoom, int y, UnitSystem unit, int pixelMin, int pixelMax) {
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318
		super(pixelMin, pixelMax);
		labelTable = new Hashtable<Integer, JLabel>();

		int diff4 = (pixelMax - pixelMin) / 4;
		int[] labelvalues = new int[]
		{ pixelMin, pixelMin + diff4, pixelMin + 2 * diff4, pixelMin + 3 * diff4, pixelMax };

		for (int i : labelvalues)
		{
			double distance = mapSpace.horizontalDistance(zoom, y, i) * unit.earthRadius * unit.unitFactor;
			String label;
			if (distance > unit.unitFactor)
			{
				distance /= unit.unitFactor;
				label = String.format("%2.0f %s", distance, unit.unitLarge);
			}
			else
				label = String.format("%2.0f %s", distance, unit.unitSmall);
			labelTable.put(new Integer(i), new JLabel(label));
		}
		setPaintTicks(true);
		setMajorTickSpacing(diff4);
		setLabelTable(labelTable);
		setPaintLabels(true);
	}
}
