package vavi.awt.image.jna.jpegxl;
import com.sun.jna.NativeLong;
import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
/**
 * JNA Wrapper for library <b></b><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public interface Library extends com.sun.jna.Library {
	String JNA_LIBRARY_NAME = "jxl_threads";
	NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(Library.JNA_LIBRARY_NAME);
	Library INSTANCE = Native.load(Library.JNA_LIBRARY_NAME, Library.class);
	interface JXL_BOOL {
		int JXL_TRUE = 1;
		int JXL_FALSE = 0;
	}
	/**
	 * <i>native declaration : jxl/types.h</i><br>
	 * enum values
	 */
	interface JxlDataType {
		/** <i>native declaration : jxl/types.h:43</i> */
		int JXL_TYPE_FLOAT = 0;
		/** <i>native declaration : jxl/types.h:47</i> */
		int JXL_TYPE_UINT8 = 2;
		/** <i>native declaration : jxl/types.h:51</i> */
		int JXL_TYPE_UINT16 = 3;
		/** <i>native declaration : jxl/types.h:54</i> */
		int JXL_TYPE_FLOAT16 = 5;
	}
	/**
	 * <i>native declaration : jxl/types.h</i><br>
	 * enum values
	 */
	interface JxlEndianness {
		/** <i>native declaration : jxl/types.h:72</i> */
		int JXL_NATIVE_ENDIAN = 0;
		/** <i>native declaration : jxl/types.h:74</i> */
		int JXL_LITTLE_ENDIAN = 1;
		/** <i>native declaration : jxl/types.h:76</i> */
		int JXL_BIG_ENDIAN = 2;
	}
	/**
	 * <i>native declaration : jxl/types.h</i><br>
	 * enum values
	 */
	interface JxlProgressiveDetail {
		/** <i>native declaration : jxl/types.h:127</i> */
		int kFrames = 0;
		/** <i>native declaration : jxl/types.h:129</i> */
		int kDC = 1;
		/** <i>native declaration : jxl/types.h:132</i> */
		int kLastPasses = 2;
		/** <i>native declaration : jxl/types.h:135</i> */
		int kPasses = 3;
		/** <i>native declaration : jxl/types.h:137</i> */
		int kDCProgressive = 4;
		/** <i>native declaration : jxl/types.h:139</i> */
		int kDCGroups = 5;
		/** <i>native declaration : jxl/types.h:141</i> */
		int kGroups = 6;
	}
	/**
	 * <i>native declaration : jxl/codestream_header.h</i><br>
	 * enum values
	 */
	interface JxlOrientation {
		/** <i>native declaration : jxl/codestream_header.h:32</i> */
		int JXL_ORIENT_IDENTITY = 1;
		/** <i>native declaration : jxl/codestream_header.h:33</i> */
		int JXL_ORIENT_FLIP_HORIZONTAL = 2;
		/** <i>native declaration : jxl/codestream_header.h:34</i> */
		int JXL_ORIENT_ROTATE_180 = 3;
		/** <i>native declaration : jxl/codestream_header.h:35</i> */
		int JXL_ORIENT_FLIP_VERTICAL = 4;
		/** <i>native declaration : jxl/codestream_header.h:36</i> */
		int JXL_ORIENT_TRANSPOSE = 5;
		/** <i>native declaration : jxl/codestream_header.h:37</i> */
		int JXL_ORIENT_ROTATE_90_CW = 6;
		/** <i>native declaration : jxl/codestream_header.h:38</i> */
		int JXL_ORIENT_ANTI_TRANSPOSE = 7;
		/** <i>native declaration : jxl/codestream_header.h:39</i> */
		int JXL_ORIENT_ROTATE_90_CCW = 8;
	}
	/**
	 * <i>native declaration : jxl/codestream_header.h</i><br>
	 * enum values
	 */
	interface JxlExtraChannelType {
		/** <i>native declaration : jxl/codestream_header.h:45</i> */
		int JXL_CHANNEL_ALPHA = 0;
		/** <i>native declaration : jxl/codestream_header.h:46</i> */
		int JXL_CHANNEL_DEPTH = 1;
		/** <i>native declaration : jxl/codestream_header.h:47</i> */
		int JXL_CHANNEL_SPOT_COLOR = 2;
		/** <i>native declaration : jxl/codestream_header.h:48</i> */
		int JXL_CHANNEL_SELECTION_MASK = 3;
		/** <i>native declaration : jxl/codestream_header.h:49</i> */
		int JXL_CHANNEL_BLACK = 4;
		/** <i>native declaration : jxl/codestream_header.h:50</i> */
		int JXL_CHANNEL_CFA = 5;
		/** <i>native declaration : jxl/codestream_header.h:51</i> */
		int JXL_CHANNEL_THERMAL = 6;
		/** <i>native declaration : jxl/codestream_header.h:52</i> */
		int JXL_CHANNEL_RESERVED0 = 7;
		/** <i>native declaration : jxl/codestream_header.h:53</i> */
		int JXL_CHANNEL_RESERVED1 = 8;
		/** <i>native declaration : jxl/codestream_header.h:54</i> */
		int JXL_CHANNEL_RESERVED2 = 9;
		/** <i>native declaration : jxl/codestream_header.h:55</i> */
		int JXL_CHANNEL_RESERVED3 = 10;
		/** <i>native declaration : jxl/codestream_header.h:56</i> */
		int JXL_CHANNEL_RESERVED4 = 11;
		/** <i>native declaration : jxl/codestream_header.h:57</i> */
		int JXL_CHANNEL_RESERVED5 = 12;
		/** <i>native declaration : jxl/codestream_header.h:58</i> */
		int JXL_CHANNEL_RESERVED6 = 13;
		/** <i>native declaration : jxl/codestream_header.h:59</i> */
		int JXL_CHANNEL_RESERVED7 = 14;
		/** <i>native declaration : jxl/codestream_header.h:60</i> */
		int JXL_CHANNEL_UNKNOWN = 15;
		/** <i>native declaration : jxl/codestream_header.h:61</i> */
		int JXL_CHANNEL_OPTIONAL = 16;
	}
	/**
	 * <i>native declaration : jxl/codestream_header.h</i><br>
	 * enum values
	 */
	interface JxlBlendMode {
		/** <i>native declaration : jxl/codestream_header.h:325</i> */
		int JXL_BLEND_REPLACE = 0;
		/** <i>native declaration : jxl/codestream_header.h:326</i> */
		int JXL_BLEND_ADD = 1;
		/** <i>native declaration : jxl/codestream_header.h:327</i> */
		int JXL_BLEND_BLEND = 2;
		/** <i>native declaration : jxl/codestream_header.h:328</i> */
		int JXL_BLEND_MULADD = 3;
		/** <i>native declaration : jxl/codestream_header.h:329</i> */
		int JXL_BLEND_MUL = 4;
	}

    /**
	 * <i>native declaration : jxl/color_encoding.h</i><br>
	 * enum values
	 */
	interface JxlColorSpace {
		/** <i>native declaration : jxl/color_encoding.h:9</i> */
		int JXL_COLOR_SPACE_RGB = 0;
		/** <i>native declaration : jxl/color_encoding.h:13</i> */
		int JXL_COLOR_SPACE_GRAY = 1;
		/** <i>native declaration : jxl/color_encoding.h:15</i> */
		int JXL_COLOR_SPACE_XYB = 2;
		/** <i>native declaration : jxl/color_encoding.h:17</i> */
		int JXL_COLOR_SPACE_UNKNOWN = 3;
	}
	/**
	 * <i>native declaration : jxl/color_encoding.h</i><br>
	 * enum values
	 */
	interface JxlWhitePoint {
		/** <i>native declaration : jxl/color_encoding.h:29</i> */
		int JXL_WHITE_POINT_D65 = 1;
		/** <i>native declaration : jxl/color_encoding.h:33</i> */
		int JXL_WHITE_POINT_CUSTOM = 2;
		/** <i>native declaration : jxl/color_encoding.h:35</i> */
		int JXL_WHITE_POINT_E = 10;
		/** <i>native declaration : jxl/color_encoding.h:37</i> */
		int JXL_WHITE_POINT_DCI = 11;
	}
	/**
	 * <i>native declaration : jxl/color_encoding.h</i><br>
	 * enum values
	 */
	interface JxlPrimaries {
		/** <i>native declaration : jxl/color_encoding.h:51</i> */
		int JXL_PRIMARIES_SRGB = 1;
		/** <i>native declaration : jxl/color_encoding.h:55</i> */
		int JXL_PRIMARIES_CUSTOM = 2;
		/** <i>native declaration : jxl/color_encoding.h:57</i> */
		int JXL_PRIMARIES_2100 = 9;
		/** <i>native declaration : jxl/color_encoding.h:59</i> */
		int JXL_PRIMARIES_P3 = 11;
	}
	/**
	 * <i>native declaration : jxl/color_encoding.h</i><br>
	 * enum values
	 */
	interface JxlTransferFunction {
		/** <i>native declaration : jxl/color_encoding.h:67</i> */
		int JXL_TRANSFER_FUNCTION_709 = 1;
		/** <i>native declaration : jxl/color_encoding.h:69</i> */
		int JXL_TRANSFER_FUNCTION_UNKNOWN = 2;
		/** <i>native declaration : jxl/color_encoding.h:71</i> */
		int JXL_TRANSFER_FUNCTION_LINEAR = 8;
		/** <i>native declaration : jxl/color_encoding.h:73</i> */
		int JXL_TRANSFER_FUNCTION_SRGB = 13;
		/** <i>native declaration : jxl/color_encoding.h:75</i> */
		int JXL_TRANSFER_FUNCTION_PQ = 16;
		/** <i>native declaration : jxl/color_encoding.h:77</i> */
		int JXL_TRANSFER_FUNCTION_DCI = 17;
		/** <i>native declaration : jxl/color_encoding.h:79</i> */
		int JXL_TRANSFER_FUNCTION_HLG = 18;
		/** <i>native declaration : jxl/color_encoding.h:82</i> */
		int JXL_TRANSFER_FUNCTION_GAMMA = 65535;
	}
	/**
	 * <i>native declaration : jxl/color_encoding.h</i><br>
	 * enum values
	 */
	interface JxlRenderingIntent {
		/** <i>native declaration : jxl/color_encoding.h:88</i> */
		int JXL_RENDERING_INTENT_PERCEPTUAL = 0;
		/** <i>native declaration : jxl/color_encoding.h:90</i> */
		int JXL_RENDERING_INTENT_RELATIVE = 1;
		/** <i>native declaration : jxl/color_encoding.h:92</i> */
		int JXL_RENDERING_INTENT_SATURATION = 2;
		/** <i>native declaration : jxl/color_encoding.h:94</i> */
		int JXL_RENDERING_INTENT_ABSOLUTE = 3;
	}
	/** DEPRECATED: bit-packed 1-bit data type. Use JXL_TYPE_UINT8 instead. */
	int JXL_TYPE_BOOLEAN = 1;
	/** DEPRECATED: uint32_t data type. Use JXL_TYPE_FLOAT instead. */
	int JXL_TYPE_UINT32 = 4;
	/** <i>native declaration : jxl/types.h</i> */
	int JXL_TRUE = (int)1;
	/** <i>native declaration : jxl/types.h</i> */
	int JXL_FALSE = (int)0;
	/** <i>native declaration : jxl/parallel_runner.h</i> */
	int JXL_PARALLEL_RET_RUNNER_ERROR = -1;
	/** <i>native declaration : jxl/version.h</i> */
	int JPEGXL_MAJOR_VERSION = 0;
	/** <i>native declaration : jxl/version.h</i> */
	int JPEGXL_MINOR_VERSION = 7;
	/** <i>native declaration : jxl/version.h</i> */
	int JPEGXL_PATCH_VERSION = 0;
	/** <i>native declaration : jxl/version.h</i> */
	int JPEGXL_NUMERIC_VERSION = (0 << 24) | (7 << 16) | (0 << 8) | 0;
	/** <i>native declaration : jxl/memory_manager.h</i> */
	interface jpegxl_alloc_func extends Callback {
		Pointer apply(Pointer opaque, NativeLong size);
	}
	/** <i>native declaration : jxl/memory_manager.h</i> */
	interface jpegxl_free_func extends Callback {
		void apply(Pointer opaque, Pointer address);
	}
	/** <i>native declaration : jxl/parallel_runner.h</i> */
	interface JxlParallelRunInit extends Callback {
		int apply(Pointer jpegxl_opaque, NativeLong num_threads);
	}
	/** <i>native declaration : jxl/parallel_runner.h</i> */
	interface JxlParallelRunFunction extends Callback {
		void apply(Pointer jpegxl_opaque, int value, NativeLong thread_id);
	}
	/** <i>native declaration : jxl/parallel_runner.h</i> */
	interface JxlParallelRunner extends Callback {
		int apply(Pointer runner_opaque, Pointer jpegxl_opaque, Library.JxlParallelRunInit init, Library.JxlParallelRunFunction func, int start_range, int end_range);
	}
	/** <i>native declaration : jxl/cms_interface.h</i> */
	interface jpegxl_cms_init_func extends Callback {
		Pointer apply(Pointer init_data, NativeLong num_threads, NativeLong pixels_per_thread, vavi.awt.image.jna.jpegxl.JxlColorProfile input_profile, vavi.awt.image.jna.jpegxl.JxlColorProfile output_profile, float intensity_target);
	}
	/** <i>native declaration : jxl/cms_interface.h</i> */
	interface jpegxl_cms_get_buffer_func extends Callback {
		FloatByReference apply(Pointer user_data, NativeLong thread);
	}
	/** <i>native declaration : jxl/cms_interface.h</i> */
	interface jpegxl_cms_run_func extends Callback {
		int apply(Pointer user_data, NativeLong thread, FloatByReference input_buffer, FloatByReference output_buffer, NativeLong num_pixels);
	}
	/** <i>native declaration : jxl/cms_interface.h</i> */
	interface jpegxl_cms_destroy_func extends Callback {
		void apply(Pointer voidPtr1);
	}
	/**
	 * Creates the runner for JxlResizableParallelRunner. Use as the opaque
	 * runner. The runner will execute tasks on the calling thread until
	 * {@link #JxlResizableParallelRunnerSetThreads} is called.
	 */
	Pointer JxlResizableParallelRunnerCreate(JxlMemoryManagerStruct memory_manager);
	/**
	 * Changes the number of threads for JxlResizableParallelRunner.
	 */
	void JxlResizableParallelRunnerSetThreads(Pointer runner_opaque, int num_threads);
	/**
	 * Suggests a number of threads to use for an image of given size.
	 */
	int	JxlResizableParallelRunnerSuggestThreads(long xsize, long ysize);
	/**
	 * Destroys the runner created by JxlResizableParallelRunnerCreate.
	 */
	void JxlResizableParallelRunnerDestroy(Pointer runner_opaque);
	/**
	 * Parallel runner internally using std::thread. Use as JxlParallelRunner.
	 */
	Pointer JxlResizableParallelRunner = JNA_NATIVE_LIB.getGlobalVariableAddress("JxlResizableParallelRunner");

	/**
	 * Creates the runner for JxlThreadParallelRunner. Use as the opaque
	 * runner.
	 */
	Pointer JxlThreadParallelRunnerCreate(Pointer memory_manager, NativeLong num_worker_threads);
	/**
	 * Destroys the runner created by JxlThreadParallelRunnerCreate.
	 */
	void JxlThreadParallelRunnerDestroy(Pointer runner_opaque);
	/**
	 * Parallel runner internally using std::thread. Use as JxlParallelRunner.
	 */
	Pointer JxlThreadParallelRunner = JNA_NATIVE_LIB.getGlobalVariableAddress("JxlThreadParallelRunner");
}
