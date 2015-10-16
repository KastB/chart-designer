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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import osmb.mapsources.ACMapSourcesManager;
import osmb.mapsources.IfMapSource;
import osmb.mapsources.MapSourcesListModel;
import osmb.utilities.GBC;
import osmb.utilities.GUIExceptionHandler;
import osmb.utilities.OSMBRsc;
import osmb.utilities.OSMBStrs;
import osmb.utilities.OSMBUtilities;
import osmb.utilities.UnitSystem;
import osmcd.OSMCDSettings;
import osmcd.OSMCDStrs;
import osmcd.gui.MainFrame;
import osmcd.gui.actions.OpenInWebbrowser;
import osmcd.gui.components.JDirectoryChooser;
import osmcd.gui.components.JMapSizeCombo;
import osmcd.gui.components.JTimeSlider;

public class SettingsGUI extends JDialog
{
	private static final long serialVersionUID = -5227934684609357198L;

	public static Logger log = Logger.getLogger(SettingsGUI.class);

	private static final Integer[] THREADCOUNT_LIST = {1, 2, 4, 6};

	private static final long MBIT1 = 1000000 / 8;

	private enum Bandwidth
	{
		UNLIMITED(OSMCDStrs.RStr("set_net_bandwidth_unlimited"), 0), //
		MBit1("1 MBit", MBIT1), //
		MBit5("5 MBit", MBIT1 * 5), //
		MBit10("10 MBit", MBIT1 * 10), //
		MBit15("15 MBit", MBIT1 * 15), //
		MBit20("20 MBit", MBIT1 * 20);

		@SuppressWarnings("unused") // /W #unused
		public final long limit;
		public final String description;

		private Bandwidth(String description, long limit)
		{
			this.description = description;
			this.limit = limit;
		}

		@Override
		public String toString()
		{
			return description;
		}
	};

	private enum SupportLocale // /W default #???
	{
		SupportLocaleEn(new Locale("en"), "English"); // default

		private final Locale locale;
		private final String displayName;

		private SupportLocale(Locale locale, String displayName)
		{
			this.locale = locale;
			this.displayName = displayName;
		}

		public static SupportLocale localeOf(String lang, String contry)
		{
			for (SupportLocale l: SupportLocale.values())
			{
				if (l.locale.getLanguage().equals(lang) && l.locale.getCountry().equals(contry))
				{
					return l;
				}
			}
			return SupportLocaleEn;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	};

	private final OSMCDSettings settings = OSMCDSettings.getInstance();

	private JComboBox<UnitSystem> unitSystem; // /W <UnitSystem>
	private JComboBox<SupportLocale> languageCombo; // /W <SupportLocale>
	private JButton mapSourcesOnlineUpdate;
	private JTextField osmHikingTicket;
	private SettingsGUITileStore tileStoreTab;
	private JTimeSlider defaultExpirationTime;
	private JTimeSlider minExpirationTime;
	private JTimeSlider maxExpirationTime;
	private JMapSizeCombo mapSize;
	private JSpinner mapOverlapTiles;
	private JTextField atlasOutputDirectory;
	private JTextField jtfTileStoreDirectory;
	private JTextField jtfCatalogsDirectory;
	private JCheckBox jCheckBoxMakeNewCatalog; // /W #boolNew
	private JComboBox<Integer> threadCount; // /W <Integer>
	private JComboBox<Bandwidth> bandwidth; // /W <Bandwidth>
	// /W #unused
	//	private JComboBox proxyType; // /W ?proxyType wird nicht initialisiert?
	//	private JTextField proxyHost;
	//	private JTextField proxyPort;
	//	private JTextField proxyUserName;
	//	private JTextField proxyPassword;
	//	private JCheckBox ignoreDlErrors; // /W ?ignoreDlErrors wird nicht initialisiert?
	private JButton okButton;
	private JButton cancelButton;
	private JTabbedPane tabbedPane;
	private JList<IfMapSource> enabledMapSources;
	private MapSourcesListModel enabledMapSourcesModel; // /W eingeschaltet
	private JList<IfMapSource> disabledMapSources;
	private MapSourcesListModel disabledMapSourcesModel; // /W eingeschaltet
	private final SettingsGUIPaper paperAtlas;
	private final SettingsGUIWgsGrid display;

	public static void showSettingsDialog(final JFrame owner, final int nSelectedIndex)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new SettingsGUI(owner, nSelectedIndex);
			}
		});
	}

	private SettingsGUI(JFrame owner, int nSelectedIndex) {
		super(owner);
		setIconImages(MainFrame.OSMCD_ICONS);
		GUIExceptionHandler.registerForCurrentThread();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setMinimumSize(new Dimension(300, 300));

		paperAtlas = new SettingsGUIPaper();
		display = new SettingsGUIWgsGrid();

		createJFrame();
		createTabbedPane(nSelectedIndex);
		createJButtons();
		loadSettings();
		addListeners();
		pack();
		// don't allow shrinking, but allow enlarging
		setMinimumSize(getSize());
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dScreen.width - getWidth()) / 2, (dScreen.height - getHeight()) / 2);
		setVisible(true);
	}

	private void createJFrame()
	{
		setLayout(new BorderLayout());
		setTitle(OSMCDStrs.RStr("set_title"));
	}

	// Create tabbed pane
	public void createTabbedPane(int nSelectedIndex)
	{
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 492, 275);
		addDirectoriesPanel(); // /W #firstStart: position 0 nessesary! // /W #tabSelection SettingsDialog
		addDisplaySettingsPanel();
		// /W #---
		//try
		//{
		//	addMapSourceSettingsPanel();
		//}
		//catch (URISyntaxException e)
		//{
		//	log.error("", e);
		//}
		addMapSourceManagerPanel();
		addTileUpdatePanel();
		tileStoreTab = new SettingsGUITileStore(this);
		addMapSizePanel();
		// /W #firstStart: move to position 0! addDirectoriesPanel();
		addNetworkPanel();
		// /W #--- tabbedPane.addTab(paperAtlas.getName(), paperAtlas);

		add(tabbedPane, BorderLayout.CENTER);
		
		// /W #tabSelection SettingsDialog
		if ((tabbedPane.getTabCount() > nSelectedIndex) && (nSelectedIndex > -1))
			tabbedPane.setSelectedIndex(nSelectedIndex);
		else
			tabbedPane.setSelectedIndex(-1);
	}

	private JPanel createNewTab(String tabTitle)
	{
		JPanel tabPanel = new JPanel();
		addTab(tabTitle, tabPanel);
		return tabPanel;
	}

	protected void addTab(String tabTitle, JPanel tabPanel)
	{
		tabPanel.setName(tabTitle);
		tabPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.add(tabPanel, tabTitle);
	}

	private void addDisplaySettingsPanel()
	{
		JPanel tab = createNewTab(OSMCDStrs.RStr("set_display_title"));
		tab.setLayout(new GridBagLayout());

		JPanel unitSystemPanel = new JPanel(new GridBagLayout());
		unitSystemPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_display_unit_system_title")));

		// Language Panel
		JPanel languagePanel = new JPanel(new GridBagLayout());
		languagePanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_display_language")));
		languageCombo = new JComboBox<SupportLocale>(SupportLocale.values());
		languageCombo.setToolTipText(OSMCDStrs.RStr("set_display_language_choose_tips"));
		languageCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{

				Locale locale = ((SupportLocale) languageCombo.getSelectedItem()).locale;
				String currentLocaleStr = "" + settings.getLocaleLanguage() + settings.getLocaleCountry();
				String LocaleStr = "" + locale.getLanguage() + locale.getCountry();
				if (!currentLocaleStr.equals(LocaleStr) && isVisible())
				{
					settings.setLocaleLanguage(locale.getLanguage());
					settings.setLocaleCountry(locale.getCountry());

					int result = JOptionPane.showConfirmDialog(null, OSMCDStrs.RStr("set_display_language_restart_desc"),
							OSMCDStrs.RStr("set_display_language_msg_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					OSMBRsc.updateLocalizedStrings();
					if (result == JOptionPane.YES_OPTION)
					{
						applySettings();

						try
						{
							final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
							final File currentJar = new File(SettingsGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());

							/* is it a jar file? */
							if (currentJar.getName().endsWith(".jar"))
							{
								/* Build command: java -jar application.jar */

								Runtime r = Runtime.getRuntime();
								long maxMem = r.maxMemory();
								final ArrayList<String> command = new ArrayList<String>();
								command.add(javaBin);
								command.add("-jar");
								command.add("-Xms64m");
								if ((Long.MAX_VALUE == maxMem))
								{
									command.add("-Xmx1024M");
								}
								else
								{
									command.add("-Xmx" + (maxMem / 1048576) + "M");
								}
								command.add(currentJar.getPath());

								log.debug("restarting OSMCD using the following command: \n\t" + Arrays.toString(command.toArray()));
								final ProcessBuilder builder = new ProcessBuilder(command);
								builder.start();
							}
						}
						catch (Exception ex)
						{

						}
						System.exit(0);
					}
				}
			}
		});

		languagePanel.add(new JLabel(OSMCDStrs.RStr("set_display_language_choose")), GBC.std());
		languagePanel.add(languageCombo, GBC.std());
		languagePanel.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));

		UnitSystem[] us = UnitSystem.values();
		unitSystem = new JComboBox<UnitSystem>(us);
		unitSystemPanel.add(new JLabel(OSMCDStrs.RStr("set_display_unit_system_scale_bar")), GBC.std());
		unitSystemPanel.add(unitSystem, GBC.std());
		unitSystemPanel.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(unitSystemPanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(display, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(languagePanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
	}

	@SuppressWarnings("unused") // /W #unused
	private void addMapSourceSettingsPanel() throws URISyntaxException
	{

		JPanel tab = createNewTab(OSMCDStrs.RStr("set_mapsrc_config_title"));
		tab.setLayout(new GridBagLayout());

		JPanel updatePanel = new JPanel(new GridBagLayout());
		updatePanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_mapsrc_config_online_update")));

		mapSourcesOnlineUpdate = new JButton(OSMCDStrs.RStr("set_mapsrc_config_online_update_btn"));
		mapSourcesOnlineUpdate.addActionListener(new MapPacksOnlineUpdateAction());
		updatePanel.add(mapSourcesOnlineUpdate, GBC.std());

		JPanel osmHikingPanel = new JPanel(new GridBagLayout());
		osmHikingPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_mapsrc_config_osmhiking")));

		osmHikingTicket = new JTextField(20);

		osmHikingPanel.add(new JLabel(OSMCDStrs.RStr("set_mapsrc_config_osmhiking_purchased")), GBC.std());
		osmHikingPanel.add(osmHikingTicket, GBC.std().insets(2, 0, 10, 0));
		JLabel osmHikingTicketUrl = new JLabel(OSMCDStrs.RStr("set_mapsrc_config_osmhiking_howto"));
		osmHikingTicketUrl.addMouseListener(new OpenInWebbrowser(OSMCDStrs.RStr("set_mapsrc_config_osmhiking_howto_url")));
		osmHikingPanel.add(osmHikingTicketUrl, GBC.eol());

		tab.add(updatePanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(osmHikingPanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	private void addMapSourceManagerPanel()
	{
		JPanel tab = createNewTab(OSMCDStrs.RStr("set_mapsrc_mgr_title"));
		tab.setLayout(new GridBagLayout());

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_mapsrc_mgr_title_enabled")));

		JPanel centerPanel = new JPanel(new GridBagLayout());
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_mapsrc_mgr_title_disabled")));

		JButton up = new JButton(OSMBUtilities.loadResourceImageIcon("arrow_blue_up.png"));
		up.setToolTipText(OSMCDStrs.RStr("set_mapsrc_mgr_move_up_tips"));
		JButton down = new JButton(OSMBUtilities.loadResourceImageIcon("arrow_blue_down.png"));
		down.setToolTipText(OSMCDStrs.RStr("set_mapsrc_mgr_move_down_tips"));
		JButton toLeft = new JButton(OSMBUtilities.loadResourceImageIcon("arrow_blue_left.png"));
		toLeft.setToolTipText(OSMCDStrs.RStr("set_mapsrc_mgr_move_left_tips"));
		JButton toRight = new JButton(OSMBUtilities.loadResourceImageIcon("arrow_blue_right.png"));
		toRight.setToolTipText(OSMCDStrs.RStr("set_mapsrc_mgr_move_right_tips"));
		Insets buttonInsets = new Insets(4, 4, 4, 4);
		Dimension buttonDimension = new Dimension(40, 40);
		up.setPreferredSize(buttonDimension);
		down.setPreferredSize(buttonDimension);
		toLeft.setPreferredSize(buttonDimension);
		toRight.setPreferredSize(buttonDimension);
		up.setMargin(buttonInsets);
		down.setMargin(buttonInsets);
		toLeft.setMargin(buttonInsets);
		toRight.setMargin(buttonInsets);

		toLeft.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] idx = disabledMapSources.getSelectedIndices();
				for (int i = 0; i < idx.length; i++)
				{
					IfMapSource ms = disabledMapSourcesModel.removeElement(idx[i] - i);
					enabledMapSourcesModel.addElement(ms);
				}
			}
		});
		toRight.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] idx = enabledMapSources.getSelectedIndices();
				for (int i = 0; i < idx.length; i++)
				{
					IfMapSource ms = enabledMapSourcesModel.removeElement(idx[i] - i);
					disabledMapSourcesModel.addElement(ms);
				}
				disabledMapSourcesModel.sort();
			}
		});
		up.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] idx = enabledMapSources.getSelectedIndices();
				if (idx.length == 0)
					return;
				for (int i = 0; i < idx.length; i++)
				{
					int index = idx[i];
					if (index == 0)
						return;
					if (enabledMapSourcesModel.moveUp(index))
					idx[i]--;
				}
				enabledMapSources.setSelectedIndices(idx);
				enabledMapSources.ensureIndexIsVisible(idx[0]);
			}
		});
		down.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] idx = enabledMapSources.getSelectedIndices();
				if (idx.length == 0)
					return;
				for (int i = idx.length - 1; i >= 0; i--)
				{
					int index = idx[i];
					if (index == enabledMapSourcesModel.getSize() - 1)
					return;
					if (enabledMapSourcesModel.moveDown(index))
					idx[i]++;
				}
				enabledMapSources.setSelectedIndices(idx);
				enabledMapSources.ensureIndexIsVisible(idx[idx.length - 1]);
			}
		});
		GBC buttonGbc = GBC.eol();
		centerPanel.add(Box.createVerticalStrut(25), GBC.eol());
		centerPanel.add(toLeft, buttonGbc);
		centerPanel.add(toRight, buttonGbc);
		centerPanel.add(up, buttonGbc);
		centerPanel.add(down, buttonGbc);
		centerPanel.add(Box.createVerticalGlue(), GBC.std().fill());

		ACMapSourcesManager msManager = ACMapSourcesManager.getInstance();

		enabledMapSourcesModel = new MapSourcesListModel(msManager.getEnabledOrderedMapSources());
		enabledMapSources = new JList<IfMapSource>(enabledMapSourcesModel);
		JScrollPane leftScrollPane = new JScrollPane(enabledMapSources, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		leftPanel.add(leftScrollPane, BorderLayout.CENTER);

		disabledMapSourcesModel = new MapSourcesListModel(msManager.getDisabledMapSources());
		disabledMapSourcesModel.sort();
		disabledMapSources = new JList<IfMapSource>(disabledMapSourcesModel);
		JScrollPane rightScrollPane = new JScrollPane(disabledMapSources, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rightPanel.add(rightScrollPane, BorderLayout.CENTER);

		JPanel mapSourcesInnerPanel = new JPanel();

		Color c = UIManager.getColor("List.background");
		mapSourcesInnerPanel.setBackground(c);

		GBC lr = GBC.std().fill();
		lr.weightx = 1.0;

		tab.add(leftPanel, lr);
		tab.add(centerPanel, GBC.std().fill(GBC.VERTICAL));
		tab.add(rightPanel, lr);
	}

	private void addTileUpdatePanel()
	{
		JPanel backGround = createNewTab(OSMCDStrs.RStr("set_tile_update_title"));
		backGround.setLayout(new GridBagLayout());

		ChangeListener sliderChangeListener = new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				JTimeSlider slider = ((JTimeSlider) e.getSource());
				long x = slider.getTimeSecondsValue();
				JPanel panel = (JPanel) slider.getParent();
				TitledBorder tb = (TitledBorder) panel.getBorder();
				tb.setTitle(panel.getName() + ": " + OSMBUtilities.formatDurationSeconds(x));
				panel.repaint();
			}
		};
		GBC gbc_ef = GBC.eol().fill(GBC.HORIZONTAL);

		JPanel defaultExpirationPanel = new JPanel(new GridBagLayout());
		defaultExpirationPanel.setName(OSMCDStrs.RStr("set_tile_update_default_expiration"));
		defaultExpirationPanel.setBorder(createSectionBorder(""));
		defaultExpirationTime = new JTimeSlider();
		defaultExpirationTime.addChangeListener(sliderChangeListener);
		JLabel descr = new JLabel(OSMCDStrs.RStr("set_tile_update_default_expiration_desc"), JLabel.CENTER);

		defaultExpirationPanel.add(descr, gbc_ef);
		defaultExpirationPanel.add(defaultExpirationTime, gbc_ef);

		JPanel maxExpirationPanel = new JPanel(new BorderLayout());
		maxExpirationPanel.setName(OSMCDStrs.RStr("set_tile_update_max_expiration"));
		maxExpirationPanel.setBorder(createSectionBorder(""));
		maxExpirationTime = new JTimeSlider();
		maxExpirationTime.addChangeListener(sliderChangeListener);
		maxExpirationPanel.add(maxExpirationTime, BorderLayout.CENTER);

		JPanel minExpirationPanel = new JPanel(new BorderLayout());
		minExpirationPanel.setName(OSMCDStrs.RStr("set_tile_update_min_expiration"));
		minExpirationPanel.setBorder(createSectionBorder(""));
		minExpirationTime = new JTimeSlider();
		minExpirationTime.addChangeListener(sliderChangeListener);
		minExpirationPanel.add(minExpirationTime, BorderLayout.CENTER);

		descr = new JLabel(OSMCDStrs.RStr("set_tile_update_desc"), JLabel.CENTER);

		backGround.add(descr, gbc_ef);
		backGround.add(defaultExpirationPanel, gbc_ef);
		backGround.add(minExpirationPanel, gbc_ef);
		backGround.add(maxExpirationPanel, gbc_ef);
		backGround.add(Box.createVerticalGlue(), GBC.std().fill());
	}

	private void addMapSizePanel()
	{
		JPanel backGround = createNewTab(OSMCDStrs.RStr("set_map_size_title"));
		backGround.setLayout(new GridBagLayout());
		mapSize = new JMapSizeCombo();
		mapSize.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				log.trace("Map size: " + mapSize.getValue());
			}
		});

		JLabel mapSizeLabel = new JLabel(OSMCDStrs.RStr("set_map_size_max_size_of_rect"));
		JLabel mapSizeText = new JLabel(OSMCDStrs.RStr("set_map_size_desc"));

		mapOverlapTiles = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));

		JLabel mapOverlapTilesLabel = new JLabel(OSMCDStrs.RStr("set_map_size_overlap_tiles"));

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_map_size_settings")));

		GBC gbc = GBC.eol().insets(0, 5, 0, 5);
		leftPanel.add(mapSizeLabel, GBC.std());
		leftPanel.add(mapSize, GBC.eol());
		leftPanel.add(mapOverlapTilesLabel, GBC.std());
		leftPanel.add(mapOverlapTiles, GBC.eol());
		leftPanel.add(mapSizeText, gbc.fill(GBC.HORIZONTAL));
		leftPanel.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));

		backGround.add(leftPanel, GBC.std().fill(GBC.HORIZONTAL).anchor(GBC.NORTHEAST));
		backGround.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
	}

	private void addDirectoriesPanel()
	{
		JPanel backGround = createNewTab(OSMCDStrs.RStr("set_directory_title"));
		backGround.setLayout(new GridBagLayout());
		
		// /W info
		JLabel jlInfo1 = new JLabel("Beim ersten Start sollen hier die Pfade gesetzt werden:");
		backGround.add(jlInfo1, GBC.eol());
		JLabel jlInfo2 = new JLabel("   - Schreib- und Lesezugriff des Users für alle Pfade nötig");
		backGround.add(jlInfo2, GBC.eol());
		JLabel jlInfo3 = new JLabel("   - tilstore und bundle output können groß werden und brauchen schnellen Zugriff!");
		backGround.add(jlInfo3, GBC.eol());
		JLabel jlInfo4 = new JLabel("   ");
		backGround.add(jlInfo4, GBC.eol());
		
		// /W bundleOutputDir
		// /W atlas <-> bundle
		JPanel atlasOutputDirPanel = new JPanel(new GridBagLayout());
		atlasOutputDirPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_directory_output_bundle")));

		atlasOutputDirectory = new JTextField();
		atlasOutputDirectory.setToolTipText(String.format(OSMCDStrs.RStr("set_directory_output_tips"), settings.getChartBundleOutputDirectory()));
		atlasOutputDirectory.setText(settings.getChartBundleOutputDirectory().toString());
		atlasOutputDirectory.setEnabled(false);
		atlasOutputDirectory.setDisabledTextColor(Color.BLACK);
		
		JButton selectAtlasOutputDirectory = new JButton(OSMCDStrs.RStr("set_directory_output_select"));
		selectAtlasOutputDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDirectoryChooser dc = new JDirectoryChooser();
				dc.setCurrentDirectory(settings.getChartBundleOutputDirectory());
				if (dc.showDialog(SettingsGUI.this, OSMCDStrs.RStr("set_directory_output_select_dlg_title")) != JFileChooser.APPROVE_OPTION)
					return;
				atlasOutputDirectory.setText(dc.getSelectedFile().getAbsolutePath());
				settings.setChartBundleOutputDirectory(dc.getSelectedFile());
			}
		});
		
		atlasOutputDirPanel.add(atlasOutputDirectory, GBC.std().fillH());
		atlasOutputDirPanel.add(selectAtlasOutputDirectory, GBC.std());

		 // /W tilestoreDir
		JPanel tileStoreDirPanel = new JPanel(new GridBagLayout());
		tileStoreDirPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_directory_output_tilestore")));
		
		jtfTileStoreDirectory = new JTextField();
		jtfTileStoreDirectory.setToolTipText(String.format(OSMCDStrs.RStr("set_directory_output_tips"), settings.getTileStoreDirectory()));
		jtfTileStoreDirectory.setText(settings.getTileStoreDirectory().toString());
		jtfTileStoreDirectory.setEnabled(false);
		jtfTileStoreDirectory.setDisabledTextColor(Color.BLACK);
		// /W #??? ToolTipTextColor of jtfTileStoreDirectory.diabled, BorderColor?
		
		JButton selectTileStoreDirectory = new JButton(OSMCDStrs.RStr("set_directory_output_select"));
		selectTileStoreDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDirectoryChooser dc = new JDirectoryChooser();
				dc.setCurrentDirectory(settings.getTileStoreDirectory());
				if (dc.showDialog(SettingsGUI.this, OSMCDStrs.RStr("set_directory_output_select_dlg_title")) != JFileChooser.APPROVE_OPTION)
					return;
				jtfTileStoreDirectory.setText(dc.getSelectedFile().getAbsolutePath());
				settings.setTileStoreDirectory(dc.getSelectedFile());
			}
		});
		
		JLabel infoTileStore = new JLabel("To enable/disable tile store see tab 'Tile store'");
		tileStoreDirPanel.add(jtfTileStoreDirectory, GBC.std().fillH());
		tileStoreDirPanel.add(selectTileStoreDirectory, GBC.eol());
		tileStoreDirPanel.add(infoTileStore, GBC.eol());
		
		
		// /W catalogsDir
		JPanel catalogsDirPanel = new JPanel(new GridBagLayout());
		catalogsDirPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_directory_output_catalogs")));
		
		jtfCatalogsDirectory = new JTextField();
		jtfCatalogsDirectory.setToolTipText(String.format(OSMCDStrs.RStr("set_directory_output_tips"), settings.getCatalogsDirectory()));
		jtfCatalogsDirectory.setText(settings.getCatalogsDirectory().toString());
		jtfCatalogsDirectory.setEnabled(false);
		jtfCatalogsDirectory.setDisabledTextColor(Color.BLACK);
		
		JButton selectCatalogsDirectory = new JButton(OSMCDStrs.RStr("set_directory_output_select"));
		selectCatalogsDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDirectoryChooser dc = new JDirectoryChooser();
				dc.setCurrentDirectory(settings.getCatalogsDirectory());
				if (dc.showDialog(SettingsGUI.this, OSMCDStrs.RStr("set_directory_output_select_dlg_title")) != JFileChooser.APPROVE_OPTION)
					return;
				jtfCatalogsDirectory.setText(dc.getSelectedFile().getAbsolutePath());
				settings.setCatalogsDirectory(dc.getSelectedFile());
			}
		});
		
		catalogsDirPanel.add(jtfCatalogsDirectory, GBC.std().fillH());
		catalogsDirPanel.add(selectCatalogsDirectory, GBC.eol());
				
		// /W #boolNew
		jCheckBoxMakeNewCatalog = new JCheckBox();
		jCheckBoxMakeNewCatalog.setSelected(settings.getCatalogNameMakeNew());
		jCheckBoxMakeNewCatalog.setText(OSMCDStrs.RStr("set_make_new_catalog"));
		//jCheckBoxMakeNewCatalog.setToolTipText(OSMCDStrs.RStr("set_make_new_catalog_tips"));
		jCheckBoxMakeNewCatalog.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				settings.setCatalogNameMakeNew(jCheckBoxMakeNewCatalog.isSelected());
			}
		});
		catalogsDirPanel.add(jCheckBoxMakeNewCatalog, GBC.eol().fillH());

		backGround.add(atlasOutputDirPanel, GBC.eol().fillH());
		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
		backGround.add(tileStoreDirPanel, GBC.eol().fillH());
		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
		backGround.add(catalogsDirPanel, GBC.eol().fillH());
		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	private void addNetworkPanel()
	{
		JPanel backGround = createNewTab(OSMCDStrs.RStr("set_net_title"));
		backGround.setLayout(new GridBagLayout());
		GBC gbc_eolh = GBC.eol().fill(GBC.HORIZONTAL);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_net_connection")));
		threadCount = new JComboBox<Integer>(THREADCOUNT_LIST);
		threadCount.setMaximumRowCount(THREADCOUNT_LIST.length);
		panel.add(threadCount, GBC.std().insets(5, 5, 5, 5).anchor(GBC.EAST));
		panel.add(new JLabel(OSMCDStrs.RStr("set_net_connection_desc")), GBC.eol().fill(GBC.HORIZONTAL));

		bandwidth = new JComboBox<Bandwidth>(Bandwidth.values());
		bandwidth.setMaximumRowCount(bandwidth.getItemCount());
		panel.add(bandwidth, GBC.std().insets(5, 5, 5, 5));
		panel.add(new JLabel(OSMCDStrs.RStr("set_net_bandwidth_desc")), GBC.eol().fill(GBC.HORIZONTAL));

		backGround.add(panel, gbc_eolh);

		// panel = new JPanel(new GridBagLayout());
		// panel.setBorder(createSectionBorder("HTTP User-Agent"));
		// backGround.add(panel, gbc_eolh);

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_net_proxy")));
		// final JLabel proxyTypeLabel = new JLabel(OSMCDStrs.RStr("set_net_proxy_settings"));
		// proxyType = new JComboBox(ProxyType.values());
		// proxyType.setSelectedItem(settings.getProxyType());
		//
		// final JLabel proxyHostLabel = new JLabel(OSMCDStrs.RStr("set_net_proxy_host"));
		// proxyHost = new JTextField(settings.getCustomProxyHost());
		//
		// final JLabel proxyPortLabel = new JLabel(OSMCDStrs.RStr("set_net_proxy_port"));
		// proxyPort = new JTextField(settings.getCustomProxyPort());
		//
		// final JLabel proxyUserNameLabel = new JLabel(OSMCDStrs.RStr("set_net_proxy_username"));
		// proxyUserName = new JTextField(settings.getCustomProxyUserName());
		//
		// final JLabel proxyPasswordLabel = new JLabel(OSMCDStrs.RStr("set_net_proxy_password"));
		// proxyPassword = new JTextField(settings.getCustomProxyPassword());

		ActionListener al = new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// boolean b = ProxyType.CUSTOM.equals(proxyType.getSelectedItem());
				// boolean c = ProxyType.CUSTOM_W_AUTH.equals(proxyType.getSelectedItem());
				// proxyHost.setEnabled(b || c);
				// proxyPort.setEnabled(b || c);
				// proxyHostLabel.setEnabled(b || c);
				// proxyPortLabel.setEnabled(b || c);
				// proxyUserName.setEnabled(c);
				// proxyPassword.setEnabled(c);
				// proxyUserNameLabel.setEnabled(c);
				// proxyPasswordLabel.setEnabled(c);
			}
		};
		al.actionPerformed(null);
		//proxyType.addActionListener(al); // /W ?proxyType nicht initialisiert? -> //

		// panel.add(proxyTypeLabel, GBC.std());
		// panel.add(proxyType, gbc_eolh.insets(5, 2, 5, 2));
		//
		// panel.add(proxyHostLabel, GBC.std());
		// panel.add(proxyHost, gbc_eolh);
		//
		// panel.add(proxyPortLabel, GBC.std());
		// panel.add(proxyPort, gbc_eolh);
		//
		// panel.add(proxyUserNameLabel, GBC.std());
		// panel.add(proxyUserName, gbc_eolh);
		//
		// panel.add(proxyPasswordLabel, GBC.std());
		// panel.add(proxyPassword, gbc_eolh);

		backGround.add(panel, GBC.eol().fillH());

		// ignoreDlErrors = new JCheckBox(OSMCDStrs.RStr("set_net_default_ignore_error"), settings.ignoreDlErrors);
		JPanel jPanel = new JPanel(new GridBagLayout());
		jPanel.setBorder(createSectionBorder(OSMCDStrs.RStr("set_net_default")));
		//jPanel.add(ignoreDlErrors, GBC.std()); // /W ?ignoreDlErrors nicht initialisiert? -> //
		jPanel.add(Box.createHorizontalGlue(), GBC.eol().fillH());
		backGround.add(jPanel, GBC.eol().fillH());

		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	public void createJButtons()
	{
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		okButton = new JButton(OSMBStrs.RStr("OK"));
		cancelButton = new JButton(OSMBStrs.RStr("Cancel"));

		GBC gbc = GBC.std().insets(5, 5, 5, 5);
		buttonPanel.add(okButton, gbc);
		buttonPanel.add(cancelButton, gbc);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadSettings()
	{
		OSMCDSettings s = settings;

		unitSystem.setSelectedItem(s.getUnitSystem());
		tileStoreTab.tileStoreEnabled.setSelected(s.getTileStoreEnabled());

		// language
		languageCombo.setSelectedItem(SupportLocale.localeOf(s.getLocaleLanguage(), s.getLocaleCountry()));

		mapSize.setValue(s.getMaxMapSize());
		mapOverlapTiles.setValue(s.getMapOverlapTiles());

		// atlasOutputDirectory.setText(s.getChartBundleOutputDirectoryString());

		// long limit = s.getBandwidthLimit();
		// for (Bandwidth b : Bandwidth.values())
		// {
		// if (limit <= b.limit)
		// {
		// bandwidth.setSelectedItem(b);
		// break;
		// }
		// }

		int index = Arrays.binarySearch(THREADCOUNT_LIST, s.getDownloadThreadCount());
		if (index < 0)
		{
			if (s.getDownloadThreadCount() > THREADCOUNT_LIST[THREADCOUNT_LIST.length - 1])
				index = THREADCOUNT_LIST.length - 1;
			else
				index = 0;
		}
		threadCount.setSelectedIndex(index);

		defaultExpirationTime.setTimeMilliValue(OSMCDSettings.getTileDefaultExpirationTime());
		maxExpirationTime.setTimeMilliValue(s.getTileMaxExpirationTime());
		minExpirationTime.setTimeMilliValue(s.getTileMinExpirationTime());

		// osmHikingTicket.setText(s.osmHikingTicket);

		// ignoreDlErrors.setSelected(s.ignoreDlErrors);

		paperAtlas.loadSettings(s);
		display.loadSettings(s);
	}

	/**
	 * Reads the user defined settings from the gui and updates the {@link OSMCDSettings} values according to the read gui settings.
	 */
	private void applySettings()
	{
		OSMCDSettings s = settings;

		s.setUnitSystem((UnitSystem) unitSystem.getSelectedItem());
		s.setTileStoreEnabled(tileStoreTab.tileStoreEnabled.isSelected());
		OSMCDSettings.setTileDefaultExpirationTime(defaultExpirationTime.getTimeMilliValue());
		s.setTileMinExpirationTime(minExpirationTime.getTimeMilliValue());
		s.setTileMaxExpirationTime(maxExpirationTime.getTimeMilliValue());
		s.setMaxMapSize(mapSize.getValue());
		s.setMapOverlapTiles((Integer) mapOverlapTiles.getValue());

		Locale locale = ((SupportLocale) languageCombo.getSelectedItem()).locale;
		s.setLocaleLanguage(locale.getLanguage());
		s.setLocaleCountry(locale.getCountry());

		// s.setChartBundleOutputDirectory(atlasOutputDirectory.getText());
		int threads = ((Integer) threadCount.getSelectedItem()).intValue();
		s.setDownloadThreadCount(threads);

		// s.setBandwidthLimit(((Bandwidth) bandwidth.getSelectedItem()).limit);
		//
		// s.setProxyType((ProxyType) proxyType.getSelectedItem());
		// s.setCustomProxyHost(proxyHost.getText());
		// s.setCustomProxyPort(proxyPort.getText());
		// s.setCustomProxyUserName(proxyUserName.getText());
		// s.setCustomProxyPassword(proxyPassword.getText());
		//
		// s.applyProxySettings();

		Vector<String> disabledMaps = new Vector<String>();
		for (IfMapSource ms : disabledMapSourcesModel.getVector())
		{
			disabledMaps.add(ms.getName());
		}
		s.mapSourcesDisabled = disabledMaps;
		
		Vector<String> enabledMaps = new Vector<String>();
		for (IfMapSource ms : enabledMapSourcesModel.getVector())
		{
			enabledMaps.add(ms.getName());
		}
		s.mapSourcesEnabled = enabledMaps;

		// s.ignoreDlErrors = ignoreDlErrors.isSelected();

		paperAtlas.applySettings(s);
		display.applySettings(s);

		if (MainFrame.getMainGUI() == null)
			return;

		MainFrame.getMainGUI().updateMapSourcesList();

		// s.osmHikingTicket = osmHikingTicket.getText().trim();
		try
		{
			MainFrame.getMainGUI().checkAndSaveSettings();
		}
		catch (Exception e)
		{
			log.error("Error saving settings to file", e);
			JOptionPane.showMessageDialog(null, String.format(OSMCDStrs.RStr("set_error_saving_msg"), e.getClass().getSimpleName()),
					OSMCDStrs.RStr("set_error_saving_title"), JOptionPane.ERROR_MESSAGE);
		}

		MainFrame.getMainGUI().previewMap.repaint();
	}

	private void addListeners()
	{

		addWindowListener(new WindowCloseListener());

		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				applySettings();
				// Close the dialog window
				SettingsGUI.this.dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SettingsGUI.this.dispose();
			}
		});

		tabbedPane.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (tabbedPane.getSelectedComponent() == null)
					return;
				// First time the tile store tab is selected start updating the tile store information
				if (tabbedPane.getSelectedComponent() == tileStoreTab)
				{
					// if ("Tile store".equals(tabbedPane.getSelectedComponent().getName())) {
					tabbedPane.removeChangeListener(this);
					tileStoreTab.updateTileStoreInfoPanelAsync(null);
				}
			}
		});

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				SettingsGUI.this.dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	public static final TitledBorder createSectionBorder(String title)
	{
		TitledBorder tb = BorderFactory.createTitledBorder(title);
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border margin = new EmptyBorder(3, 3, 3, 3);
		tb.setBorder(new CompoundBorder(border, margin));
		return tb;
	}

	private class WindowCloseListener extends WindowAdapter
	{

		@Override
		public void windowClosed(WindowEvent event)
		{
			// On close we check if the tile store information retrieval thread
			// is still running and if yes we interrupt it
			tileStoreTab.stopThread();
			// /W #firstStart: write selected tab to settings -> firstStart over!
			OSMCDSettings.getInstance().setSettingsTabSelected(tabbedPane.getSelectedIndex());
		}

	}

	private class MapPacksOnlineUpdateAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			// try {
			// boolean result = MapSourcesUpdater.mapsourcesOnlineUpdate();
			// String msg = (result) ? "Online update successfull" : "No new update avilable";
			// DateFormat df = DateFormat.getDateTimeInstance();
			// Date date = MapSourcesUpdater.getMapSourcesDate(System.getProperties());
			// msg += "\nCurrent iMap source date: " + df.format(date);
			// JOptionPane.showMessageDialog(SettingsGUI.this, msg);
			// if (result)
			// MainFrame.getMainGUI().refreshPreviewMap();
			// } catch (MapSourcesUpdateException e) {
			// JOptionPane.showMessageDialog(SettingsGUI.this, e.getMessage(), "Mapsources online update failed",
			// JOptionPane.ERROR_MESSAGE);
			// }
			// MapPackManager mpm;
			// try
			// {
			// mpm = new MapPackManager(OSMCDSettings.getInstance().getMapSourcesDirectory());
			// int result = mpm.updateMapPacks();
			// switch (result)
			// {
			// case -1:
			// JOptionPane.showMessageDialog(SettingsGUI.this, OSMCDStrs.RStr("set_mapsrc_config_online_update_msg_outdate"),
			// OSMCDStrs.RStr("set_mapsrc_config_online_update_no_update"), JOptionPane.ERROR_MESSAGE);
			// break;
			// case 0:
			// JOptionPane.showMessageDialog(SettingsGUI.this, OSMCDStrs.RStr("set_mapsrc_config_online_update_msg_noneed"),
			// OSMCDStrs.RStr("set_mapsrc_config_online_update_no_update"), JOptionPane.INFORMATION_MESSAGE);
			// break;
			// default:
			// JOptionPane.showMessageDialog(SettingsGUI.this, String.format(OSMCDStrs.RStr("set_mapsrc_config_online_update_msg_done"), result),
			// OSMCDStrs.RStr("set_mapsrc_config_online_update_done"), JOptionPane.INFORMATION_MESSAGE);
			// }
			// }
			// catch (UpdateFailedException e)
			// {
			// JOptionPane.showMessageDialog(SettingsGUI.this, e.getMessage(), OSMCDStrs.RStr("set_mapsrc_config_online_update_failed"), JOptionPane.ERROR_MESSAGE);
			// }
			// catch (Exception e)
			// {
			// OSMCDSettings.getInstance().mapSourcesUpdate.etag = null;
			// GUIExceptionHandler.processException(e);
			// }
		}
	}
}
