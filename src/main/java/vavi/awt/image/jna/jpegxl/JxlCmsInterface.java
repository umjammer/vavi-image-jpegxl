package vavi.awt.image.jna.jpegxl;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import vavi.awt.image.jna.jpegxl.Library.jpegxl_cms_destroy_func;
import vavi.awt.image.jna.jpegxl.Library.jpegxl_cms_get_buffer_func;
import vavi.awt.image.jna.jpegxl.Library.jpegxl_cms_init_func;
import vavi.awt.image.jna.jpegxl.Library.jpegxl_cms_run_func;
/**
 * <i>native declaration : jxl/cms_interface.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class JxlCmsInterface extends Structure {
	/** C type : void* */
	public Pointer init_data;
	/** C type : jpegxl_cms_init_func */
	public jpegxl_cms_init_func init;
	/** C type : jpegxl_cms_get_buffer_func */
	public jpegxl_cms_get_buffer_func get_src_buf;
	/** C type : jpegxl_cms_get_buffer_func */
	public jpegxl_cms_get_buffer_func get_dst_buf;
	/** C type : jpegxl_cms_run_func */
	public jpegxl_cms_run_func run;
	/** C type : jpegxl_cms_destroy_func */
	public jpegxl_cms_destroy_func destroy;
	public JxlCmsInterface() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("init_data", "init", "get_src_buf", "get_dst_buf", "run", "destroy");
	}
	/**
	 * @param init_data C type : void*<br>
	 * @param init C type : jpegxl_cms_init_func<br>
	 * @param get_src_buf C type : jpegxl_cms_get_buffer_func<br>
	 * @param get_dst_buf C type : jpegxl_cms_get_buffer_func<br>
	 * @param run C type : jpegxl_cms_run_func<br>
	 * @param destroy C type : jpegxl_cms_destroy_func
	 */
	public JxlCmsInterface(Pointer init_data, jpegxl_cms_init_func init, jpegxl_cms_get_buffer_func get_src_buf, jpegxl_cms_get_buffer_func get_dst_buf, jpegxl_cms_run_func run, jpegxl_cms_destroy_func destroy) {
		super();
		this.init_data = init_data;
		this.init = init;
		this.get_src_buf = get_src_buf;
		this.get_dst_buf = get_dst_buf;
		this.run = run;
		this.destroy = destroy;
	}
	public JxlCmsInterface(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends JxlCmsInterface implements Structure.ByReference {
	}
	public static class ByValue extends JxlCmsInterface implements Structure.ByValue {
	}
}