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
package osmcd.gui.mapview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.List;

import osmb.mapsources.MP2MapSpace;
//W #mapSpaceimport osmb.program.map.IfMapSpace;

/**
 * Displays a polygon on the map - only for testing purposes
 */
public class PolygonSelectionLayer implements IfMapLayer
{
	private final AbstractPolygonSelectionMapController mapController;

	public PolygonSelectionLayer(AbstractPolygonSelectionMapController mapController)
	{
		this.mapController = mapController;
	}

	@Override
	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY)
	{
		 // W #mapSpace IfMapSpace mapSpace = map.getMapSource().getMapSpace();
		g.setColor(Color.RED);
		Point lastPoint = null;
		List<Point> pointList = mapController.getPolygonPoints();
		if (pointList.size() == 0)
			return;
		AffineTransform at = g.getTransform();
		try
		{
			g.translate(-minX, -minY);
			for (Point p : pointList)
			{
				Point p1 = MP2MapSpace.changeZoom(p, map.getMaxZoom(), zoom); // W #mapSpace mapSpace.changeZoom(p, map.getMaxZoom(), zoom);
				g.fillOval(p1.x - 3, p1.y - 3, 6, 6);
				if (lastPoint != null)
				{
					g.drawLine(p1.x, p1.y, lastPoint.x, lastPoint.y);
				}
				lastPoint = p1;
			}
			// Draw line back to the starting point
			Point p1 = MP2MapSpace.changeZoom(pointList.get(0), map.getMaxZoom(), zoom); // W #mapSpace 
			g.drawLine(p1.x, p1.y, lastPoint.x, lastPoint.y);
		}
		finally
		{
			g.setTransform(at);
		}
	}
}
