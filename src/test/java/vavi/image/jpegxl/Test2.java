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
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
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

        PointerByReference dec = DecodeLibrary.INSTANCE.JxlDecoderCreate(null);
        if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS !=
                DecodeLibrary.INSTANCE.JxlDecoderSubscribeEvents(dec,
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO |
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING |
                        DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE)) {
            throw new IllegalStateException("JxlDecoderSubscribeEvents failed");
        }

Debug.println("JxlResizableParallelRunner: " + Library.JxlResizableParallelRunner);
        if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != DecodeLibrary.INSTANCE.JxlDecoderSetParallelRunner(dec,
                Library.JxlResizableParallelRunner,
                runner)) {
            throw new IllegalStateException("JxlDecoderSetParallelRunner failed\n");
        }

        JxlBasicInfo.ByReference info = new JxlBasicInfo.ByReference();
        // 32-bit floating point with 4-channel RGBA
        JxlPixelFormat format = new JxlPixelFormat(4, Library.JxlDataType.JXL_TYPE_FLOAT, Library.JxlEndianness.JXL_NATIVE_ENDIAN, 0);
        int bytes = format.necessaryBytes();

        ByteBuffer bbs = ByteBuffer.allocateDirect(jxl.length);
        bbs.put(jxl);
        DecodeLibrary.INSTANCE.JxlDecoderSetInput(dec, Native.getDirectBufferPointer(bbs), bbs.capacity());

        FloatBuffer pixels = null;
        int xsize = 0;
        int ysize = 0;
        while (true) {
            int status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(dec);

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
                LongByReference icc_size = new LongByReference();
                status = DecodeLibrary.INSTANCE.JxlDecoderGetICCProfileSize(
                        dec, format,
                        DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA, icc_size);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetICCProfileSize failed: " + status);
                }
                ByteBuffer icc_profile = ByteBuffer.allocateDirect((int) icc_size.getValue());
Debug.printf("icc_profile: " + icc_profile.capacity());
                status = DecodeLibrary.INSTANCE.JxlDecoderGetColorAsICCProfile(
                        dec, format,
                        DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA,
                        icc_profile, icc_profile.capacity());
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderGetColorAsICCProfile failed: " + status);
                }
            } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
Debug.printf("JXL_DEC_NEED_IMAGE_OUT_BUFFER");
                LongByReference buffer_size = new LongByReference();
                status = DecodeLibrary.INSTANCE.JxlDecoderImageOutBufferSize(dec, format, buffer_size);
                if (DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS != status) {
                    throw new IllegalStateException("JxlDecoderImageOutBufferSize failed: " + status);
                }
Debug.printf("buffer_size: " + (buffer_size.getValue()));
                if (buffer_size.getValue() != (long) xsize * ysize * format.num_channels * bytes) {
                    String sizes = String.format("%d, %d",
                            buffer_size.getValue(), (long) xsize * ysize * format.num_channels * bytes);
Debug.printf("sizes: " + sizes);
//                    throw new IllegalStateException("Invalid out buffer size " + sizes);
                }
                pixels = ByteBuffer.allocateDirect(xsize * ysize * format.num_channels * bytes).asFloatBuffer();
                Pointer pixels_buffer = Native.getDirectBufferPointer(pixels);
                int pixels_buffer_size = pixels.capacity() * bytes;
                status = DecodeLibrary.INSTANCE.JxlDecoderSetImageOutBuffer(
                        dec, format,
                        pixels_buffer, pixels_buffer_size);
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
        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                int p = y * xsize + x * 4;
                d[p + 4] = (byte) floatToByte(pixels.get());
                d[p + 3] = (byte) floatToByte(pixels.get());
                d[p + 2] = (byte) floatToByte(pixels.get());
                d[p + 1] = (byte) floatToByte(pixels.get());
            }
        }

        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.setContentPane(panel);
        frame.setTitle("JPEG XL");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        DecodeLibrary.INSTANCE.JxlDecoderDestroy(dec);
        Library.INSTANCE.JxlResizableParallelRunnerDestroy(runner);
    }

    static final int MODE = 3;  // Try changing me!

    // https://www.nayuki.io/res/portable-floatmap-format-io-java/PfmToPng.java
    private static int floatToByte(float x) {

        if (MODE == 0)  // Simple mapping of [0.0, 1.0] to [0, 255]
            return to8Bit(x);

        else if (MODE == 1)  // Mapping [0.0, 1.0] to [0, 255] with standard gamma correction of 2.2
            return to8Bit(linearToGamma(x, 2.2));

        else if (MODE == 2)  // Mapping [0.0, 1.0] to [0, 255] with sRGB gamma correction
            return to8Bit(linearToSrgb(x));

        else if (MODE == 3) {  // Film-like exposure curve output as sRGB
            final double GAIN = Math.log(2);  // By default, GAIN=log(2) maps 0 to 0, 1 to 1/2, 2 to 3/4, 3 to 7/8, etc.
            return to8Bit(linearToSrgb(1 - Math.exp(-x * GAIN)));

        } else
            throw new AssertionError();
    }

    // Returns a value in the range [0, 255].
    private static int to8Bit(double val) {
        if (val > 1)
            val = 1;
        else if (val < 0)
            val = 0;
        return (int) (val * 255 + 0.5);
    }

    private static double linearToGamma(double val, double gamma) {
        return Math.pow(val, 1 / gamma);
    }

    private static double linearToSrgb(double val) {
        if (val <= 0.0031308)
            return val * 12.92;
        else
            return Math.pow(val, 1 / 2.4) * 1.055 - 0.055;
    }
}
