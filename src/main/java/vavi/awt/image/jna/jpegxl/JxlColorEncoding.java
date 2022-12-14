package vavi.awt.image.jna.jpegxl;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : jxl/color_encoding.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class JxlColorEncoding extends Structure {
	/**
	 * @see Library.JxlColorSpace
	 * C type : JxlColorSpace
	 */
	public int color_space;
	/**
	 * @see Library.JxlWhitePoint
	 * C type : JxlWhitePoint
	 */
	public int white_point;
	/** C type : double[2] */
	public double[] white_point_xy = new double[2];
	/**
	 * @see Library.JxlPrimaries
	 * C type : JxlPrimaries
	 */
	public int primaries;
	/** C type : double[2] */
	public double[] primaries_red_xy = new double[2];
	/** C type : double[2] */
	public double[] primaries_green_xy = new double[2];
	/** C type : double[2] */
	public double[] primaries_blue_xy = new double[2];
	/**
	 * @see Library.JxlTransferFunction
	 * C type : JxlTransferFunction
	 */
	public int transfer_function;
	public double gamma;
	/**
	 * @see Library.JxlRenderingIntent
	 * C type : JxlRenderingIntent
	 */
	public int rendering_intent;
	public JxlColorEncoding() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("color_space", "white_point", "white_point_xy", "primaries", "primaries_red_xy", "primaries_green_xy", "primaries_blue_xy", "transfer_function", "gamma", "rendering_intent");
	}
	public JxlColorEncoding(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends JxlColorEncoding implements Structure.ByReference {
	}
	public static class ByValue extends JxlColorEncoding implements Structure.ByValue {
	}
}
