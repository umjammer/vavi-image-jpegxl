[![Release](https://jitpack.io/v/umjammer/vavi-image-jpegxl.svg)](https://jitpack.io/#umjammer/vavi-image-jpegxl)
[![Java CI](https://github.com/umjammer/vavi-image-jpegxl/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-image-jpegxl/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-image-jpegxl/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-image-jpegxl/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-image-jpegxl

Java JPEG XL decoder<br/>
wrapped [libjxl](https://github.com/libjxl/) by jna<br/>

<img src="https://upload.wikimedia.org/wikipedia/commons/0/06/JPEG_XL_logo.svg" width="160" alt="jpeg-xl logo"/>
<sub>© <a href="https://jpeg.org/jpegxl/">JPEG</a></sub>

## Install

### maven

* repository

https://jitpack.io/#umjammer/vavi-image-jpegxl

### jpeg-xl

```shell
$ brew install jpeg-xl
```

* jvm option

```
  -Djna.library.path=/usr/local/lib
```

## Usage

```java
    BufferedImage image = ImageIO.read(Paths.get("/foo/bar.jxl").toFile());
```

## References

 * [based on](https://github.com/libjxl/libjxl/blob/v0.9.x/examples/)
 * https://github.com/libjxl/libjxl
 * https://github.com/Dwedit/JxlSharp/blob/main/JxlSharp/JXL.cs
 * https://github.com/libjxl/libjxl/tree/v0.6.x/tools/jni/org/jpeg/jpegxl/wrapper (jni!!!)
 * https://github.com/Traneptora/jxlatte (pure java!!!)

## TODO

 * ~~jna version doesn't work well~~ 0.7.0 works fine
   * ~~different result per every execution (maybe memory related?)~~ 
 * ~~brew updated to 0.7.0~~ done
 * ~~jna version spi~~