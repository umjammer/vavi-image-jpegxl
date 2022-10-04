/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.image.jpegxl;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import vavi.awt.image.jna.jpegxl.JxlBasicInfo;
import vavi.awt.image.jna.jpegxl.JxlColorEncoding;
import vavi.awt.image.jna.jpegxl.JxlMemoryManagerStruct;
import vavi.awt.image.jna.jpegxl.JxlPixelFormat;
import vavi.awt.image.jna.jpegxl.Library;
import vavi.awt.image.jna.jpegxl.decode.DecodeLibrary;
import vavi.awt.image.jna.jpegxl.encode.EncodeLibrary;
import vavi.util.Debug;


/**
 * JpegXL.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-02 nsano initial version <br>
 * @see "https://github.com/Dwedit/JxlSharp/blob/main/JxlSharp/JXL.cs"
 */
public class JpegXL {

    /** */
    private JpegXL() {}

    /** */
    private static final JpegXL instance = new JpegXL();

    /** */
    public static JpegXL getInstance() {
        return instance;
    }

    /**
     * Returns the number of bytes per pixel for the built-in BufferedImage type
     * @param pixelFormat The GDI+ pixel format
     * @return The number of bytes per pixel for that pixel format
     */
    int getBytesPerPixel(int pixelFormat) {
        switch (pixelFormat) {
//        case BufferedImage.Format16bppArgb1555:
        case BufferedImage.TYPE_USHORT_GRAY:
        case BufferedImage.TYPE_USHORT_555_RGB:
        case BufferedImage.TYPE_USHORT_565_RGB:
            return 2;
//        case BufferedImage.Format64bppArgb:
//        case BufferedImage.Format64bppPArgb:
//            return 8;
//        case BufferedImage.Format48bppRgb:
//            return 6;
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
        case BufferedImage.TYPE_INT_RGB:
            return 4;
        case BufferedImage.TYPE_3BYTE_BGR:
            return 3;
        case BufferedImage.TYPE_BYTE_INDEXED:
            return 1;
        case BufferedImage.TYPE_BYTE_BINARY:
//        case BufferedImage.Format4bppIndexed:
            throw new IllegalArgumentException();
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Loads a JXL file and returns a BufferedImage.
     * @param fileName 
     * @return Returns a bitmap, or returns null on failure.
     */
    public BufferedImage loadImage(String fileName) throws IOException {
        return loadImage(Files.readAllBytes(Paths.get(fileName)));
    }

    /**
     * Returns the basic info for a JXL file
     * @param data The bytes of the JXL file (can be partial data)
     * @param canTranscodeToJpeg 
     * Set to true if the image contains JPEG reconstruction data
     * @return A JxlBasicInfo object describing the image
     */
    public JxlBasicInfo.ByReference getBasicInfo(byte[] data, /*out*/ boolean[] canTranscodeToJpeg) {
        PointerByReference decoder = DecodeLibrary.INSTANCE.JxlDecoderCreate(null);
        if (decoder == null) {
            throw new IllegalStateException("JxlDecoderCreate");
        }
        DecodeLibrary.INSTANCE.JxlDecoderReset(decoder);
        try {
            JxlBasicInfo.ByReference basicInfo = new JxlBasicInfo.ByReference();
            canTranscodeToJpeg[0] = false;
            int status = DecodeLibrary.INSTANCE.JxlDecoderSetInput(decoder, data, data.length);
            if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                throw new IllegalStateException("JxlDecoderSetInput: " + status);
            }
            status = DecodeLibrary.INSTANCE.JxlDecoderSubscribeEvents(decoder, DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO | DecodeLibrary.JxlDecoderStatus.JXL_DEC_JPEG_RECONSTRUCTION | DecodeLibrary.JxlDecoderStatus.JXL_DEC_FRAME);
            if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                throw new IllegalStateException("JxlDecoderSubscribeEvents: " + status);
            }

            while (true) {
                status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(decoder);
                if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                    status = DecodeLibrary.INSTANCE.JxlDecoderGetBasicInfo(decoder, /*out*/ basicInfo);
                    if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        throw new IllegalStateException("JxlDecoderGetBasicInfo: " + status);
                    }
Debug.println("status is JXL_DEC_BASIC_INFO");
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_JPEG_RECONSTRUCTION) {
                    canTranscodeToJpeg[0] = true;
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_FRAME) {
Debug.println("status is JXL_DEC_FRAME");
                    return basicInfo;
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
Debug.println("status is JXL_DEC_SUCCESS");
                    return basicInfo;
                } else if (status >= DecodeLibrary.JxlDecoderStatus.JXL_DEC_ERROR && status < DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                    throw new IllegalStateException("JxlDecoderProcessInput: " + status);
                } else if (status < DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
Debug.println("status is JXL_DEC_BASIC_INFO");
                    return basicInfo;
                }
            }
        } finally {
            DecodeLibrary.INSTANCE.JxlDecoderDestroy(decoder);
        }
    }

    private void bgrSwap(int width, int height, int bytesPerPixel, DataBuffer scan0, int stride) {
        if (bytesPerPixel == 3) {
            for (int y = 0; y < height; y++) {
                byte[] p = ((DataBufferByte) scan0).getData();
                int pP = stride * y;
                for (int x = 0; x < width; x++) {
                    byte r = p[pP + 2];
                    byte b = p[pP + 0];
                    p[pP + 0] = r;
                    p[pP + 2] = b;
                    pP += 3;
                }
            }
        } else if (bytesPerPixel == 4) {
            for (int y = 0; y < height; y++) {
                byte[] p = ((DataBufferByte) scan0).getData();
                int pP = stride * y;
                for (int x = 0; x < width; x++) {
                    byte r = p[pP + 2];
                    byte b = p[pP + 0];
                    p[pP + 0] = r;
                    p[pP + 2] = b;
                    pP += 4;
                }
            }
        }
    }

    private void bgrSwap(Raster bitmapData, int pixelFormat) {
        int bytesPerPixel = 4;
        switch (pixelFormat) {
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
        case BufferedImage.TYPE_INT_RGB:
            bytesPerPixel = 4;
            break;
        case BufferedImage.TYPE_3BYTE_BGR:
            bytesPerPixel = 3;
            break;
        default:
            return;
        }
        bgrSwap(bitmapData.getWidth(), bitmapData.getHeight(), bytesPerPixel, bitmapData.getDataBuffer(), bitmapData.getWidth()/*Stride*/);
    }

    private void bgrSwap(BufferedImage bitmap) {
        Raster bitmapData = bitmap.getData(new Rectangle(0, 0, bitmap.getWidth(), bitmap.getHeight()));
        bgrSwap(bitmapData, bitmap.getType());
    }

    private BufferedImage asGrayscale(BufferedImage bitmap) {
        BufferedImage gray = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(bitmap, 0, 0, null);
        return gray;
    }

    /**
     * Loads a JXL image into a BufferedImageData object (SRGB or grayscale only, Image dimensions must match the file)
     * @param data The byte data for the JXL file
     * @param bitmapData A BufferedImageData object (from BufferedImage.LockBits)
     */
    public void loadImageIntoBufferedImage(byte[] data, Raster bitmapData, int pixelFormat) {
        loadImageIntoMemory(data, bitmapData.getWidth(), bitmapData.getHeight(), getBytesPerPixel(pixelFormat), bitmapData.getDataBuffer(), bitmapData.getWidth()/*Stride*/, true);
    }

    /**
     * Loads a JXL image into a BufferedImage object (SRGB or grayscale only, Image dimensions must match the file)
     * @param data The byte data for the JXL file
     * @param bitmap A BufferedImage object
     * @return success or not
     */
    public BufferedImage loadImageIntoBufferedImage(byte[] data, BufferedImage bitmap) {
        Raster bitmapData = bitmap.getData(new Rectangle(0, 0, bitmap.getWidth(), bitmap.getHeight()));
        if (bitmapData.getWidth() /*Stride*/ < 0) {
            throw new IllegalArgumentException("Stride can not be negative");
        }
        loadImageIntoBufferedImage(data, bitmap.getRaster(), bitmap.getType());
        if (bitmap.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            bitmap = asGrayscale(bitmap);
        }
        return bitmap;
    }

    /**
     * Loads a JXL file into a locked image buffer  (SRGB or grayscale only, Image dimensions and alpha channel must match the file)
     * @param data The byte data for the JXL file
     * @param width Width of the buffer (must match the file)
     * @param height Height of the buffer (must match the file)
     * @param bytesPerPixel Bytes per pixel (1 = grayscale, 3 = RGB, 4 = RGBA)
     * @param scan0 
     * Pointer to a locked scanline buffer
     * @param stride Distance between scanlines in the buffer (must be positive)
     * @param doBgrSwap If true, swaps the red and blue channel.  Required for GDI/GDI+ bitmaps which use BGR byte order.
     */
    public void loadImageIntoMemory(byte[] data, int width, int height, int bytesPerPixel, DataBuffer scan0, int stride, boolean doBgrSwap) {
        if (stride < 0) throw new IllegalArgumentException("Stride can not be negative");
        if (bytesPerPixel < 0 || bytesPerPixel > 4)
            throw new IllegalArgumentException("bytesPerPixel must be between 1 and 4: " + bytesPerPixel);

        JxlBasicInfo.ByReference basicInfo = new JxlBasicInfo.ByReference();
        PointerByReference decoder = DecodeLibrary.INSTANCE.JxlDecoderCreate(null);
        if (decoder == null) {
            throw new IllegalStateException("JxlDecoderCreate");
        }
DecodeLibrary.INSTANCE.JxlDecoderReset(decoder);
        try {
            int status = DecodeLibrary.INSTANCE.JxlDecoderSetInput(decoder, data, data.length);
            if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                throw new IllegalStateException("JxlDecoderSetInput: " + status);
            }
            status = DecodeLibrary.INSTANCE.JxlDecoderSubscribeEvents(decoder, DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO | DecodeLibrary.JxlDecoderStatus.JXL_DEC_FRAME | DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE);
            if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                throw new IllegalStateException("JxlDecoderSubscribeEvents: " + status);
            }
            JxlPixelFormat pixelFormat = new JxlPixelFormat();
            while (true) {
                status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(decoder);
                if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
Debug.println("status is JXL_DEC_BASIC_INFO");
                    status = DecodeLibrary.INSTANCE.JxlDecoderGetBasicInfo(decoder, /*out*/ basicInfo);
                    if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        if (width != basicInfo.xsize || height != basicInfo.ysize) {
                            throw new IllegalStateException(String.format("size is different: %dx%d, expected: %dx%d", width, height, basicInfo.xsize, basicInfo.ysize));
                        }
                    } else {
                        throw new IllegalStateException("JxlDecoderGetBasicInfo: " + status);
                    }
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_FRAME) {
Debug.println("status is JXL_DEC_FRAME: stride: " + stride);
                    pixelFormat.data_type = Library.JxlDataType.JXL_TYPE_UINT8;
                    pixelFormat.endianness = Library.JxlEndianness.JXL_NATIVE_ENDIAN;
                    pixelFormat.num_channels = bytesPerPixel;
                    pixelFormat.align = stride;

LongByReference buffer_size = new LongByReference();
status = DecodeLibrary.INSTANCE.JxlDecoderImageOutBufferSize(decoder, pixelFormat, buffer_size);
Debug.println("min: " + buffer_size.getValue());

                    FloatBuffer fb = ByteBuffer.allocateDirect(width * height * 4 * Float.BYTES * 10).asFloatBuffer();
                    Pointer fbp = Native.getDirectBufferPointer(fb);
Debug.println("buffer: bb: " + fb.capacity() + ", w*h*4: " + width * height * 4 + ", bytesPerPixel: " + bytesPerPixel);

                    status = DecodeLibrary.INSTANCE.JxlDecoderSetImageOutBuffer(decoder, pixelFormat, fbp, fb.capacity() * Float.BYTES);
                    if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        throw new IllegalStateException("JxlDecoderSetImageOutBuffer: " + status);
                    }

                    status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(decoder);
                    if (status > DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS && status < DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                        throw new IllegalStateException("JxlDecoderProcessInput: " + status);
                    }

                    fb.flip();
                    byte[] d = ((DataBufferByte) scan0).getData();
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int p = y * width + x * 4;
                            d[p + 0] = (byte) fb.get();
                            d[p + 1] = (byte) fb.get();
                            d[p + 2] = (byte) fb.get();
                            d[p + 3] = (byte) fb.get();
                        }
                    }

//                    if (doBgrSwap && bytesPerPixel >= 3) {
//                        bgrSwap(width, height, bytesPerPixel, scan0, stride);
//                    }
                    return;
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_COLOR_ENCODING) {
Debug.println("status is JXL_DEC_COLOR_ENCODING");
                    // Get the ICC color profile of the pixel data
                    LongByReference icc_size = new LongByReference();
                    status = DecodeLibrary.INSTANCE.JxlDecoderGetICCProfileSize(
                            decoder, pixelFormat, DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA, icc_size);
                    if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        throw new IllegalStateException("JxlDecoderGetICCProfileSize: " + status);
                    }

                    ByteBuffer icc_profile = ByteBuffer.allocateDirect((int) icc_size.getValue());
                    status = DecodeLibrary.INSTANCE.JxlDecoderGetColorAsICCProfile(
                            decoder, pixelFormat,
                            DecodeLibrary.JxlColorProfileTarget.JXL_COLOR_PROFILE_TARGET_DATA,
                            icc_profile, icc_profile.capacity());
                    if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        throw new IllegalStateException("JxlDecoderGetColorAsICCProfile: " + status);
                    }
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
Debug.println("status is JXL_DEC_NEED_IMAGE_OUT_BUFFER");
                    LongByReference buffer_size = new LongByReference();
                    status = DecodeLibrary.INSTANCE.JxlDecoderImageOutBufferSize(decoder, pixelFormat, buffer_size);
                    if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        throw new IllegalStateException("JxlDecoderImageOutBufferSize: " + status);
                    }
                    if (buffer_size.getValue() != basicInfo.xsize * basicInfo.ysize * 16L) {
                        throw new IllegalStateException(String.format("Invalid out buffer size %d %d\n",
                                buffer_size.getValue(), basicInfo.xsize * basicInfo.ysize * 16));
                    }

                    FloatBuffer pixels = ByteBuffer.allocateDirect(basicInfo.xsize * basicInfo.ysize * 4 * Float.BYTES).asFloatBuffer();
                    Pointer pixels_buffer = Native.getDirectBufferPointer(pixels);
                    int pixels_buffer_size = pixels.capacity() * Float.BYTES;
                    status = DecodeLibrary.INSTANCE.JxlDecoderSetImageOutBuffer(decoder, pixelFormat, pixels_buffer, pixels_buffer_size);
                    if (status != DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                        throw new IllegalStateException("JxlDecoderSetImageOutBuffer: " + status);
                    }
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE) {
Debug.println("status is JXL_DEC_FULL_IMAGE: Nothing to do. Do not yet return. If the image is an animation, more full frames may be decoded. This example only keeps the last one.");
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
Debug.println("status is JXL_DEC_SUCCESS");
                    return;
                } else if (status > DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS && status < DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                    throw new IllegalStateException("wrong status: " + status);
                }
            }
        } finally {
            DecodeLibrary.INSTANCE.JxlDecoderDestroy(decoder);
        }
    }

    /**
     * Suggests a pixel format based on the BasicInfo header
     * @param basicInfo A JxlBasicInfo object describing the image
     * @return Either BufferedImage.Format32bppArgb, BufferedImage.Format24bppRgb, or BufferedImage.Format8bppIndexed
     */
    public int suggestBufferedImage(JxlBasicInfo.ByReference basicInfo) {
        boolean isColor = basicInfo.num_color_channels > 1;
        boolean hasAlpha = basicInfo.alpha_bits > 0;
        int bitmapPixelFormat;
        if (isColor) {
            if (hasAlpha) {
                bitmapPixelFormat = BufferedImage.TYPE_INT_ARGB;
            } else {
                bitmapPixelFormat = BufferedImage.TYPE_3BYTE_BGR;
            }
        } else {
            if (hasAlpha) {
                bitmapPixelFormat = BufferedImage.TYPE_INT_ARGB;
            } else {
                bitmapPixelFormat = BufferedImage.TYPE_BYTE_INDEXED;
            }
        }
        return bitmapPixelFormat;
    }

    private BufferedImage createBlankBufferedImage(JxlBasicInfo.ByReference basicInfo) {
        int bitmapPixelFormat = suggestBufferedImage(basicInfo);
        BufferedImage bitmap = new BufferedImage(basicInfo.xsize, basicInfo.ysize, bitmapPixelFormat);
        return bitmap;
    }

    /**
     * Loads a JXL image as a BufferedImage
     * @param data The JXL bytes
     * @return Returns a bitmap on success, otherwise returns null
     */
    public BufferedImage loadImage(byte[] data) {
        BufferedImage bitmap = null;
        JxlBasicInfo.ByReference basicInfo = getBasicInfo(data, /*out*/ new boolean[1]);
Debug.println(basicInfo);
        bitmap = createBlankBufferedImage(basicInfo);
        return loadImageIntoBufferedImage(data, bitmap);
    }

    /**
     * Transcodes a JXL file back to a JPEG file, only possible if the image was originally a JPEG file.
     * @param jxlBytes File bytes for the JXL file
     * @return The resulting com.sun.imageio.plugins.jpeg.JPEG bytes on success, otherwise returns null
     */
    public byte[] transcodeJxlToJpeg(byte[] jxlBytes) {
        byte[] buffer = new byte[0];
        int outputPosition = 0;
        //byte[] buffer = new byte[1024 * 1024];
        PointerByReference jxlDecoder = DecodeLibrary.INSTANCE.JxlDecoderCreate(null);
        try {
            DecodeLibrary.INSTANCE.JxlDecoderSetInput(jxlDecoder, jxlBytes, jxlBytes.length);
            JxlBasicInfo.ByReference basicInfo = new JxlBasicInfo.ByReference();
            boolean[] canTranscodeToJpeg = new boolean[1];
            DecodeLibrary.INSTANCE.JxlDecoderSubscribeEvents(jxlDecoder, DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO | DecodeLibrary.JxlDecoderStatus.JXL_DEC_JPEG_RECONSTRUCTION | DecodeLibrary.JxlDecoderStatus.JXL_DEC_FRAME | DecodeLibrary.JxlDecoderStatus.JXL_DEC_FULL_IMAGE);
            while (true) {
                int status = DecodeLibrary.INSTANCE.JxlDecoderProcessInput(jxlDecoder);
                if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                    status = DecodeLibrary.INSTANCE.JxlDecoderGetBasicInfo(jxlDecoder, /*out*/ basicInfo);
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_JPEG_RECONSTRUCTION) {
                    canTranscodeToJpeg[0] = true;
                    buffer = new byte[1024 * 1024];
                    DecodeLibrary.INSTANCE.JxlDecoderSetJPEGBuffer(jxlDecoder, ByteBuffer.wrap(buffer), outputPosition);
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_PREVIEW_OUT_BUFFER) {
                    outputPosition += buffer.length - DecodeLibrary.INSTANCE.JxlDecoderReleaseJPEGBuffer(jxlDecoder);
                    byte[] nextBuffer = new byte[buffer.length * 4];
                    if (outputPosition > 0) {
                        System.arraycopy(buffer, 0, nextBuffer, 0, outputPosition);
                    }
                    buffer = nextBuffer;
                    DecodeLibrary.INSTANCE.JxlDecoderSetJPEGBuffer(jxlDecoder, ByteBuffer.wrap(buffer), outputPosition);
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_FRAME) {
                    //if (!canTranscodeToJpeg) {
                    //	return null;
                    //}
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_SUCCESS) {
                    outputPosition += buffer.length - DecodeLibrary.INSTANCE.JxlDecoderReleaseJPEGBuffer(jxlDecoder);
                    byte[] jpegBytes;
                    if (buffer.length == outputPosition) {
                        jpegBytes = buffer;
                    } else {
                        jpegBytes = new byte[outputPosition];
                        System.arraycopy(buffer, 0, jpegBytes, 0, outputPosition);
                    }
                    return jpegBytes;
                } else if (status == DecodeLibrary.JxlDecoderStatus.JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
                    return null;
                } else if (status >= DecodeLibrary.JxlDecoderStatus.JXL_DEC_ERROR && status < DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                    return null;
                } else if (status < DecodeLibrary.JxlDecoderStatus.JXL_DEC_BASIC_INFO) {
                    return null;
                }
            }
        } finally {
            DecodeLibrary.INSTANCE.JxlDecoderDestroy(jxlDecoder);
        }
    }

    /**
     * Transcodes a JPEG file to a JXL file
     * @param jpegBytes File bytes for the JPEG file
     * @return The resulting JXL bytes on success, otherwise returns null
     */
    public byte[] transcodeJpegToJxl(byte[] jpegBytes) {
        JxlMemoryManagerStruct ms = new JxlMemoryManagerStruct();
        PointerByReference encoder = EncodeLibrary.INSTANCE.JxlEncoderCreate(ms);
        try {
            int status = EncodeLibrary.INSTANCE.JxlEncoderStoreJPEGMetadata(encoder, Library.JXL_TRUE);
            PointerByReference options = EncodeLibrary.INSTANCE.JxlEncoderOptionsCreate(encoder, null);
            status = EncodeLibrary.INSTANCE.JxlEncoderAddJPEGFrame(options, jpegBytes, jpegBytes.length);
            EncodeLibrary.INSTANCE.JxlEncoderCloseInput(encoder);
            status = processOutput(encoder);
            if (status == EncodeLibrary.JxlEncoderStatus.JXL_ENC_SUCCESS) {
                return ms.opaque.getByteArray(0, ms.size());
            }
            return null;
        } finally {
            EncodeLibrary.INSTANCE.JxlEncoderDestroy(encoder);
        }
    }

    private static void createBasicInfo(BufferedImage bitmap, JxlBasicInfo basicInfo, JxlPixelFormat pixelFormat, JxlColorEncoding colorEncoding) {
        pixelFormat.data_type = Library.JxlDataType.JXL_TYPE_UINT8;
        pixelFormat.endianness = Library.JxlEndianness.JXL_NATIVE_ENDIAN;
        if (bitmap.getType() == BufferedImage.TYPE_INT_ARGB || bitmap.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
            basicInfo.alpha_bits = 8;
            basicInfo.num_color_channels = 3;
            basicInfo.num_extra_channels = 1;
            if (bitmap.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
                basicInfo.alpha_premultiplied = Library.JXL_TRUE;
            }
            pixelFormat.num_channels = 4;
        } else if (bitmap.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            basicInfo.num_color_channels = 1;
            pixelFormat.num_channels = 1;
        } else {
            basicInfo.num_color_channels = 3;
            pixelFormat.num_channels = 3;
        }
        basicInfo.bits_per_sample = 8;
//        basicInfo.IntrinsicWidth = bitmap.getWidth();
//        basicInfo.IntrinsicHeight = bitmap.getHeight();
        basicInfo.xsize = bitmap.getWidth();
        basicInfo.ysize = bitmap.getHeight();
        int isGray = basicInfo.num_color_channels == 1 ? Library.JXL_TRUE : Library.JXL_FALSE;
        EncodeLibrary.INSTANCE.JxlColorEncodingSetToSRGB(colorEncoding, isGray);
    }

    /**
     * Returns an RGB/RGBA byte array with Blue and Red swapped
     * @param bitmap The bitmap to return a copy of
     * @param hasAlpha True to include an alpha channel
     * @return The image converted to an array (with blue and red swapped)
     */
    private static byte[] copyBufferedImageAndBgrSwap(BufferedImage bitmap, boolean hasAlpha) {
        if (hasAlpha) {
            byte[] newBytes = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
            Raster bitmapData = bitmap.getData(new Rectangle(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            byte[] pBytes = newBytes;
            for (int y = 0; y < bitmap.getHeight(); y++) {
                byte[] src = ((DataBufferByte) bitmapData.getDataBuffer()).getData();
                int srcP = bitmapData.getWidth()/*Stride*/ * y;
                int destP = bitmap.getWidth() * 4 * y;
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    pBytes[destP + 0] = src[srcP + 2];
                    pBytes[destP + 1] = src[srcP + 1];
                    pBytes[destP + 2] = src[srcP + 0];
                    pBytes[destP + 3] = src[srcP + 3];
                    destP += 4;
                    srcP += 4;
                }
            }
            return newBytes;
        } else {
            byte[] newBytes = new byte[bitmap.getWidth() * bitmap.getHeight() * 3];
            Raster bitmapData = bitmap.getData(new Rectangle(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            byte[] pBytes = newBytes;
            for (int y = 0; y < bitmap.getHeight(); y++) {
                byte[] src = ((DataBufferByte) bitmapData.getDataBuffer()).getData();
                int srcP = bitmapData.getWidth()/*Stride*/ * y;
                int destP = bitmap.getWidth() * 3 * y;
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    pBytes[destP + 0] = src[srcP + 2];
                    pBytes[destP + 1] = src[srcP + 1];
                    pBytes[destP + 2] = src[srcP + 0];
                    destP += 3;
                    srcP += 3;
                }
            }
            return newBytes;
        }
    }

    /**
     * Encodes a JXL file using the settings provided
     * @param bitmap The bitmap to save
     * @param lossyMode Whether to save lossless, lossy, photo, or drawing
     * @param frameDistance Sets the distance level for lossy compression<br/>
     * target max butteraugli distance, lower = higher quality. <br/>
     * Range: 0 .. 15.<br/>
     * 0.0 = mathematically lossless (however, use lossless mode instead to use true lossless,
     * as setting distance to 0 alone is not the only requirement).<br/>
     * 1.0 = visually lossless. <br/>
     * Recommended range: 0.5 .. 3.0. <br/>
     * Default value: 1.0.
     * @param settings The settings to save the image with
     * @return The JXL file, or null on failure
     */
    public byte[] encodeJxl(BufferedImage bitmap, JxlLossyMode lossyMode, float frameDistance, PointerByReference settings) {
        int status;
        JxlMemoryManagerStruct ms = new JxlMemoryManagerStruct();
        PointerByReference encoder = EncodeLibrary.INSTANCE.JxlEncoderCreate(ms);
        try {
            JxlBasicInfo basicInfo = new JxlBasicInfo();
            JxlPixelFormat pixelFormat = new JxlPixelFormat();
            JxlColorEncoding colorEncoding = new JxlColorEncoding();
            createBasicInfo(bitmap, basicInfo, pixelFormat, colorEncoding);
            boolean hasAlpha = basicInfo.alpha_bits > 0;
            byte[] bitmapCopy = copyBufferedImageAndBgrSwap(bitmap, hasAlpha);
            status = EncodeLibrary.INSTANCE.JxlEncoderSetBasicInfo(encoder, basicInfo);
            status = EncodeLibrary.INSTANCE.JxlEncoderSetColorEncoding(encoder, colorEncoding);
            PointerByReference options = EncodeLibrary.INSTANCE.JxlEncoderOptionsCreate(encoder, settings);
            if (lossyMode == JxlLossyMode.Lossless) {
                status = EncodeLibrary.INSTANCE.JxlEncoderOptionsSetLossless(encoder, Library.JXL_TRUE);
                status = EncodeLibrary.INSTANCE.JxlEncoderOptionsSetDistance(encoder, 0);
            } else {
                status = EncodeLibrary.INSTANCE.JxlEncoderOptionsSetDistance(encoder, frameDistance);
                status = EncodeLibrary.INSTANCE.JxlEncoderOptionsSetLossless(encoder, Library.JXL_FALSE);
            }

            ByteBuffer bb = ByteBuffer.allocateDirect(bitmapCopy.length);
            bb.put(bitmapCopy);
            Pointer p = Native.getDirectBufferPointer(bb);
            status = EncodeLibrary.INSTANCE.JxlEncoderAddImageFrame(options, pixelFormat, p, bitmapCopy.length);
            EncodeLibrary.INSTANCE.JxlEncoderCloseInput(encoder);
            status = processOutput(encoder);
            byte[] bytes = null;
            if (status == EncodeLibrary.JxlEncoderStatus.JXL_ENC_SUCCESS) {
                bytes = ms.opaque.getByteArray(0, ms.size());
            }
            return bytes;
        } finally {
            EncodeLibrary.INSTANCE.JxlEncoderDestroy(encoder);
        }
    }

    /** */
    public int processOutput(PointerByReference encoder) {
        ByteBuffer compressed = ByteBuffer.allocateDirect(64);

        int next_out = 0;
        long[] avail_out = new long[] { compressed.capacity() };
        int status = EncodeLibrary.JxlEncoderStatus.JXL_ENC_NEED_MORE_OUTPUT;
        while (status == EncodeLibrary.JxlEncoderStatus.JXL_ENC_NEED_MORE_OUTPUT) {
            PointerByReference p1 = new PointerByReference(Native.getDirectBufferPointer(compressed).getPointer(next_out));
            status = EncodeLibrary.INSTANCE.JxlEncoderProcessOutput(encoder, p1, avail_out);
            if (status == EncodeLibrary.JxlEncoderStatus.JXL_ENC_NEED_MORE_OUTPUT) {
                compressed = ByteBuffer.allocateDirect(compressed.capacity() * 2);
                avail_out[0] = compressed.capacity() - next_out;
            }
        }
        return status;
    }

    /**
     * Lossless/Lossy Mode for JXL.EncodeJxl
     */
    public enum JxlLossyMode {
        /** Lossless mode */
        Lossless,
        /** Automatic selection */
        Default,
        /** VarDCT mode (like JPEG) */
        Photo,
        /** Modular Mode for drawn images, not for things that have previously been saved as JPEG. */
        Drawing
    }
}
