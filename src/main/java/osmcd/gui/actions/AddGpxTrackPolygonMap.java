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
package osmcd.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import osmb.exceptions.InvalidNameException;
import osmb.mapsources.ACMapSource;
import osmb.mapsources.MP2MapSpace;
import osmb.program.ACSettings;
import osmb.program.catalog.IfCatalog;
import osmb.program.map.IfMap;
import osmb.program.map.Layer;
import osmb.program.map.MapPolygon;
import osmb.program.tiles.TileImageParameters;
import osmb.utilities.OSMBStrs;
import osmb.utilities.UnitSystem;
import osmb.utilities.geo.GeoCoordinate;
import osmcd.OSMCDSettings;
import osmcd.OSMCDStrs;
import osmcd.data.gpx.gpx11.TrkType;
import osmcd.data.gpx.gpx11.TrksegType;
import osmcd.data.gpx.interfaces.GpxPoint;
import osmcd.gui.MainFrame;
import osmcd.gui.catalog.JCatalogTree;
import osmcd.gui.components.JDistanceSlider;
import osmcd.gui.gpxtree.GpxEntry;
import osmcd.gui.gpxtree.GpxRootEntry;
import osmcd.gui.gpxtree.TrkEntry;
import osmcd.gui.gpxtree.TrksegEntry;
import osmcd.gui.mapview.MapAreaHighlightingLayer;
import osmcd.program.SelectedZoomLevels;

public class AddGpxTrackPolygonMap implements ActionListener
{
	public static final AddGpxTrackPolygonMap INSTANCE = new AddGpxTrackPolygonMap();

	private MapAreaHighlightingLayer msl = null;

	@Override
	public void actionPerformed(ActionEvent event)
	{
		final MainFrame mg = MainFrame.getMainGUI();
		GpxEntry entry = mg.getSelectedGpx();

		if (entry == null)
			return;

		TrksegType trk = null;
		TrkType t = null;
		if (entry instanceof TrksegEntry)
		{
			trk = ((TrksegEntry) entry).getTrkSeg();
		}
		else if (entry instanceof GpxRootEntry)
		{
			GpxRootEntry re = (GpxRootEntry) entry;
			List<TrkType> tlist = re.getLayer().getGpx().getTrk();
			if (tlist.size() > 1)
			{
				JOptionPane.showMessageDialog(mg, OSMCDStrs.RStr("msg_add_gpx_polygon_too_many_track"));
				return;
			}
			else if (tlist.size() == 1)
				t = tlist.get(0);
		}
		if (entry instanceof TrkEntry)
			t = ((TrkEntry) entry).getTrk();
		if (t != null)
		{
			if (t.getTrkseg().size() > 1)
			{
				JOptionPane.showMessageDialog(mg, OSMCDStrs.RStr("msg_add_gpx_polygon_too_many_segment"));
				return;
			}
			else if (t.getTrkseg().size() == 1)
				trk = t.getTrkseg().get(0);
		}
		if (trk == null)
		{
			JOptionPane.showMessageDialog(mg, OSMCDStrs.RStr("msg_add_gpx_polygon_no_select"), OSMBStrs.RStr("Error"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		JCatalogTree catalogTree = mg.getCatalogTree();
		final String mapNameFmt = "%s %02d";
		IfCatalog catalog = catalogTree.getCatalog();
		String name = mg.getCatalogName();
		final ACMapSource mapSource = mg.getSelectedMapSource();
		SelectedZoomLevels sZL = mg.getSelectedZoomLevels();
		int[] zoomLevels = sZL.getZoomLevels();
		if (zoomLevels.length == 0)
		{
			JOptionPane.showMessageDialog(mg, OSMCDStrs.RStr("msg_no_zoom_level_selected"));
			return;
		}
		List<? extends GpxPoint> points = trk.getTrkpt();
		final GeoCoordinate[] trackPoints = new GeoCoordinate[points.size()];
		GeoCoordinate minCoordinate = new GeoCoordinate(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		GeoCoordinate maxCoordinate = new GeoCoordinate(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for (int i = 0; i < trackPoints.length; i++)
		{
			GpxPoint gpxPoint = points.get(i);
			GeoCoordinate c = new GeoCoordinate(gpxPoint.getLat().doubleValue(), gpxPoint.getLon().doubleValue());
			minCoordinate.lat = Math.min(minCoordinate.lat, c.lat);
			minCoordinate.lon = Math.min(minCoordinate.lon, c.lon);
			maxCoordinate.lat = Math.max(maxCoordinate.lat, c.lat);
			maxCoordinate.lon = Math.max(maxCoordinate.lon, c.lon);
			trackPoints[i] = c;
		}

		final int maxZoom = zoomLevels[zoomLevels.length - 1]; // W #??? if zoomlevels == [3, 4, 5] -> maxZoom == 2
		Point p1 = new Point(maxCoordinate.toPixelCoordinate(maxZoom).getX(), maxCoordinate.toPixelCoordinate(maxZoom).getY());
		Point p2 = new Point(minCoordinate.toPixelCoordinate(maxZoom).getX(), minCoordinate.toPixelCoordinate(maxZoom).getY());

		final int centerY = p1.y + ((p1.y - p2.y) / 2);

		final UnitSystem unitSystem = ACSettings.getInstance().getUnitSystem();

		final TileImageParameters customTileParameters = mg.getSelectedTileImageParameters();

		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(300, 100));
		final JLabel label = new JLabel("");
		final JDistanceSlider slider = new JDistanceSlider(maxZoom, centerY, unitSystem, 5, 500);
		ChangeListener cl = new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				double d = MP2MapSpace.horizontalDistance(maxZoom, centerY, slider.getValue()); // W #mapSpace ??? TODO test method! horizontalDistance changed!!!
				d *= unitSystem.earthRadius * unitSystem.unitFactor;
				String unitName = unitSystem.unitSmall;
				if (d > unitSystem.unitFactor)
				{
					d /= unitSystem.unitFactor;
					unitName = unitSystem.unitLarge;
				}
				label.setText(String.format(OSMCDStrs.RStr("dlg_gpx_track_select_distance"), ((int) d), unitName));
			}
		};
		final JButton previewButton = new JButton(OSMCDStrs.RStr("dlg_gpx_track_select_preview"));
		previewButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int distance = slider.getValue();
				MapPolygon maxZoomMap = MapPolygon.createTrackEnclosure(null, "Dummy", mapSource, maxZoom, trackPoints, distance, customTileParameters);
				if (msl != null)
					msl.setObject(maxZoomMap);
				msl = new MapAreaHighlightingLayer(maxZoomMap);
				mg.previewMap.repaint();
			}
		});

		cl.stateChanged(null);
		slider.addChangeListener(cl);
		panel.add(label, BorderLayout.NORTH);
		panel.add(slider, BorderLayout.CENTER);
		panel.add(previewButton, BorderLayout.SOUTH);

		int result = JOptionPane.showConfirmDialog(mg, panel, OSMCDStrs.RStr("dlg_gpx_track_select_title"), JOptionPane.OK_CANCEL_OPTION);

		if (msl != null)
		{
			mg.previewMap.mapLayers.remove(msl);
			msl.setObject(null);
		}

		if (result != JOptionPane.OK_OPTION)
			return;

		int distance = slider.getValue();
		MapPolygon maxZoomMap = MapPolygon.createTrackEnclosure(null, "Dummy", mapSource, maxZoom, trackPoints, distance, customTileParameters);

		int width = maxZoomMap.getMaxPixelCoordinate().x - maxZoomMap.getMinPixelCoordinate().x;
		int height = maxZoomMap.getMaxPixelCoordinate().y - maxZoomMap.getMinPixelCoordinate().y;
		if (Math.max(width, height) > OSMCDSettings.getInstance().getMaxMapSize())
		{
			String msg = OSMCDStrs.RStr("msg_add_gpx_polygon_maxsize");
			result = JOptionPane.showConfirmDialog(mg, msg, OSMCDStrs.RStr("msg_add_gpx_polygon_maxsize_title"), JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE);
			if (result != JOptionPane.YES_OPTION)
				return;
		}

		Layer layer = null;
		for (int zoom : zoomLevels)
		{
			String layerName = name;
			int c = 1;
			boolean success = false;
			do
			{
				try
				{
					layer = new Layer(catalog, layerName, zoom);
					success = true;
				}
				catch (InvalidNameException e)
				{
					layerName = name + "_" + Integer.toString(c++);
				}
			} while (!success);
			String mapName = String.format(mapNameFmt, new Object[]
			{ layerName, zoom });
			IfMap map = MapPolygon.createFromMapPolygon(layer, mapName, zoom, maxZoomMap);
			layer.addMap(map);
		}
		catalog.addLayer(layer);
		mg.notifyLayerInsert(layer);
	}
}
