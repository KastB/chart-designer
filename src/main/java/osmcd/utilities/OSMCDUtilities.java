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
package osmcd.utilities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import osmb.mapsources.ACMapSource;
import osmb.mapsources.MP2MapSpace;
import osmb.utilities.Charsets;
import osmb.utilities.OSMBStrs;
import osmb.utilities.OSMBUtilities;
import osmcd.OSMCDApp;
import osmcd.exceptions.OSMCDOutOfMemoryException;

public class OSMCDUtilities extends OSMBUtilities
{
	public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);
	public static final DecimalFormatSymbols DFS_ENG = new DecimalFormatSymbols(Locale.ENGLISH);
	public static final DecimalFormatSymbols DFS_LOCAL = new DecimalFormatSymbols();
	public static final DecimalFormat FORMAT_6_DEC = new DecimalFormat("#0.######");
	public static final DecimalFormat FORMAT_6_DEC_ENG = new DecimalFormat("#0.######", DFS_ENG);
	public static final DecimalFormat FORMAT_2_DEC = new DecimalFormat("0.00");
	private static final DecimalFormat cDmsMinuteFormatter = new DecimalFormat("00");
	private static final DecimalFormat cDmsSecondFormatter = new DecimalFormat("00.0");

	private static final Logger log = Logger.getLogger(OSMCDUtilities.class);

	public static final long SECONDS_PER_HOUR = TimeUnit.HOURS.toSeconds(1);
	public static final long SECONDS_PER_DAY = TimeUnit.DAYS.toSeconds(1);

	public static boolean testJaiColorQuantizerAvailable()
	{
		try
		{
			Class<?> c = Class.forName("javax.media.jai.operator.ColorQuantizerDescriptor");
			if (c != null)
				return true;
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
		catch (Throwable t)
		{
			log.error("Error in testJaiColorQuantizerAvailable():", t);
			return false;
		}
		return true;
	}

	public static BufferedImage createEmptyTileImage(ACMapSource mapSource)
	{
		int tileSize = MP2MapSpace.getTileSize();
		Color color = mapSource.getBackgroundColor();

		int imageType;
		if (color.getAlpha() == 255)
			imageType = BufferedImage.TYPE_INT_RGB;
		else
			imageType = BufferedImage.TYPE_INT_ARGB;
		BufferedImage emptyImage = new BufferedImage(tileSize, tileSize, imageType);
		Graphics2D g = emptyImage.createGraphics();
		try
		{
			g.setColor(color);
			g.fillRect(0, 0, tileSize, tileSize);
		}
		finally
		{
			g.dispose();
		}
		return emptyImage;
	}

	public static BufferedImage safeCreateBufferedImage(int width, int height, int imageType)
	{
		try
		{
			return new BufferedImage(width, height, imageType);
		}
		catch (OutOfMemoryError e)
		{
			int bytesPerPixel = getBytesPerPixel(imageType);
			if (bytesPerPixel < 0)
				throw e;
			long requiredMemory = ((long) width) * ((long) height) * bytesPerPixel;
			String message = String.format("Available free memory not sufficient for creating image of size %dx%d pixels", width, height);
			throw new OSMCDOutOfMemoryException(requiredMemory, message);
		}
	}

	/**
	 * 
	 * @param imageType
	 *          as used for {@link BufferedImage#BufferedImage(int, int, int)}
	 * @return
	 */
	public static int getBytesPerPixel(int bufferedImageType)
	{
		switch (bufferedImageType)
		{
			case BufferedImage.TYPE_INT_ARGB:
			case BufferedImage.TYPE_INT_ARGB_PRE:
			case BufferedImage.TYPE_INT_BGR:
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_4BYTE_ABGR_PRE:
				return 4;
			case BufferedImage.TYPE_3BYTE_BGR:
				return 3;
			case BufferedImage.TYPE_USHORT_GRAY:
			case BufferedImage.TYPE_USHORT_565_RGB:
			case BufferedImage.TYPE_USHORT_555_RGB:
				return 2;
			case BufferedImage.TYPE_BYTE_GRAY:
			case BufferedImage.TYPE_BYTE_BINARY:
			case BufferedImage.TYPE_BYTE_INDEXED:
				return 1;
		}
		return -1;
	}

	public static byte[] createEmptyTileData(ACMapSource mapSource)
	{
		BufferedImage emptyImage = createEmptyTileImage(mapSource);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
		try
		{
			ImageIO.write(emptyImage, mapSource.getTileImageType().getFileExt(), buf);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		byte[] emptyTileData = buf.toByteArray();
		return emptyTileData;
	}

	@SuppressWarnings("unused") // W #unused
	private static final byte[] PNG = new byte[]
	{ (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
	@SuppressWarnings("unused") // W #unused
	private static final byte[] JPG = new byte[]
	{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, (byte) 0x00, 0x10, 'J', 'F', 'I', 'F' };
	@SuppressWarnings("unused") // W #unused
	private static final byte[] GIF_1 = "GIF87a".getBytes();
	@SuppressWarnings("unused") // W #unused
	private static final byte[] GIF_2 = "GIF89a".getBytes();

	public static InputStream loadResourceAsStream(String resourcePath) throws IOException
	{
		return OSMCDApp.class.getResourceAsStream("resources/" + resourcePath);
	}

	public static String loadTextResource(String resourcePath) throws IOException
	{
		DataInputStream in = new DataInputStream(OSMCDApp.class.getResourceAsStream("resources/" + resourcePath));
		byte[] buf;
		buf = new byte[in.available()];
		in.readFully(buf);
		in.close();
		String text = new String(buf, Charsets.UTF_8);
		return text;
	}

	/**
	 * 
	 * @param imageName
	 *          imagePath resource path relative to the class {@link OSMCDApp}
	 * @return
	 */
	public static ImageIcon loadResourceImageIcon(String imageName)
	{
		URL url = OSMCDApp.class.getResource("resources/images/" + imageName);
		return new ImageIcon(url);
	}

	public static URL getResourceImageUrl(String imageName)
	{
		return OSMCDApp.class.getResource("resources/images/" + imageName);
	}

	/**
	 * Checks if the current {@link Thread} has been interrupted and if so a {@link InterruptedException}. Therefore it behaves similar to
	 * {@link Thread#sleep(long)} without actually slowing down anything by sleeping a certain amount of time.
	 * 
	 * @throws InterruptedException
	 */
	public static void checkForInterruption() throws InterruptedException
	{
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
	}

	/**
	 * Checks if the current {@link Thread} has been interrupted and if so a {@link RuntimeException} will be thrown. This method is useful for long lasting
	 * operations that do not allow to throw an {@link InterruptedException}.
	 * 
	 * @throws RuntimeException
	 */
	public static void checkForInterruptionRt() throws RuntimeException
	{
		if (Thread.currentThread().isInterrupted())
			throw new RuntimeException(new InterruptedException());
	}

	public static double parseLocaleDouble(String text) throws ParseException
	{
		ParsePosition pos = new ParsePosition(0);
		Number n = OSMBUtilities.FORMAT_6_DEC.parse(text, pos);
		if (n == null)
			throw new ParseException("Unknown error", 0);
		if (pos.getIndex() != text.length())
			throw new ParseException("Text ends with unparsable characters", pos.getIndex());
		return n.doubleValue();
	}

	public static void showTooltipNow(JComponent c)
	{
		Action toolTipAction = c.getActionMap().get("postTip");
		if (toolTipAction != null)
		{
			ActionEvent postTip = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "");
			toolTipAction.actionPerformed(postTip);
		}
	}

	/**
	 * Formats a byte value depending on the size to "Bytes", "KiBytes", "MiByte" and "GiByte"
	 * 
	 * @param bytes
	 * @return Formatted {@link String}
	 */
	public static String formatBytes(long bytes)
	{
		if (bytes < 1024)
			return Long.toString(bytes) + " " + OSMBStrs.RStr("Bytes");
		if (bytes < 1048576)
			return FORMAT_2_DEC.format(bytes / 1024d) + " " + OSMBStrs.RStr("KiByte");
		if (bytes < 1073741824)
			return FORMAT_2_DEC.format(bytes / 1048576d) + " " + OSMBStrs.RStr("MiByte");
		return FORMAT_2_DEC.format(bytes / 1073741824d) + " " + OSMBStrs.RStr("GiByte");
	}

	public static String formatDurationSeconds(long seconds)
	{
		long x = seconds;
		long days = x / SECONDS_PER_DAY;
		x %= SECONDS_PER_DAY;
		int years = (int) (days / 365);
		days -= (years * 365);

		int months = (int) (days * 12d / 365d);
		String m = (months == 1) ? "month" : "months";

		if (years > 5)
			return String.format("%d years", years);
		if (years > 0)
		{
			String y = (years == 1) ? "year" : "years";
			return String.format("%d %s %d %s", years, y, months, m);
		}
		String d = (days == 1) ? "day" : "days";
		if (months > 0)
		{
			days -= months * (365d / 12d);
			return String.format("%d %s %d %s", months, m, days, d);
		}
		long hours = TimeUnit.SECONDS.toHours(x);
		String h = (hours == 1) ? "hour" : "hours";
		x -= hours * SECONDS_PER_HOUR;
		if (days > 0)
			return String.format("%d %s %d %s", days, d, hours, h);
		long minutes = TimeUnit.SECONDS.toMinutes(x);
		String min = (minutes == 1) ? "minute" : "minutes";
		if (hours > 0)
			return String.format("%d %s %d %s", hours, h, minutes, min);
		else
			return String.format("%d %s", minutes, min);
	}

	public static byte[] getFileBytes(File file) throws IOException
	{
		int size = (int) file.length();
		byte[] buffer = new byte[size];
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try
		{
			in.readFully(buffer);
			return buffer;
		}
		finally
		{
			closeStream(in);
		}
	}

	/**
	 * Fully reads data from <tt>in</tt> to an internal buffer until the end of in has been reached. Then the buffer is returned.
	 * 
	 * @param in
	 *          data source to be read
	 * @return buffer all data available in in
	 * @throws IOException
	 */
	public static byte[] getInputBytes(InputStream in) throws IOException
	{
		int initialBufferSize = in.available();
		if (initialBufferSize <= 0)
			initialBufferSize = 32768;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(initialBufferSize);
		byte[] b = new byte[1024];
		int ret = 0;
		while ((ret = in.read(b)) >= 0)
		{
			buffer.write(b, 0, ret);
		}
		return buffer.toByteArray();
	}

	/**
	 * Fully reads data from <tt>in</tt> the read data is discarded.
	 * 
	 * @param in
	 * @throws IOException
	 */
	public static void readFully(InputStream in) throws IOException
	{
		byte[] b = new byte[4096];
		while ((in.read(b)) >= 0)
		{
		}
	}

	public static String prettyPrintLatLon(double coord, boolean isCoordKindLat)
	{
		boolean neg = coord < 0.0;
		String c;
		if (isCoordKindLat)
		{
			c = (neg ? "S" : "N");
		}
		else
		{
			c = (neg ? "W" : "E");
		}
		double tAbsCoord = Math.abs(coord);
		int tDegree = (int) tAbsCoord;
		double tTmpMinutes = (tAbsCoord - tDegree) * 60;
		int tMinutes = (int) tTmpMinutes;
		double tSeconds = (tTmpMinutes - tMinutes) * 60;
		return c + tDegree + "\u00B0" + cDmsMinuteFormatter.format(tMinutes) + "\'" + cDmsSecondFormatter.format(tSeconds) + "\"";
	}

	/**
	 * Returns the file path for the selected class. If the class is located inside a JAR file the return value contains the directory that contains the JAR file.
	 * If the class file is executed outside of an JAR the root directory holding the class/package structure is returned.
	 * 
	 * @param mainClass
	 * @return
	 * @throws URISyntaxException
	 */
	public static File getClassLocation(Class<?> mainClass)
	{
		ProtectionDomain pDomain = mainClass.getProtectionDomain();
		CodeSource cSource = pDomain.getCodeSource();
		File f;
		try
		{
			URL loc = cSource.getLocation(); // file:/c:/almanac14/examples/
			f = new File(loc.toURI());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unable to determine program directory: ", e);
		}
		if (f.isDirectory())
		{
			// Class is executed from class/package structure from file system
			return f;
		}
		else
		{
			// Class is executed from inside of a JAR -> f references the JAR
			// file
			return f.getParentFile();
		}
	}

	/**
	 * Saves <code>data</code> to the file specified by <code>filename</code>.
	 * 
	 * @param filename
	 * @param data
	 * @throws IOException
	 */
	public static void saveBytes(String filename, byte[] data) throws IOException
	{
		FileOutputStream fo = null;
		try
		{
			fo = new FileOutputStream(filename);
			fo.write(data);
		}
		finally
		{
			closeStream(fo);
		}
	}

	/**
	 * Saves <code>data</code> to the file specified by <code>filename</code>.
	 * 
	 * @param filename
	 * @param data
	 * @return Data has been saved successfully?
	 */
	public static boolean saveBytesEx(String filename, byte[] data)
	{
		FileOutputStream fo = null;
		try
		{
			fo = new FileOutputStream(filename);
			fo.write(data);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{
			closeStream(fo);
		}
	}

	public static int getJavaMaxHeapMB()
	{
		try
		{
			return (int) (Runtime.getRuntime().maxMemory() / 1048576l);
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	/**
	 * 
	 * @param value
	 *          positive value
	 * @return 0 if no bit is set else the highest bit that is one in <code>value</code>
	 */
	public static int getHighestBitSet(int value)
	{
		int bit = 0x40000000;
		for (int i = 31; i > 0; i--)
		{
			int test = bit & value;
			if (test != 0)
				return i;
			bit >>= 1;
		}
		return 0;
	}

}
