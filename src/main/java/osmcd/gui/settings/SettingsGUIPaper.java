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
package osmcd.gui.settings;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import osmb.program.WgsGrid.WgsDensity;
import osmb.utilities.GBCTable;
import osmb.utilities.UnitSystem;
import osmcd.OSMCDSettings;
import osmcd.OSMCDStrs;
import osmcd.program.PaperSize;
import osmcd.program.PaperSize.Format;
import osmcd.program.SettingsPaperAtlas;

public class SettingsGUIPaper extends JPanel
{
	private static final long serialVersionUID = -8265562215604604074L;

	private static void setModel(SpinnerNumberModel model, double min, double max, Double step)
	{
		model.setMaximum(max);
		model.setMinimum(min);
		model.setStepSize(step);
		double value = model.getNumber().doubleValue();
		value = Math.max(Math.min(value, max), min);
		model.setValue(value);
	}

	private static void setEditor(JSpinner jSpinner, String pattern)
	{
		jSpinner.setEditor(new JSpinner.NumberEditor(jSpinner, pattern));
	}

	private static JPanel createSection(TitledBorder border)
	{
		JPanel jPanel = new JPanel(new GridBagLayout());
		jPanel.setBorder(border);
		return jPanel;
	}

	private final JButton jButtonDefaults = new JButton();
	private final JButton jButtonExport = new JButton();
	private final JButton jButtonImport = new JButton();

	private final JCheckBox jCheckBoxCompass = new JCheckBox();
	private final JCheckBox jCheckBoxLandscape = new JCheckBox();
	private final JCheckBox jCheckBoxPageNumbers = new JCheckBox("", true);
	private final JCheckBox jCheckBoxScaleBar = new JCheckBox();
	private final JCheckBox jCheckBoxWgsGrid = new JCheckBox("", true);

	private final JFileChooser jFileChooser = new JFileChooser();

	private final TitledBorder titledBorderActions = SettingsGUI.createSectionBorder("");
	private final TitledBorder titledBorderAdditions = SettingsGUI.createSectionBorder("");
	private final TitledBorder titledBorderAdvanced = SettingsGUI.createSectionBorder("");
	private final TitledBorder titledBorderMargins = SettingsGUI.createSectionBorder("");
	private final TitledBorder titledBorderSize = SettingsGUI.createSectionBorder("");

	private final JPanel jPanelActions = createSection(titledBorderActions), jPanelAdditions = createSection(titledBorderAdditions),
			jPanelAdvanced = createSection(titledBorderAdvanced), jPanelMargins = createSection(titledBorderMargins), jPanelSize = createSection(titledBorderSize);

	private final JComboBox<Format> jComboBoxFormat = new JComboBox<Format>(Format.values());
	private final JComboBox<WgsDensity> jComboBoxWgsDensity = new JComboBox<WgsDensity>(WgsDensity.values());

	private final JRadioButton jRadioButtonCustom = new JRadioButton("", true);
	private final JRadioButton jRadioButtonDefault = new JRadioButton("", true);
	private final JRadioButton jRadioButtonSelection = new JRadioButton("", true);

	private final SpinnerNumberModel modelCompression = new SpinnerNumberModel(SettingsPaperAtlas.COMPRESSION_DEFAULT, SettingsPaperAtlas.COMPRESSION_MIN,
			SettingsPaperAtlas.COMPRESSION_MAX, 1);

	private final SpinnerNumberModel modelCrop = new SpinnerNumberModel(SettingsPaperAtlas.CROP_DEFAULT, SettingsPaperAtlas.CROP_MIN,
			SettingsPaperAtlas.CROP_MAX, 1), modelDpi = new SpinnerNumberModel(SettingsPaperAtlas.DPI_DEFAULT, SettingsPaperAtlas.DPI_MIN,
			SettingsPaperAtlas.DPI_MAX, 1);

	private final SpinnerNumberModel modelHeight = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);
	private final SpinnerNumberModel modelWidth = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);

	private final SpinnerNumberModel modelMarginBottom = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);
	private final SpinnerNumberModel modelMarginLeft = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);
	private final SpinnerNumberModel modelMarginRight = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);
	private final SpinnerNumberModel modelMarginTop = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);

	private final SpinnerNumberModel modelOverlap = new SpinnerNumberModel(0.0, 0.0, 0.0, 1.0);

	private final JSpinner jSpinnerDpi = new JSpinner(modelDpi), jSpinnerWidth = new JSpinner(modelWidth), jSpinnerHeight = new JSpinner(modelHeight),
			jSpinnerMarginTop = new JSpinner(modelMarginTop), jSpinnerMarginLeft = new JSpinner(modelMarginLeft), jSpinnerMarginBottom = new JSpinner(
					modelMarginBottom), jSpinnerMarginRight = new JSpinner(modelMarginRight), jSpinnerOverlap = new JSpinner(modelOverlap), jSpinnerCrop = new JSpinner(
					modelCrop), jSpinnerCompression = new JSpinner(modelCompression);

	private final JLabel jLabelCompression = new JLabel(), jLabelDpi = new JLabel(), jLabelWidth = new JLabel(), jLabelHeight = new JLabel(),
			jLabelMarginTop = new JLabel(), jLabelMarginLeft = new JLabel(), jLabelMarginBottom = new JLabel(), jLabelMarginRight = new JLabel(),
			jLabelOverlap = new JLabel(), jLabelCrop = new JLabel();

	private String importError, exportError, errorReason, errorTitle, xmlFileFilter;

	private UnitSystem unitSystem;

	public SettingsGUIPaper() {
		super(new GridBagLayout());
		jSpinnerCrop.setEditor(new JSpinner.NumberEditor(jSpinnerCrop, "#0'%'"));
		setUnitSystem(UnitSystem.Metric);
		i18n();
		jFileChooser.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName().endsWith(".xml");
			}

			@Override
			public String getDescription()
			{
				return xmlFileFilter;
			}
		});
		jButtonImport.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				importFromXml();
			}
		});
		jButtonExport.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				exportToXml();
			}
		});
		jButtonDefaults.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				resetToDefaults();
			}
		});
		jComboBoxFormat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Format format = (Format) jComboBoxFormat.getSelectedItem();
				double width = unitSystem.pointsToUnits(format.width);
				double height = unitSystem.pointsToUnits(format.height);
				modelWidth.setValue(width);
				modelHeight.setValue(height);
			}
		});
		jCheckBoxWgsGrid.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				boolean enabled = e.getStateChange() != ItemEvent.DESELECTED;
				jComboBoxWgsDensity.setEnabled(enabled);
			}
		});
		jRadioButtonCustom.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				boolean enabled = e.getStateChange() != ItemEvent.DESELECTED;
				jLabelWidth.setEnabled(enabled);
				jLabelHeight.setEnabled(enabled);
				jSpinnerWidth.setEnabled(enabled);
				jSpinnerHeight.setEnabled(enabled);
			}
		});
		jRadioButtonDefault.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				boolean enabled = e.getStateChange() != ItemEvent.DESELECTED;
				jComboBoxFormat.setEnabled(enabled);
				jCheckBoxLandscape.setEnabled(enabled);
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonSelection);
		buttonGroup.add(jRadioButtonDefault);
		buttonGroup.add(jRadioButtonCustom);

		GBCTable gbc = new GBCTable();

		jPanelSize.add(jRadioButtonDefault, gbc.begin());
		jPanelSize.add(jRadioButtonCustom, gbc.incY());
		jPanelSize.add(jRadioButtonSelection, gbc.incY());
		jPanelSize.add(jComboBoxFormat, gbc.incX());
		jPanelSize.add(jLabelWidth, gbc.incY());
		jPanelSize.add(jLabelHeight, gbc.incY());
		jPanelSize.add(jCheckBoxLandscape, gbc.incX());
		jPanelSize.add(jSpinnerWidth, gbc.incY());
		jPanelSize.add(jSpinnerHeight, gbc.incY());
		jPanelSize.add(Box.createGlue(), gbc.incX().gridheight(3).fill());

		jPanelMargins.add(jLabelMarginTop, gbc.begin());
		jPanelMargins.add(jLabelMarginBottom, gbc.incY());
		jPanelMargins.add(jSpinnerMarginTop, gbc.incX());
		jPanelMargins.add(jSpinnerMarginBottom, gbc.incY());
		jPanelMargins.add(jLabelMarginLeft, gbc.incX());
		jPanelMargins.add(jLabelMarginRight, gbc.incY());
		jPanelMargins.add(jSpinnerMarginLeft, gbc.incX());
		jPanelMargins.add(jSpinnerMarginRight, gbc.incY());
		jPanelMargins.add(Box.createHorizontalGlue(), gbc.incX().fillH());

		jPanelAdditions.add(jCheckBoxWgsGrid, gbc.begin());
		jPanelAdditions.add(jCheckBoxPageNumbers, gbc.incY().gridwidth(2));
		jPanelAdditions.add(jCheckBoxScaleBar, gbc.incY());
		jPanelAdditions.add(jComboBoxWgsDensity, gbc.incX());
		gbc.incY();
		jPanelAdditions.add(jCheckBoxCompass, gbc.incY());
		jPanelAdditions.add(Box.createHorizontalGlue(), gbc.incX().fillH());

		jPanelAdvanced.add(jLabelDpi, gbc.begin());
		jPanelAdvanced.add(jLabelCompression, gbc.incY());
		jPanelAdvanced.add(jSpinnerDpi, gbc.incX());
		jPanelAdvanced.add(jSpinnerCompression, gbc.incY());
		jPanelAdvanced.add(jLabelOverlap, gbc.incX());
		jPanelAdvanced.add(jLabelCrop, gbc.incY());
		jPanelAdvanced.add(jSpinnerOverlap, gbc.incX());
		jPanelAdvanced.add(jSpinnerCrop, gbc.incY());
		jPanelAdvanced.add(Box.createHorizontalGlue(), gbc.incX().fillH());

		jPanelActions.add(jButtonImport, gbc.begin());
		jPanelActions.add(jButtonExport, gbc.incX());
		jPanelActions.add(jButtonDefaults, gbc.incX());
		jPanelActions.add(Box.createHorizontalGlue(), gbc.incX().fillH());

		gbc = new GBCTable(0);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(jPanelSize, gbc.begin());
		add(jPanelMargins, gbc.incY());
		add(jPanelAdditions, gbc.incX().fillH());
		add(jPanelAdvanced, gbc.incY().fillH());
		add(jPanelActions, gbc.begin(1, 3).gridwidth(2).fillH());
		add(Box.createGlue(), gbc.incY().gridwidth(2).fill());
	}

	private void importFromXml()
	{
		int state = jFileChooser.showOpenDialog(SettingsGUIPaper.this);
		if (state == JFileChooser.APPROVE_OPTION)
		{
			File file = jFileChooser.getSelectedFile();
			JAXBContext context;
			try
			{
				context = JAXBContext.newInstance(SettingsPaperAtlas.class);
				Unmarshaller um = context.createUnmarshaller();
				SettingsPaperAtlas s = (SettingsPaperAtlas) um.unmarshal(file);
				loadSettings(s);
			}
			catch (JAXBException ex)
			{
				String text = importError + file.getName() + "\n" + errorReason + ex.getMessage();
				JOptionPane.showMessageDialog(SettingsGUIPaper.this, text, errorTitle, JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void exportToXml()
	{
		int state = jFileChooser.showSaveDialog(SettingsGUIPaper.this);
		if (state == JFileChooser.APPROVE_OPTION)
		{
			File file = jFileChooser.getSelectedFile();
			JAXBContext context;
			try
			{
				context = JAXBContext.newInstance(SettingsPaperAtlas.class);
				Marshaller m = context.createMarshaller();
				SettingsPaperAtlas s = new SettingsPaperAtlas();
				applySettings(s);
				m.marshal(s, file);
			}
			catch (JAXBException ex)
			{
				String text = exportError + file.getName() + "\n" + errorReason + ex.getMessage();
				JOptionPane.showMessageDialog(SettingsGUIPaper.this, text, errorTitle, JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void resetToDefaults()
	{
		loadSettings(new SettingsPaperAtlas());
	}

	private void i18n()
	{
		setName(OSMCDStrs.RStr("set_paper_title"));
		titledBorderActions.setTitle(OSMCDStrs.RStr("set_paper_actions"));
		titledBorderAdditions.setTitle(OSMCDStrs.RStr("set_paper_additions"));
		titledBorderAdvanced.setTitle(OSMCDStrs.RStr("set_paper_advanced"));
		titledBorderMargins.setTitle(OSMCDStrs.RStr("set_paper_margins"));
		titledBorderSize.setTitle(OSMCDStrs.RStr("set_paper_size"));
		jComboBoxFormat.setToolTipText(OSMCDStrs.RStr("set_paper_size_default_format_tips"));
		jComboBoxWgsDensity.setToolTipText(OSMCDStrs.RStr("set_paper_wgs_grid_density_tips"));
		jRadioButtonSelection.setText(OSMCDStrs.RStr("set_paper_size_selection"));
		jRadioButtonSelection.setToolTipText(OSMCDStrs.RStr("set_paper_size_selection_tips"));
		jRadioButtonDefault.setText(OSMCDStrs.RStr("set_paper_size_default"));
		jRadioButtonDefault.setToolTipText(OSMCDStrs.RStr("set_paper_size_default_tips"));
		jRadioButtonCustom.setText(OSMCDStrs.RStr("set_paper_size_custom"));
		jRadioButtonCustom.setToolTipText(OSMCDStrs.RStr("set_paper_size_custom_tips"));
		jCheckBoxScaleBar.setText(OSMCDStrs.RStr("set_paper_scale_bar"));
		jCheckBoxScaleBar.setToolTipText(OSMCDStrs.RStr("set_paper_scale_tips"));
		jCheckBoxCompass.setText(OSMCDStrs.RStr("set_paper_compass"));
		jCheckBoxCompass.setToolTipText(OSMCDStrs.RStr("set_paper_compass_tips"));
		jCheckBoxLandscape.setText(OSMCDStrs.RStr("set_paper_size_default_landscape"));
		jCheckBoxLandscape.setToolTipText(OSMCDStrs.RStr("set_paper_size_default_landscape_tips"));
		jCheckBoxWgsGrid.setText(OSMCDStrs.RStr(OSMCDStrs.RStr("set_paper_wgs_grid")));
		jCheckBoxWgsGrid.setToolTipText(OSMCDStrs.RStr("set_paper_wgs_grid_tips"));
		jCheckBoxPageNumbers.setText(OSMCDStrs.RStr("set_paper_paper_nubmer"));
		jCheckBoxPageNumbers.setToolTipText(OSMCDStrs.RStr("set_paper_paper_nubmer_tips"));
		jLabelCompression.setText(OSMCDStrs.RStr("set_paper_advanced_compression"));
		String compression = OSMCDStrs.RStr("set_paper_advanced_compression_tips");
		jLabelCompression.setToolTipText(compression);
		jSpinnerCompression.setToolTipText(compression);
		jLabelDpi.setText(OSMCDStrs.RStr("set_paper_advanced_dpi"));
		String dpi = OSMCDStrs.RStr("set_paper_advanced_dpi_tips");
		jLabelDpi.setToolTipText(dpi);
		jSpinnerDpi.setToolTipText(dpi);
		jLabelWidth.setText(OSMCDStrs.RStr("set_paper_size_custom_width"));
		String width = OSMCDStrs.RStr("set_paper_size_custom_width_tips");
		jLabelWidth.setToolTipText(width);
		jSpinnerWidth.setToolTipText(width);
		jLabelHeight.setText(OSMCDStrs.RStr("set_paper_size_custom_height"));
		String height = OSMCDStrs.RStr("set_paper_size_custom_height_tips");
		jLabelHeight.setToolTipText(height);
		jSpinnerHeight.setToolTipText(height);
		String margin = OSMCDStrs.RStr("set_paper_margins_tips");
		jLabelMarginTop.setText(OSMCDStrs.RStr("set_paper_margins_top"));
		jLabelMarginTop.setToolTipText(margin);
		jSpinnerMarginTop.setToolTipText(margin);
		jLabelMarginLeft.setText(OSMCDStrs.RStr("set_paper_margins_left"));
		jLabelMarginLeft.setToolTipText(margin);
		jSpinnerMarginLeft.setToolTipText(margin);
		jLabelMarginBottom.setText(OSMCDStrs.RStr("set_paper_margins_bottom"));
		jLabelMarginBottom.setToolTipText(margin);
		jSpinnerMarginBottom.setToolTipText(margin);
		jLabelMarginRight.setText(OSMCDStrs.RStr("set_paper_margins_right"));
		jLabelMarginRight.setToolTipText(margin);
		jSpinnerMarginRight.setToolTipText(margin);
		jLabelOverlap.setText(OSMCDStrs.RStr("set_paper_advanced_overlap"));
		String overlap = OSMCDStrs.RStr("set_paper_advanced_overlap_tips");
		jLabelOverlap.setToolTipText(overlap);
		jSpinnerOverlap.setToolTipText(overlap);
		jLabelCrop.setText(OSMCDStrs.RStr("set_paper_advanced_crop"));
		String crop = OSMCDStrs.RStr("set_paper_advanced_crop_tips");
		jLabelCrop.setToolTipText(crop);
		jSpinnerCrop.setToolTipText(crop);
		jButtonImport.setText(OSMCDStrs.RStr("set_paper_actions_import_xml"));
		jButtonImport.setToolTipText(OSMCDStrs.RStr("set_paper_actions_import_xml_tip"));
		jButtonExport.setText(OSMCDStrs.RStr("set_paper_actions_export_xml"));
		jButtonExport.setToolTipText(OSMCDStrs.RStr("set_paper_actions_export_xml_tip"));
		jButtonDefaults.setText(OSMCDStrs.RStr("set_paper_actions_restore_default"));
		jButtonDefaults.setToolTipText(OSMCDStrs.RStr("set_paper_actions_restore_default_tips"));
		importError = OSMCDStrs.RStr("set_paper_actions_error_import");
		exportError = OSMCDStrs.RStr("set_paper_actions_error_export");
		errorReason = OSMCDStrs.RStr("set_paper_actions_error_reason");
		errorTitle = OSMCDStrs.RStr("set_paper_actions_error_title");
		xmlFileFilter = OSMCDStrs.RStr("set_paper_actions_xml_filter");
	}

	private void setUnitSystem(UnitSystem unitSystem)
	{
		if (unitSystem.equals(this.unitSystem))
			return;
		this.unitSystem = unitSystem;
		Double step = 0.1d;
		double min, max;
		min = unitSystem.pointsToUnits(SettingsPaperAtlas.MARGIN_MIN);
		max = unitSystem.pointsToUnits(SettingsPaperAtlas.MARGIN_MAX);
		setModel(modelMarginBottom, min, max, step);
		setModel(modelMarginLeft, min, max, step);
		setModel(modelMarginRight, min, max, step);
		setModel(modelMarginTop, min, max, step);
		min = unitSystem.pointsToUnits(SettingsPaperAtlas.PAPER_SIZE_MIN);
		max = unitSystem.pointsToUnits(SettingsPaperAtlas.PAPER_SIZE_MAX);
		setModel(modelWidth, min, max, step);
		setModel(modelHeight, min, max, step);
		min = unitSystem.pointsToUnits(SettingsPaperAtlas.OVERLAP_MIN);
		max = unitSystem.pointsToUnits(SettingsPaperAtlas.OVERLAP_MAX);
		setModel(modelOverlap, min, max, step);
		String pattern = "#0.00 " + unitSystem.unitTiny;
		setEditor(jSpinnerWidth, pattern);
		setEditor(jSpinnerHeight, pattern);
		setEditor(jSpinnerMarginTop, pattern);
		setEditor(jSpinnerMarginLeft, pattern);
		setEditor(jSpinnerMarginBottom, pattern);
		setEditor(jSpinnerMarginRight, pattern);
		setEditor(jSpinnerOverlap, pattern);
	}

	private PaperSize getPaperSize()
	{
		if (jRadioButtonDefault.isSelected())
		{
			Format format = (Format) jComboBoxFormat.getSelectedItem();
			boolean landscape = jCheckBoxLandscape.isSelected();
			return new PaperSize(format, landscape);
		}
		if (jRadioButtonCustom.isSelected())
		{
			double width = modelWidth.getNumber().doubleValue();
			double height = modelHeight.getNumber().doubleValue();
			width = unitSystem.unitsToPoints(width);
			height = unitSystem.unitsToPoints(height);
			return new PaperSize(width, height);
		}
		return null;
	}

	private void setPaperSize(PaperSize paperSize)
	{
		if (paperSize == null)
		{
			jRadioButtonSelection.setSelected(true);
			return;
		}
		if (paperSize.format != null)
		{
			jRadioButtonDefault.setSelected(true);
			jComboBoxFormat.setSelectedIndex(paperSize.format.ordinal());
			jCheckBoxLandscape.setSelected(paperSize.landscape);
		}
		else
		{
			jRadioButtonCustom.setSelected(true);
		}
	}

	public void loadSettings(OSMCDSettings s)
	{
		setUnitSystem(s.getUnitSystem());
		loadSettings(s.getPaperAtlas());
	}

	public void loadSettings(SettingsPaperAtlas s)
	{
		setPaperSize(s.paperSize);
		modelMarginTop.setValue(unitSystem.pointsToUnits(s.marginTop));
		modelMarginLeft.setValue(unitSystem.pointsToUnits(s.marginLeft));
		modelMarginBottom.setValue(unitSystem.pointsToUnits(s.marginBottom));
		modelMarginRight.setValue(unitSystem.pointsToUnits(s.marginRight));
		jCheckBoxScaleBar.setSelected(s.scaleBar);
		jCheckBoxCompass.setSelected(s.compass);
		jComboBoxWgsDensity.setSelectedItem(s.wgsDensity);
		jCheckBoxWgsGrid.setSelected(s.wgsEnabled);
		jCheckBoxPageNumbers.setSelected(s.pageNumbers);
		modelCrop.setValue(s.crop);
		modelOverlap.setValue(unitSystem.pointsToUnits(s.overlap));
		modelCompression.setValue(s.compression);
		modelDpi.setValue(s.dpi);
	}

	public void applySettings(OSMCDSettings s)
	{
		applySettings(s.getPaperAtlas());
	}

	public void applySettings(SettingsPaperAtlas s)
	{
		s.paperSize = getPaperSize();
		s.marginTop = unitSystem.unitsToPoints(modelMarginTop.getNumber().doubleValue());
		s.marginLeft = unitSystem.unitsToPoints(modelMarginLeft.getNumber().doubleValue());
		s.marginBottom = unitSystem.unitsToPoints(modelMarginBottom.getNumber().doubleValue());
		s.marginRight = unitSystem.unitsToPoints(modelMarginRight.getNumber().doubleValue());
		s.scaleBar = jCheckBoxScaleBar.isSelected();
		s.compass = jCheckBoxCompass.isSelected();
		s.wgsDensity = (WgsDensity) jComboBoxWgsDensity.getSelectedItem();
		s.wgsEnabled = jCheckBoxWgsGrid.isSelected();
		s.pageNumbers = jCheckBoxPageNumbers.isSelected();
		s.crop = modelCrop.getNumber().intValue();
		s.overlap = unitSystem.unitsToPoints(modelOverlap.getNumber().doubleValue());
		s.compression = modelCompression.getNumber().intValue();
		s.dpi = modelDpi.getNumber().intValue();
	}
}
