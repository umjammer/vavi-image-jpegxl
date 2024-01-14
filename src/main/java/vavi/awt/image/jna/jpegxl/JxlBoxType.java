/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.jna.jpegxl;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;


/**
 * JxlBoxType.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-01-14 nsano initial version <br>
 */
public
class JxlBoxType extends PointerType {

    public JxlBoxType(Pointer address) {
        super(address);
    }

    public JxlBoxType() {
        super();
    }
}
