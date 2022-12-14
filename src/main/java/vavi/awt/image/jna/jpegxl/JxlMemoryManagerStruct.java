package vavi.awt.image.jna.jpegxl;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import vavi.awt.image.jna.jpegxl.Library.jpegxl_alloc_func;
import vavi.awt.image.jna.jpegxl.Library.jpegxl_free_func;
/**
 * <i>native declaration : jxl/memory_manager.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class JxlMemoryManagerStruct extends Structure {
	/** C type : void* */
	public Pointer opaque;
	/** C type : jpegxl_alloc_func */
	public jpegxl_alloc_func alloc;
	/** C type : jpegxl_free_func */
	public jpegxl_free_func free;
	public JxlMemoryManagerStruct() {
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("opaque", "alloc", "free");
	}
	/**
	 * @param opaque C type : void*<br>
	 * @param alloc C type : jpegxl_alloc_func<br>
	 * @param free C type : jpegxl_free_func
	 */
	public JxlMemoryManagerStruct(Pointer opaque, jpegxl_alloc_func alloc, jpegxl_free_func free) {
		this.opaque = opaque;
		this.alloc = alloc;
		this.free = free;
	}
	public JxlMemoryManagerStruct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends JxlMemoryManagerStruct implements Structure.ByReference {
	}
	public static class ByValue extends JxlMemoryManagerStruct implements Structure.ByValue {
	}
}
