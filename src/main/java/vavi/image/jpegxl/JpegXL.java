/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.image.jpegxl;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import vavi.awt.image.jna.jpegxl.JxlBasicInfo;
import vavi.awt.image.jna.jpegxl.JxlPixelFormat;
import vavi.awt.image.jna.jpegxl.Library;
import vavi.awt.image.jna.jpegxl.decode.DecodeLibrary;
import vavi.util.Debug;


/**
 * JpegXL.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-12 nsano initial version <br>
 */
public class JpegXL {

    private final PointerByReference dec;
    private final Pointer runner;
    private final JxlBasicInfo.ByReference info;
    private final JxlPixelFormat format;
    private final int bytes;

    /** */
    public JpegXL() {
        // Multi-threaded parallel runner.
        this.runner = Library.INSTANCE.JxlResizableParallelRunnerCreate(null);
//        Pointer runner = Library.INSTANCE.JxlThreadParallelRunnerCreate(null, null);
Debug.println(Level.FINE, "runner: " + runner);

        this.dec = DecodeLibrary.INSTANCE.JxlDecoderCreate(null);
Debug.println(Level.FINE, "dec: " + dec);

        int status = DecodeLibrary.INSTANCE.JxlDecoderSubscribeEvents(dec,
                DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO |
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING |
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE);
        if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
            throw new IllegalStateException("JxlDecoderSubscribeEvents failed: " + status);
        }
Debug.println(Level.FINE, "JxlResizableParallelRunner: " + Library.JxlResizableParallelRunner);
//Debug.println("JxlThreadParallelRunner: " + Library.JxlThreadParallelRunner);
        status = DecodeLibrary.INSTANCE.JxlDecoderSetParallelRunner(dec,
                Library.JxlResizableParallelRunner,
//            Library.JxlThreadParallelRunner,
                runner);
        if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
            throw new IllegalStateException("JxlDecoderSetParallelRunner failed: " + status);
        }

        this.info = new JxlBasicInfo.ByReference();
        // 8-bit integer with 4-channel RGBA
        this.format = new JxlPixelFormat(4, Library.JxlDataType.JXL_TYPE_UINT8, Library.JxlEndianness.JXL_LITTLE_ENDIAN, new NativeLong(0));
        this.bytes = format.necessaryBytes();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
Debug.println("shutdownHook");
            DecodeLibrary.INSTANCE.JxlDecoderDestroy(dec);
            Library.INSTANCE.JxlResizableParallelRunnerDestroy(runner);
//        Library.INSTANCE.JxlThreadParallelRunnerDestroy(runner);
        }));
    }

    /** */
    public static boolean canDecode(byte[] jxl) {
        int r = DecodeLibrary.INSTANCE.JxlSignatureCheck(jxl, new NativeLong(jxl.length));
Debug.println(Level.FINE, "JxlSignatureCheck: " + r);
        return r == DecodeLibrary.JxlSignature.JXL_SIG_CODESTREAM ||
                r == DecodeLibrary.JxlSignature.JXL_SIG_CONTAINER;
    }

    /** */
    public BufferedImage decode(byte[] jxl) {

        ByteBuffer bbs = ByteBuffer.allocateDirect(jxl.length);
        bbs.put(jxl);
        int status = DecodeLibrary.INSTANCE.JxlDecoderSetInput(dec, Native.getDirectBufferPointer(bbs), new NativeLong(bbs.capacity()));
        if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
Debug.printf(Level.FINE, "JxlDecoderStatus: " + status);
            throw new IllegalStateException("JxlDecoderSetInput failed: " + status);
        }
        DecodeLibrary.INSTANCE.JxlDecoderCloseInput(dec);

        ByteBuffer pixels = null;
        int xsize = 0;
        int ysize = 0;
        while (true) {
            status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(dec);

            if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_ERROR) {
Debug.printf(Level.FINE, "JXL_DEC_ERROR");
                throw new IllegalStateException("Decoder error");
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_MORE_INPUT) {
Debug.printf(Level.FINE, "JXL_DEC_NEED_MORE_INPUT");
                throw new IllegalStateException("Error, already provided all input");
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
Debug.printf(Level.FINE, "JXL_DEC_BASIC_INFO");
                status = DecodeLibrary.INSTANCE.JxlDecoderGetBasicInfo(dec, info);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetBasicInfo failed: " + status);
                }
                xsize = info.xsize;
                ysize = info.ysize;
Debug.printf(Level.FINE, "size: %dx%d", xsize, ysize);
                Library.INSTANCE.JxlResizableParallelRunnerSetThreads(
                        runner,
                        Library.INSTANCE.JxlResizableParallelRunnerSuggestThreads(info.xsize, info.ysize));
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING) {
Debug.printf(Level.FINE, "JXL_DEC_COLOR_ENCODING");
                // Get the ICC color profile of the pixel data
                NativeLongByReference icc_size = new NativeLongByReference();
                status = DecodeLibrary.INSTANCE.JxlDecoderGetICCProfileSize(
                        dec, format,
                        DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA, icc_size);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetICCProfileSize failed: " + status);
                }
                ByteBuffer icc_profile = ByteBuffer.allocateDirect(icc_size.getValue().intValue());
Debug.printf(Level.FINE, "icc_profile: " + icc_profile.capacity());
                status = DecodeLibrary.INSTANCE.JxlDecoderGetColorAsICCProfile(
                        dec, format,
                        DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA,
                        Native.getDirectBufferPointer(icc_profile), new NativeLong(icc_profile.capacity()));
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetColorAsICCProfile failed: " + status);
                }
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
Debug.printf(Level.FINE, "JXL_DEC_NEED_IMAGE_OUT_BUFFER");
                NativeLongByReference buffer_size = new NativeLongByReference();
                status = DecodeLibrary.INSTANCE.JxlDecoderImageOutBufferSize(dec, format, buffer_size);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderImageOutBufferSize failed: " + status);
                }
Debug.printf(Level.FINE, "buffer_size: " + (buffer_size.getValue()));
                if (buffer_size.getValue().intValue() != (long) xsize * ysize * format.num_channels * bytes) {
                    String sizes = String.format("%d, %d",
                            buffer_size.getValue().intValue(), (long) xsize * ysize * format.num_channels * bytes);
Debug.printf(Level.FINE, "sizes: " + sizes);
//                    throw new IllegalStateException("Invalid out buffer size " + sizes);
                }
                pixels = ByteBuffer.allocateDirect(xsize * ysize * format.num_channels * bytes);
                Pointer pixels_buffer = Native.getDirectBufferPointer(pixels);
                int pixels_buffer_size = pixels.capacity() * bytes;
                status = DecodeLibrary.INSTANCE.JxlDecoderSetImageOutBuffer(
                        dec, format,
                        pixels_buffer, new NativeLong(pixels_buffer_size));
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderSetImageOutBuffer failed: " + status);
                }
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE) {
Debug.printf(Level.FINE, "JXL_DEC_FULL_IMAGE");
                // Nothing to do. Do not yet return. If the image is an animation, more
                // full frames may be decoded. This example only keeps the last one.
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
Debug.printf(Level.FINE, "JXL_DEC_SUCCESS");
                // All decoding successfully finished.
                // It's not required to call JxlDecoderReleaseInput(dec) here since
                // the decoder will be destroyed.
                break;
            } else {
                throw new IllegalStateException("Unknown decoder status: " + status);
            }
        }

        pixels.position(0);
Debug.printf(Level.FINE, "pixel: " + pixels.capacity() + ", " + pixels.limit());
        BufferedImage image = new BufferedImage(xsize, ysize, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] d = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        pixels.get(d);
        int p = 0;
        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                d[p + 3] = pixels.get();
                d[p + 2] = pixels.get();
                d[p + 1] = pixels.get();
                d[p + 0] = pixels.get();
                p += 4;
            }
        }
        DecodeLibrary.INSTANCE.JxlDecoderReleaseInput(dec);
        return image;
    }
}
