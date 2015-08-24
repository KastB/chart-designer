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
package osmcd.gui.gpxtree;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

<<<<<<< HEAD:src/main/java/osmcd/gui/gpxtree/JGpxPanel.java
import osmb.utilities.GBC;
=======
import osmcb.utilities.GBC;
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318:src/main/java/osmcd/gui/panels/JGpxPanel.java
import osmcd.OSMCDStrs;
import osmcd.data.gpx.gpx11.RteType;
import osmcd.data.gpx.gpx11.TrkType;
import osmcd.data.gpx.gpx11.TrksegType;
import osmcd.data.gpx.gpx11.WptType;
import osmcd.gui.actions.GpxAddPoint;
import osmcd.gui.actions.GpxClear;
import osmcd.gui.actions.GpxLoad;
import osmcd.gui.actions.GpxNew;
import osmcd.gui.actions.GpxSave;
import osmcd.gui.components.JCollapsiblePanel;
import osmcd.gui.mapview.GpxLayer;
import osmcd.gui.mapview.PreviewMap;
<<<<<<< HEAD:src/main/java/osmcd/gui/gpxtree/JGpxPanel.java
=======
import osmcd.gui.mapview.layer.GpxLayer;
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318:src/main/java/osmcd/gui/panels/JGpxPanel.java

/**
 * Allows to load, display, edit and save gpx files using a tree view. TODO warn unsaved changes on exit
 * 
 */
public class JGpxPanel extends JCollapsiblePanel
{
	private static final long serialVersionUID = 1L;

	private JTree tree;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel model;
	private ArrayList<String> openedFiles;

	private PreviewMap previewMap;

	public JGpxPanel(PreviewMap previewMap)
	{
		super("Gpx", new GridBagLayout());

		this.previewMap = previewMap;

<<<<<<< HEAD:src/main/java/osmcd/gui/gpxtree/JGpxPanel.java
		GBC eol = GBC.eol().fill(GBC.HORIZONTAL);
		GBC std = GBC.std().fill(GBC.HORIZONTAL);

		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(100, 300));
		addContent(treeView, GBC.eol().fill());

=======
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318:src/main/java/osmcd/gui/panels/JGpxPanel.java
		JButton newGpx = new JButton(OSMCDStrs.RStr("rp_gpx_new_gpx"));
		newGpx.addActionListener(new GpxNew(this));
		addContent(newGpx, std);

		JButton loadGpx = new JButton(OSMCDStrs.RStr("rp_gpx_load_gpx"));
		loadGpx.addActionListener(new GpxLoad(this));
		addContent(loadGpx, std);

		JButton saveGpx = new JButton(OSMCDStrs.RStr("rp_gpx_save_gpx"));
		saveGpx.addActionListener(new GpxSave(this));
		addContent(saveGpx, eol);

		JButton clearGpx = new JButton(OSMCDStrs.RStr("rp_gpx_clear_gpx"));
		clearGpx.addActionListener(new GpxClear(this));
		addContent(clearGpx, std);

		JButton addPointGpx = new JButton(OSMCDStrs.RStr("rp_gpx_add_wpt"));
		addPointGpx.addActionListener(new GpxAddPoint(this));
		addContent(addPointGpx, eol);

		rootNode = new DefaultMutableTreeNode(OSMCDStrs.RStr("rp_gpx_default_node_name"));
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.addMouseListener(new GpxTreeListener());

		model = (DefaultTreeModel) tree.getModel();

		openedFiles = new ArrayList<String>();
	}

	/**
	 * adds a layer for a new gpx file on the iMap and adds its structure to the treeview
	 * 
	 */
	public GpxRootEntry addGpxLayer(GpxLayer layer)
	{
		layer.setPanel(this);
		GpxRootEntry gpxEntry = new GpxRootEntry(layer);
		DefaultMutableTreeNode gpxNode = new DefaultMutableTreeNode(gpxEntry);
		model.insertNodeInto(gpxNode, rootNode, rootNode.getChildCount());
		TreePath path = new TreePath(gpxNode.getPath());
		tree.scrollPathToVisible(new TreePath(path));
		tree.setSelectionPath(path);

		addRoutes(layer, gpxNode);
		addTracks(layer, gpxNode);
		addWaypoints(layer, gpxNode);

		if (layer.getFile() != null)
			openedFiles.add(layer.getFile().getAbsolutePath());

		previewMap.mapLayers.add(layer);
		return gpxEntry;
	}

	/**
	 * @param layer
	 * @param gpxNode
	 * @param model
	 */
	private void addWaypoints(GpxLayer layer, DefaultMutableTreeNode gpxNode)
	{
		List<WptType> wpts = layer.getGpx().getWpt();
		for (WptType wpt : wpts)
		{
			WptEntry wptEntry = new WptEntry(wpt, layer);
			DefaultMutableTreeNode wptNode = new DefaultMutableTreeNode(wptEntry);
			model.insertNodeInto(wptNode, gpxNode, gpxNode.getChildCount());
		}
	}

	/**
	 * @param layer
	 * @param gpxNode
	 * @param model
	 */
	private void addTracks(GpxLayer layer, DefaultMutableTreeNode gpxNode)
	{
		// tracks
		List<TrkType> trks = layer.getGpx().getTrk();
		for (TrkType trk : trks)
		{
			TrkEntry trkEntry = new TrkEntry(trk, layer);
			DefaultMutableTreeNode trkNode = new DefaultMutableTreeNode(trkEntry);
			model.insertNodeInto(trkNode, gpxNode, gpxNode.getChildCount());
			// trkseg
			List<TrksegType> trksegs = trk.getTrkseg();
			int counter = 1;
			for (TrksegType trkseg : trksegs)
			{
				TrksegEntry trksegEntry = new TrksegEntry(trkseg, counter, layer);
				DefaultMutableTreeNode trksegNode = new DefaultMutableTreeNode(trksegEntry);
				model.insertNodeInto(trksegNode, trkNode, trkNode.getChildCount());
				counter++;

				// add trkpts
				List<WptType> trkpts = trkseg.getTrkpt();
				for (WptType trkpt : trkpts)
				{
					WptEntry trkptEntry = new WptEntry(trkpt, layer);
					DefaultMutableTreeNode trkptNode = new DefaultMutableTreeNode(trkptEntry);
					model.insertNodeInto(trkptNode, trksegNode, trksegNode.getChildCount());
				}
			}
		}
	}

	/**
	 * adds routes and route points to the tree view
	 * 
	 * @param layer
	 * @param gpxNode
	 */
	private void addRoutes(GpxLayer layer, DefaultMutableTreeNode gpxNode)
	{
		List<RteType> rtes = layer.getGpx().getRte();
		for (RteType rte : rtes)
		{
			RteEntry rteEntry = new RteEntry(rte, layer);
			DefaultMutableTreeNode rteNode = new DefaultMutableTreeNode(rteEntry);
			model.insertNodeInto(rteNode, gpxNode, gpxNode.getChildCount());
			// add rtepts
			List<WptType> rtepts = rte.getRtept();
			for (WptType rtept : rtepts)
			{
				WptEntry rteptEntry = new WptEntry(rtept, layer);
				DefaultMutableTreeNode rteptNode = new DefaultMutableTreeNode(rteptEntry);
				model.insertNodeInto(rteptNode, rteNode, rteNode.getChildCount());
			}
		}
	}

	/**
	 * Updates the tree view to show the newly added waypoint.
	 * 
	 * @param wpt
	 *          - new waypoint
	 * @param gpxEntry
	 *          - parent entry in the tree
	 */
	public void addWaypoint(WptType wpt, GpxEntry gpxEntry)
	{
		WptEntry wptEntry = new WptEntry(wpt, gpxEntry.getLayer());
		DefaultMutableTreeNode wptNode = new DefaultMutableTreeNode(wptEntry);
		model.insertNodeInto(wptNode, gpxEntry.getNode(), gpxEntry.getNode().getChildCount());
	}

	/**
	 * Updates the tree view after removing a waypoint.
	 * 
	 * @param wpt
	 *          - deleted waypoint
	 * @param gpxEntry
	 *          - parent entry of the deleted element in the tree
	 */
	public void removeWaypoint(WptEntry wptEntry)
	{
		DefaultMutableTreeNode wptNode = wptEntry.getNode();
		model.removeNodeFromParent(wptNode);

		// update layer (is changing the gpx enough? prolly not
		// did remove it already...check wheter its enough
	}

	public GpxEntry getSelectedEntry()
	{
		TreePath selection = tree.getSelectionPath();
		if (selection == null)
		{
			return null;
		}
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selection.getLastPathComponent();

		GpxEntry gpxEntry = null;
		try
		{
			gpxEntry = (GpxEntry) selectedNode.getUserObject();
			gpxEntry.setNode(selectedNode);
		}
		catch (ClassCastException e)
		{
		}
		return gpxEntry;
	}

	public boolean isFileOpen(String path)
	{
		return openedFiles.contains(path);
	}

	/**
	 * Resets the tree view. Used by GpxClear.
	 * 
	 */
<<<<<<< HEAD:src/main/java/osmcd/gui/gpxtree/JGpxPanel.java
	public void resetModel()
	{
=======
	public void resetModel() {
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318:src/main/java/osmcd/gui/panels/JGpxPanel.java
		rootNode = new DefaultMutableTreeNode(OSMCDStrs.RStr("rp_gpx_default_node_name"));
		model.setRoot(rootNode);
		openedFiles = new ArrayList<String>();
	}

	public DefaultTreeModel getTreeModel()
	{
		return model;
	}

}
