/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.image.jpegxl;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
 * Test2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-03 nsano initial version <br>
 */
public class Test2 {

    /**
     * @param args 0: jxl
     */
    public static void main(String[] args) throws Exception{
//        String file = args[0];
        String file = "src/test/resources/test2.jxl";
        byte[] jxl = Files.readAllBytes(Paths.get(file));

        // Multi-threaded parallel runner.
        Pointer runner = Library.INSTANCE.JxlResizableParallelRunnerCreate(null);
//        Pointer runner = Library.INSTANCE.JxlThreadParallelRunnerCreate(null, null);
Debug.println("runner: " + runner);

        PointerByReference dec = DecodeLibrary.INSTANCE.JxlDecoderCreate(null);
Debug.println("dec: " + dec);

        int status = DecodeLibrary.INSTANCE.JxlDecoderSubscribeEvents(dec,
                DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO |
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING |
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE);
        if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
            throw new IllegalStateException("JxlDecoderSubscribeEvents failed: " + status);
        }
try {
Debug.println("JxlResizableParallelRunner: " + Library.JxlResizableParallelRunner);
//Debug.println("JxlThreadParallelRunner: " + Library.JxlThreadParallelRunner);
        status = DecodeLibrary.INSTANCE.JxlDecoderSetParallelRunner(dec,
            Library.JxlResizableParallelRunner,
//            Library.JxlThreadParallelRunner,
            runner);
        if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
            throw new IllegalStateException("JxlDecoderSetParallelRunner failed: " + status);
        }

        JxlBasicInfo.ByReference info = new JxlBasicInfo.ByReference();
        // 8-bit integer with 4-channel RGBA
        JxlPixelFormat format = new JxlPixelFormat(4, Library.JxlDataType.JXL_TYPE_UINT8, Library.JxlEndianness.JXL_LITTLE_ENDIAN, new NativeLong(0));
        int bytes = format.necessaryBytes();

        ByteBuffer bbs = ByteBuffer.allocateDirect(jxl.length);
        bbs.put(jxl);
        DecodeLibrary.INSTANCE.JxlDecoderSetInput(dec, Native.getDirectBufferPointer(bbs), new NativeLong(bbs.capacity()));

        ByteBuffer pixels = null;
        int xsize = 0;
        int ysize = 0;
        while (true) {
            status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(dec);

            if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_ERROR) {
Debug.printf("JXL_DEC_ERROR");
                throw new IllegalStateException("Decoder error");
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_MORE_INPUT) {
Debug.printf("JXL_DEC_NEED_MORE_INPUT");
                throw new IllegalStateException("Error, already provided all input");
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
Debug.printf("JXL_DEC_BASIC_INFO");
                status = DecodeLibrary.INSTANCE.JxlDecoderGetBasicInfo(dec, info);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetBasicInfo failed: " + status);
                }
                xsize = info.xsize;
                ysize = info.ysize;
Debug.printf("size: %dx%d", xsize, ysize);
                Library.INSTANCE.JxlResizableParallelRunnerSetThreads(
                        runner,
                        Library.INSTANCE.JxlResizableParallelRunnerSuggestThreads(info.xsize, info.ysize));
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING) {
Debug.printf("JXL_DEC_COLOR_ENCODING");
                // Get the ICC color profile of the pixel data
                NativeLongByReference icc_size = new NativeLongByReference();
                status = DecodeLibrary.INSTANCE.JxlDecoderGetICCProfileSize(
                        dec, format,
                        DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA, icc_size);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetICCProfileSize failed: " + status);
                }
                ByteBuffer icc_profile = ByteBuffer.allocateDirect(icc_size.getValue().intValue());
Debug.printf("icc_profile: " + icc_profile.capacity());
                status = DecodeLibrary.INSTANCE.JxlDecoderGetColorAsICCProfile(
                        dec, format,
                        DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA,
                        Native.getDirectBufferPointer(icc_profile), new NativeLong(icc_profile.capacity()));
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetColorAsICCProfile failed: " + status);
                }
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
Debug.printf("JXL_DEC_NEED_IMAGE_OUT_BUFFER");
                NativeLongByReference buffer_size = new NativeLongByReference();
                status = DecodeLibrary.INSTANCE.JxlDecoderImageOutBufferSize(dec, format, buffer_size);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderImageOutBufferSize failed: " + status);
                }
Debug.printf("buffer_size: " + (buffer_size.getValue()));
                if (buffer_size.getValue().intValue() != (long) xsize * ysize * format.num_channels * bytes) {
                    String sizes = String.format("%d, %d",
                            buffer_size.getValue().intValue(), (long) xsize * ysize * format.num_channels * bytes);
Debug.printf("sizes: " + sizes);
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
Debug.printf("JXL_DEC_FULL_IMAGE");
                // Nothing to do. Do not yet return. If the image is an animation, more
                // full frames may be decoded. This example only keeps the last one.
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
Debug.printf("JXL_DEC_SUCCESS");
                // All decoding successfully finished.
                // It's not required to call JxlDecoderReleaseInput(dec) here since
                // the decoder will be destroyed.
                break;
            } else {
                throw new IllegalStateException("Unknown decoder status: " + status);
            }
        }

        pixels.position(0);
Debug.printf("pixel: " + pixels.capacity() + ", " + pixels.limit());
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

        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.setContentPane(new JScrollPane(panel));
        frame.setTitle("JPEG XL (JNA)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

} finally {
        DecodeLibrary.INSTANCE.JxlDecoderDestroy(dec);
        Library.INSTANCE.JxlResizableParallelRunnerDestroy(runner);
//        Library.INSTANCE.JxlThreadParallelRunnerDestroy(runner);
}
    }
}
